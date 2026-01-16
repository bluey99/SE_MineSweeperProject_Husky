package multiplayer;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkSession {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private volatile boolean running = true;

    public NetworkSession(Socket socket) throws IOException {
        this.socket = socket;

        // IMPORTANT: Create ObjectOutputStream first, flush, then ObjectInputStream
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void startReceiverLoop(Consumer<MpMessage> onMessage, Consumer<Exception> onError) {
        Thread t = new Thread(() -> {
            try {
                while (running) {
                    Object obj = in.readObject();
                    if (!(obj instanceof MpMessage msg)) continue;
                    onMessage.accept(msg);
                }
            } catch (Exception e) {
                if (running) onError.accept(e);
            } finally {
                close();
            }
        }, "mp-receiver");
        t.setDaemon(true);
        t.start();
    }

    public synchronized void send(MpMessage msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    public void close() {
        running = false;
        try { socket.close(); } catch (Exception ignored) {}
    }
}
