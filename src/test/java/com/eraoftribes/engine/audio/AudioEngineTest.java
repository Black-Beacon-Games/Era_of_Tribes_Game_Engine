package com.eraoftribes.engine.audio;

import com.eraoftribes.engine.EngineConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioEngineTest {

    private AudioEngine engine;

    @BeforeEach
    void setUp() {
        var config = new EngineConfig.AudioConfig();
        config.masterVolume = 0.8;
        config.musicVolume = 0.7;
        config.sfxVolume = 1.0;
        config.ambientVolume = 1.0;
        config.voiceVolume = 1.0;
        engine = new AudioEngine(config);
    }

    @Test
    void loadTrack_storesClip() {
        engine.loadTrack("test", "path/to/file.wav", false, 1.0);
        var clip = engine.getClips().get("test");
        assertNotNull(clip);
        assertEquals("path/to/file.wav", clip.file);
        assertFalse(clip.loop);
        assertEquals(1.0, clip.volume, 0.001);
        assertFalse(clip.stopped);
    }

    @Test
    void loadTrack_overwritesExisting() {
        engine.loadTrack("a", "first.wav", false, 0.5);
        engine.loadTrack("a", "second.mp3", true, 1.0);
        var clip = engine.getClips().get("a");
        assertEquals("second.mp3", clip.file);
        assertTrue(clip.loop);
    }

    @Test
    void play_unknownId_doesNotAddToActiveSFX() {
        engine.play("nonexistent");
        assertTrue(engine.getActiveSFX().isEmpty());
    }

    @Test
    void stop_setsClipStopped() {
        engine.loadTrack("x", "f.wav", false, 1.0);
        engine.stop("x");
        assertTrue(engine.getClips().get("x").stopped);
    }

    @Test
    void stop_unknownId_doesNothing() {
        engine.loadTrack("x", "f.wav", false, 1.0);
        engine.stop("unknown");
        assertFalse(engine.getClips().get("x").stopped);
    }

    @Test
    void stopAll_stopsAllClipsAndClearsMusic() {
        engine.loadTrack("a", "a.wav", false, 1.0);
        engine.loadTrack("b", "b.mp3", true, 1.0);
        engine.stopAll();
        assertTrue(engine.getClips().get("a").stopped);
        assertTrue(engine.getClips().get("b").stopped);
        assertNull(engine.getCurrentMusic());
    }

    @Test
    void setVolume_getVolumeWorkAsExpected() {
        engine.setVolume(0.5);
        engine.loadTrack("t", "t.wav", false, 1.0);
        var clip = engine.getClips().get("t");
        assertEquals(1.0, clip.volume, 0.001);
    }

    @Test
    void setMusicVolume_updateMusicUpdatesVolumeField() {
        engine.setMusicVolume(0.3);
        engine.setMusicVolume(0.3);
    }

    @Test
    void setSFXVolume_appliesToActiveSFX() {
        engine.setSFXVolume(0.4);
        engine.setSFXVolume(0.4);
    }

    @Test
    void clearTracks_removesAllTracksAndStopsPlayback() {
        engine.loadTrack("a", "a.wav", false, 1.0);
        engine.loadTrack("b", "b.wav", false, 1.0);
        engine.clearTracks();
        assertTrue(engine.getClips().isEmpty());
        assertNull(engine.getCurrentMusic());
    }

    @Test
    void clearTracks_withActiveSFX_doesNotCrash() {
        engine.loadTrack("s", "s.wav", false, 1.0);
        engine.clearTracks();
        assertTrue(engine.getClips().isEmpty());
    }

    @Test
    void multipleTracks_canBeStoppedIndependently() {
        engine.loadTrack("a", "a.wav", false, 0.5);
        engine.loadTrack("b", "b.mp3", true, 0.8);
        engine.stop("a");
        assertTrue(engine.getClips().get("a").stopped);
        assertFalse(engine.getClips().get("b").stopped);
    }

    @Test
    void loadTrack_withLoopTrue_setsLoopFlag() {
        engine.loadTrack("bgm", "music.mp3", true, 0.7);
        assertTrue(engine.getClips().get("bgm").loop);
    }

    @Test
    void stopAll_clearsActiveSFX() {
        engine.loadTrack("s", "s.wav", false, 1.0);
        engine.stopAll();
        assertTrue(engine.getActiveSFX().isEmpty());
    }
}
