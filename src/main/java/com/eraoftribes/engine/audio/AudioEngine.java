package com.eraoftribes.engine.audio;

import com.eraoftribes.engine.EngineConfig.AudioConfig;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioEngine {
    private final AudioConfig config;
    private final Map<String, SoundClip> clips = new HashMap<>();
    private final ExecutorService sfxPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "audio-sfx");
        t.setDaemon(true);
        return t;
    });
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
            sfxPool.submit(() -> playSource(clip, sfxVolume * masterVolume));
        }
    }

    private void playMusic(SoundClip clip) {
        if (currentMusic != null) {
            currentMusic.cancel();
        }
        currentMusic = new MusicPlayer(clip, this);
        currentMusic.start();
    }

    void playSource(SoundClip clip, double volume) {
        var file = new File(clip.file);
        if (!file.exists()) {
            System.err.println("[AudioEngine] File not found: " + clip.file);
            return;
        }
        SourceDataLine line = null;
        try (var ais = AudioSystem.getAudioInputStream(file)) {
            var baseFormat = ais.getFormat();
            var targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false
            );
            try (var das = AudioSystem.getAudioInputStream(targetFormat, ais)) {
                var info = new DataLine.Info(SourceDataLine.class, targetFormat);
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(targetFormat);
                setVolume(line, volume);
                line.start();
                var buf = new byte[4096];
                int n;
                while ((n = das.read(buf)) != -1) {
                    if (clip.stopped) break;
                    line.write(buf, 0, n);
                }
                line.drain();
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("[AudioEngine] Playback error: " + clip.file + " - " + e.getMessage());
        } finally {
            if (line != null) line.close();
        }
    }

    void setVolume(SourceDataLine line, double vol) {
        if (vol < 0.0) vol = 0.0;
        if (vol > 1.0) vol = 1.0;
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            var gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log10(Math.max(vol, 0.0001)) * 20.0);
            dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
            gain.setValue(dB);
        }
    }

    public void stop(String id) {
        var clip = clips.get(id);
        if (clip != null) clip.stopped = true;
    }

    public void stopAll() {
        if (currentMusic != null) {
            currentMusic.cancel();
            currentMusic = null;
        }
        for (var clip : clips.values()) clip.stopped = true;
    }

    public void setVolume(double volume) {
        this.masterVolume = volume;
    }

    public void setMusicVolume(double v) {
        this.musicVolume = v;
        if (currentMusic != null) currentMusic.updateVolume();
    }

    public void setSFXVolume(double v) {
        this.sfxVolume = v;
    }

    private static class SoundClip {
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

    private static class MusicPlayer extends Thread {
        private final SoundClip clip;
        private final AudioEngine engine;
        private SourceDataLine line;

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
                    var baseFormat = ais.getFormat();
                    var targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                        baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false
                    );
                    try (var das = AudioSystem.getAudioInputStream(targetFormat, ais)) {
                        var info = new DataLine.Info(SourceDataLine.class, targetFormat);
                        line = (SourceDataLine) AudioSystem.getLine(info);
                        line.open(targetFormat);
                        engine.setVolume(line, clip.volume * engine.musicVolume * engine.masterVolume);
                        line.start();
                        var buf = new byte[4096];
                        int n;
                        while ((n = das.read(buf)) != -1) {
                            if (clip.stopped) break;
                            line.write(buf, 0, n);
                        }
                        line.drain();
                    }
                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                    System.err.println("[AudioEngine] Music error: " + e.getMessage());
                    break;
                } finally {
                    if (line != null) {
                        line.close();
                        line = null;
                    }
                }
            } while (clip.loop && !clip.stopped);
        }

        void cancel() {
            clip.stopped = true;
            interrupt();
            if (line != null) line.close();
        }

        void updateVolume() {
            if (line != null && line.isOpen()) {
                engine.setVolume(line, clip.volume * engine.musicVolume * engine.masterVolume);
            }
        }
    }
}
