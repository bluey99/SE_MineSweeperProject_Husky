package multiplayer;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class LanDiscoveryService {

    public static final int DISCOVERY_PORT = 54545;

    private volatile boolean running;

    // Host side: broadcast "MineMates|hostName|difficulty|tcpPort"
    public void startHostingBroadcast(String hostName, String difficulty, int tcpPort) {
        running = true;
        Thread t = new Thread(() -> {
            try (DatagramSocket sock = new DatagramSocket()) {
                sock.setBroadcast(true);

                String msg = "MineMates|" + hostName + "|" + difficulty + "|" + tcpPort;
                byte[] data = msg.getBytes(StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        data, data.length,
                        InetAddress.getByName("255.255.255.255"),
                        DISCOVERY_PORT
                );

                while (running) {
                    sock.send(packet);
                    Thread.sleep(700);
                }
            } catch (Exception ignored) {}
        }, "mp-discovery-broadcast");
        t.setDaemon(true);
        t.start();
    }

    // Join side: listen and report discoveries
    public void startListening(Consumer<DiscoveredHost> onHostFound) {
        running = true;
        Thread t = new Thread(() -> {
            try (DatagramSocket sock = new DatagramSocket(DISCOVERY_PORT)) {
                sock.setSoTimeout(1000);

                byte[] buf = new byte[512];
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        sock.receive(packet);

                        String s = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                        if (!s.startsWith("MineMates|")) continue;

                        String[] parts = s.split("\\|");
                        if (parts.length != 4) continue;

                        String hostName = parts[1];
                        String difficulty = parts[2];
                        int tcpPort = Integer.parseInt(parts[3]);

                        String hostIp = packet.getAddress().getHostAddress();

                        onHostFound.accept(new DiscoveredHost(hostIp, tcpPort, hostName, difficulty));
                    } catch (SocketTimeoutException ignore) {
                        // keep looping
                    }
                }
            } catch (Exception ignored) {}
        }, "mp-discovery-listen");
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        running = false;
    }

    public record DiscoveredHost(String ip, int port, String hostName, String difficulty) {}
}
