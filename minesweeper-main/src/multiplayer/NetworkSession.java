package multiplayer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
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
                while (running && !socket.isClosed()) {
                    Object obj = in.readObject(); // <-- REAL failure usually happens here
                    if (!(obj instanceof MpMessage msg)) continue;
                    onMessage.accept(msg);
                }
            } catch (EOFException | SocketException e) {
                // normal disconnect / socket closed
                if (running) {
                    System.err.println("[NET] Connection closed: " + e);
                    if (onError != null) onError.accept(e);
                }
            } catch (Exception e) {
                // ✅ THIS IS THE IMPORTANT ONE (ClassNotFound / InvalidClass / StreamCorrupted, etc.)
                System.err.println("[NET] Receiver crashed with exception:");
                e.printStackTrace();
                if (running && onError != null) onError.accept(e);
            } finally {
                close();
            }
        }, "mp-receiver");

        t.setDaemon(true);
        t.start();
    }

    public synchronized void send(MpMessage msg) throws IOException {
        if (!running || socket.isClosed()) {
            throw new SocketException("Socket closed");
        }

        out.writeObject(msg);
        out.flush();

        // ✅ Helps avoid ObjectOutputStream caching issues if same objects are resent
        out.reset();
    }

    public void close() {
        running = false;

        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
