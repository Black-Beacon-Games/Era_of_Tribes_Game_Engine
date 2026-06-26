package com.eraoftribes.engine.audio;

import com.eraoftribes.engine.EngineConfig.AudioConfig;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioEngine {
    private static final int FADE_MS = 300;
    private static final int FADE_STEPS = 30;

    private final AudioConfig config;
    private final Map<String, SoundClip> clips = new HashMap<>();
    private final ExecutorService sfxPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "audio-sfx");
        t.setDaemon(true);
        return t;
    });
    private final CopyOnWriteArrayList<ActiveSFX> activeSFX = new CopyOnWriteArrayList<>();
    private MusicPlayer currentMusic;
    private double masterVolume = 1.0;
    private double musicVolume = 1.0;
    private double sfxVolume = 1.0;

    public AudioEngine(AudioConfig config) {
        this.config = config;
        this.masterVolume = config.masterVolume;
        this.musicVolume = config.musicVolume;
        this.sfxVolume = config.sfxVolume;
        System.out.println("[AudioEngine] Initialized.");
    }

    public void loadTrack(String id, String file, boolean loop, double volume) {
        clips.put(id, new SoundClip(file, loop, volume));
    }

    public void play(String id) {
        var clip = clips.get(id);
        if (clip == null) {
            System.err.println("[AudioEngine] Track not found: " + id);
            return;
        }
        if (clip.loop) {
            playMusic(clip);
        } else {
            var sfx = new ActiveSFX(clip);
            activeSFX.add(sfx);
            sfxPool.submit(() -> {
                try {
                    playSource(sfx);
                } finally {
                    activeSFX.remove(sfx);
                }
            });
        }
    }

    private void playMusic(SoundClip clip) {
        if (currentMusic != null) {
            currentMusic.cancel();
        }
        currentMusic = new MusicPlayer(clip, this);
        currentMusic.start();
    }

    private void playSource(ActiveSFX sfx) {
        var file = new File(sfx.clip.file);
        if (!file.exists()) {
            System.err.println("[AudioEngine] File not found: " + sfx.clip.file);
            return;
        }
        SourceDataLine line = null;
        try (var ais = AudioSystem.getAudioInputStream(file)) {
            var fmt = toPCM(ais.getFormat());
            try (var das = AudioSystem.getAudioInputStream(fmt, ais)) {
                var info = new DataLine.Info(SourceDataLine.class, fmt);
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(fmt);
                sfx.line = line;
                applyVolume(line, sfx.clip.volume * sfxVolume * masterVolume);
                line.start();
                var buf = new byte[4096];
                int n;
                while ((n = das.read(buf)) != -1) {
                    if (sfx.stopped) break;
                    line.write(buf, 0, n);
                }
                if (!sfx.stopped) line.drain();
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("[AudioEngine] Playback error: " + sfx.clip.file + " - " + e.getMessage());
        } finally {
            sfx.line = null;
            if (line != null) line.close();
        }
    }

    private static AudioFormat toPCM(AudioFormat src) {
        return new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            src.getSampleRate(), 16, src.getChannels(),
            src.getChannels() * 2, src.getSampleRate(), false
        );
    }

    void applyVolume(SourceDataLine line, double vol) {
        if (vol < 0.0) vol = 0.0;
        if (vol > 1.0) vol = 1.0;
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            var gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log10(Math.max(vol, 0.0001)) * 20.0);
            dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
            gain.setValue(dB);
        }
    }

    private void applyFadeOut(SourceDataLine line) {
        if (line == null || !line.isOpen()) return;
        try {
            if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
            var gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            float start = gain.getValue();
            float end = gain.getMinimum();
            for (int i = 1; i <= FADE_STEPS; i++) {
                float t = (float) i / FADE_STEPS;
                gain.setValue(start + (end - start) * t);
                Thread.sleep(FADE_MS / FADE_STEPS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop(String id) {
        var clip = clips.get(id);
        if (clip == null) return;
        clip.stopped = true;
        if (currentMusic != null && currentMusic.clip == clip) {
            currentMusic.cancel();
        }
        for (var sfx : activeSFX) {
            if (sfx.clip == clip) sfx.close();
        }
    }

    public void stopAll() {
        if (currentMusic != null) {
            currentMusic.cancel();
            currentMusic = null;
        }
        for (var sfx : activeSFX) sfx.close();
        for (var clip : clips.values()) clip.stopped = true;
    }

    public void setVolume(double v) {
        this.masterVolume = v;
        if (currentMusic != null) currentMusic.updateVolume();
        updateActiveSFXVolume();
    }

    public void setMusicVolume(double v) {
        this.musicVolume = v;
        if (currentMusic != null) currentMusic.updateVolume();
    }

    public void setSFXVolume(double v) {
        this.sfxVolume = v;
        updateActiveSFXVolume();
    }

    private void updateActiveSFXVolume() {
        for (var sfx : activeSFX) sfx.updateVolume(this);
    }

    public void clearTracks() {
        stopAll();
        clips.clear();
    }

    public Map<String, SoundClip> getClips() {
        return clips;
    }

    public MusicPlayer getCurrentMusic() {
        return currentMusic;
    }

    public CopyOnWriteArrayList<ActiveSFX> getActiveSFX() {
        return activeSFX;
    }

    static class SoundClip {
        final String file;
        final boolean loop;
        final double volume;
        volatile boolean stopped;

        SoundClip(String file, boolean loop, double volume) {
            this.file = file;
            this.loop = loop;
            this.volume = volume;
        }
    }

    class ActiveSFX {
        final SoundClip clip;
        volatile boolean stopped;
        volatile SourceDataLine line;

        ActiveSFX(SoundClip clip) {
            this.clip = clip;
        }

        void updateVolume(AudioEngine engine) {
            var l = line;
            if (l != null && l.isOpen()) {
                engine.applyVolume(l, clip.volume * engine.sfxVolume * engine.masterVolume);
            }
        }

        void close() {
            stopped = true;
            var l = line;
            if (l != null) l.close();
        }
    }

    static class MusicPlayer extends Thread {
        final SoundClip clip;
        private final AudioEngine engine;
        private volatile SourceDataLine line;

        MusicPlayer(SoundClip clip, AudioEngine engine) {
            super("audio-music");
            setDaemon(true);
            this.clip = clip;
            this.engine = engine;
        }

        @Override
        public void run() {
            var file = new File(clip.file);
            if (!file.exists()) {
                System.err.println("[AudioEngine] Music file not found: " + clip.file);
                return;
            }
            do {
                try (var ais = AudioSystem.getAudioInputStream(file)) {
                    var fmt = toPCM(ais.getFormat());
                    try (var das = AudioSystem.getAudioInputStream(fmt, ais)) {
                        if (line == null || !line.isOpen()) {
                            var info = new DataLine.Info(SourceDataLine.class, fmt);
                            line = (SourceDataLine) AudioSystem.getLine(info);
                            line.open(fmt);
                            engine.applyVolume(line, clip.volume * engine.musicVolume * engine.masterVolume);
                            line.start();
                        }
                        var buf = new byte[4096];
                        int n;
                        while ((n = das.read(buf)) != -1) {
                            if (clip.stopped) break;
                            line.write(buf, 0, n);
                        }
                        if (!clip.stopped) line.drain();
                    }
                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                    System.err.println("[AudioEngine] Music error: " + e.getMessage());
                    break;
                }
            } while (clip.loop && !clip.stopped);
            if (line != null) {
                line.close();
                line = null;
            }
        }

        void cancel() {
            clip.stopped = true;
            interrupt();
            var l = line;
            if (l != null && l.isOpen()) {
                engine.applyFadeOut(l);
                l.close();
                line = null;
            }
        }

        void updateVolume() {
            var l = line;
            if (l != null && l.isOpen()) {
                engine.applyVolume(l, clip.volume * engine.musicVolume * engine.masterVolume);
            }
        }
    }
}
