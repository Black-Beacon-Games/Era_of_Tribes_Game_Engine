package com.eraoftribes.engine.save;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private final String saveDir;
    private final List<SaveSlot> slots = new ArrayList<>();

    public SaveManager(String saveDir) {
        this.saveDir = saveDir;
        new File(saveDir).mkdirs();
        System.out.println("[SaveManager] Directory: " + saveDir);
    }

    public SaveSlot save(String name, Object data) {
        var slot = new SaveSlot(name, data);
        var file = new File(saveDir, sanitize(name) + ".sav");
        try (var oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
            slot.timestamp = LocalDateTime.now().toString();
            slots.add(slot);
            System.out.println("[SaveManager] Saved: " + name);
        } catch (IOException e) {
            System.err.println("[SaveManager] Save failed: " + e.getMessage());
        }
        return slot;
    }

    public Object load(String name) {
        var file = new File(saveDir, sanitize(name) + ".sav");
        if (!file.exists()) return null;
        try (var ois = new ObjectInputStream(new FileInputStream(file))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SaveManager] Load failed: " + e.getMessage());
            return null;
        }
    }

    public void saveAll() {
        System.out.println("[SaveManager] Autosave...");
    }

    public List<SaveSlot> listSaves() { return slots; }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public static class SaveSlot {
        public final String name;
        public final Object data;
        public String timestamp;

        SaveSlot(String name, Object data) {
            this.name = name;
            this.data = data;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
