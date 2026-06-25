package com.eraoftribes.engine.audio;

import com.eraoftribes.engine.EngineConfig.AudioConfig;
import java.util.HashMap;
import java.util.Map;

public class AudioEngine {
    private final AudioConfig config;
    private final Map<String, AudioTrack> tracks = new HashMap<>();

    public AudioEngine(AudioConfig config) {
        this.config = config;
        System.out.println("[AudioEngine] Initialized.");
    }

    public void loadTrack(String id, String file, boolean loop, double volume) {
        tracks.put(id, new AudioTrack(file, loop, volume));
    }

    public void play(String id) {
        var track = tracks.get(id);
        if (track != null) track.play();
    }

    public void stop(String id) {
        var track = tracks.get(id);
        if (track != null) track.stop();
    }

    public void stopAll() {
        for (var track : tracks.values()) track.stop();
    }

    public void setVolume(double volume) {}
    public void setMusicVolume(double v) {}
    public void setSFXVolume(double v) {}

    private static class AudioTrack {
        private final String file;
        private final boolean loop;
        private final double volume;
        private boolean playing;

        AudioTrack(String file, boolean loop, double volume) {
            this.file = file;
            this.loop = loop;
            this.volume = volume;
        }

        void play() { playing = true; }
        void stop() { playing = false; }
    }
}
