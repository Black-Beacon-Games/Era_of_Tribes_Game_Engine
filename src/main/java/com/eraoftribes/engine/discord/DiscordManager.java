package com.eraoftribes.engine.discord;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordManager {
    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;
    private static final int OP_CLOSE = 2;
    private static final int OP_PING = 3;
    private static final int OP_PONG = 4;

    private final DiscordConfig config;
    private WinNT.HANDLE pipeHandle;
    private volatile boolean connected;
    private long startTime;
    private final Gson gson = new Gson();
    private Thread readerThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public DiscordManager(DiscordConfig config) {
        this.config = config;
    }

    public void init() {
        if (!config.enabled || config.applicationId.isEmpty()) {
            System.out.println("[Discord] Disabled or no Application ID set.");
            return;
        }
        System.out.println("[Discord] Connecting...");
        connect();
        if (connected) {
            startTime = System.currentTimeMillis() / 1000;
            System.out.println("[Discord] Connected with PID " + ProcessHandle.current().pid());
        }
    }

    private void connect() {
        for (int i = 0; i < 10; i++) {
            String pipeName = "\\\\.\\pipe\\discord-ipc-" + i;
            System.out.println("[Discord] Trying pipe: " + pipeName);
            WinNT.HANDLE h = Kernel32.INSTANCE.CreateFile(
                pipeName,
                WinNT.GENERIC_READ | WinNT.GENERIC_WRITE,
                WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE,
                null,
                WinNT.OPEN_EXISTING,
                WinNT.FILE_ATTRIBUTE_NORMAL,
                null);
            if (WinNT.INVALID_HANDLE_VALUE.equals(h)) {
                int err = Kernel32.INSTANCE.GetLastError();
                System.out.println("[Discord] Pipe " + i + " error: " + err);
                continue;
            }
            System.out.println("[Discord] Opened pipe " + i);
            pipeHandle = h;
            if (sendHandshake()) {
                connected = true;
                startReaderThread();
                return;
            }
            System.out.println("[Discord] Handshake failed on pipe " + i);
            Kernel32.INSTANCE.CloseHandle(h);
            pipeHandle = null;
        }
        if (!connected) System.out.println("[Discord] No Discord IPC pipe found. Is Discord running?");
    }

    private void startReaderThread() {
        running.set(true);
        readerThread = new Thread(() -> {
            while (running.get() && connected && pipeHandle != null) {
                try {
                    readFrame();
                } catch (Exception e) {
                    break;
                }
            }
        }, "discord-ipc-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private boolean sendHandshake() {
        JsonObject hs = new JsonObject();
        hs.addProperty("v", 1);
        hs.addProperty("client_id", config.applicationId);
        String json = hs.toString();
        System.out.println("[Discord] Sending handshake: " + json);
        if (!sendFrame(OP_HANDSHAKE, json)) return false;

        String resp = readFrameSync();
        System.out.println("[Discord] Handshake response: " + (resp != null ? resp : "null"));
        return resp != null;
    }

    private boolean sendFrame(int op, String json) {
        if (pipeHandle == null) return false;
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer header = ByteBuffer.allocate(8);
        header.order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(op);
        header.putInt(payload.length);

        byte[] data = new byte[8 + payload.length];
        System.arraycopy(header.array(), 0, data, 0, 8);
        System.arraycopy(payload, 0, data, 8, payload.length);

        IntByReference written = new IntByReference();
        boolean ok = Kernel32.INSTANCE.WriteFile(pipeHandle, data, data.length, written, null);
        if (!ok) {
            int err = Kernel32.INSTANCE.GetLastError();
            System.out.println("[Discord] WriteFile error: " + err);
        }
        return ok && written.getValue() == data.length;
    }

    private String readFrameSync() {
        return readFrameInternal();
    }

    private void readFrame() {
        readFrameInternal();
    }

    private String readFrameInternal() {
        if (pipeHandle == null) return null;

        byte[] header = new byte[8];
        IntByReference read = new IntByReference();
        boolean ok = Kernel32.INSTANCE.ReadFile(pipeHandle, header, 8, read, null);
        if (!ok || read.getValue() != 8) {
            if (!ok) {
                int err = Kernel32.INSTANCE.GetLastError();
                if (err != 0) System.out.println("[Discord] ReadFile header error: " + err);
            }
            connected = false;
            return null;
        }

        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int op = bb.getInt();
        int len = bb.getInt();

        if (op == OP_CLOSE || len <= 0) {
            if (op == OP_CLOSE) {
                System.out.println("[Discord] Server closed connection.");
                connected = false;
            }
            return null;
        }

        byte[] payload = new byte[len];
        ok = Kernel32.INSTANCE.ReadFile(pipeHandle, payload, len, read, null);
        if (!ok || read.getValue() != len) {
            if (!ok) System.out.println("[Discord] ReadFile payload error: " + Kernel32.INSTANCE.GetLastError());
            connected = false;
            return null;
        }

        String json = new String(payload, StandardCharsets.UTF_8);

        if (op == OP_PING) {
            sendFrame(OP_PONG, json);
            return null;
        }

        return json;
    }

    public void updatePresence(String state, String details) {
        if (!connected) return;

        JsonObject activity = new JsonObject();
        activity.addProperty("state", state);
        activity.addProperty("details", details);

        JsonObject timestamps = new JsonObject();
        timestamps.addProperty("start", startTime);
        activity.add("timestamps", timestamps);

        JsonObject assets = new JsonObject();
        assets.addProperty("large_image", config.largeImageKey);
        assets.addProperty("large_text", config.largeImageText);
        if (!config.smallImageKey.isEmpty()) {
            assets.addProperty("small_image", config.smallImageKey);
            assets.addProperty("small_text", config.smallImageText);
        }
        activity.add("assets", assets);

        JsonArray partyArr = new JsonArray();
        partyArr.add(config.partyMin);
        partyArr.add(config.partyMax);
        JsonObject party = new JsonObject();
        party.add("size", partyArr);
        activity.add("party", party);

        JsonObject args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());
        args.add("activity", activity);

        JsonObject frame = new JsonObject();
        frame.addProperty("cmd", "SET_ACTIVITY");
        frame.add("args", args);
        frame.addProperty("nonce", UUID.randomUUID().toString());

        String json = frame.toString();
        boolean ok = sendFrame(OP_FRAME, json);
        System.out.println("[Discord] Presence update: " + (ok ? "sent" : "FAILED") + " (" + state + ")");
    }

    public void shutdown() {
        running.set(false);
        connected = false;
        if (pipeHandle != null) {
            Kernel32.INSTANCE.CloseHandle(pipeHandle);
            pipeHandle = null;
        }
        System.out.println("[Discord] Disconnected.");
    }

    public boolean isConnected() { return connected; }
}
