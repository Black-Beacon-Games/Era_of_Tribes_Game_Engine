package com.eraoftribes.engine.networking;

import com.eraoftribes.engine.EngineConfig.MultiplayerConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class NetworkManager {
    private final MultiplayerConfig config;
    private final List<NetworkPlayer> players = new ArrayList<>();
    private boolean connected;
    private boolean isHost;

    public NetworkManager(MultiplayerConfig config) {
        this.config = config;
        System.out.println("[NetworkManager] Max players: " + config.maxPlayers);
    }

    public void connect(String host, int port) {
        System.out.println("[NetworkManager] Connecting to " + host + ":" + port);
        connected = true;
    }

    public void disconnect() {
        connected = false;
        System.out.println("[NetworkManager] Disconnected.");
    }

    public void hostLobby(String name) {
        isHost = true;
        System.out.println("[NetworkManager] Hosting lobby: " + name);
    }

    public void joinLobby(String lobbyId) {
        System.out.println("[NetworkManager] Joining lobby: " + lobbyId);
    }

    public void broadcast(String event, Object data) {}
    public void sendTo(String playerId, String event, Object data) {}

    public boolean isConnected() { return connected; }
    public boolean isHost() { return isHost; }
    public List<NetworkPlayer> getPlayers() { return players; }

    public static class NetworkPlayer {
        private final String id;
        private final String name;
        private boolean ready;

        public NetworkPlayer(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public boolean isReady() { return ready; }
        public void setReady(boolean r) { ready = r; }
    }
}
