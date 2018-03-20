package distributed.consistent.server;

import java.io.IOException;
import java.net.*;

public class MultiCastHelper {
    private final static String INET_ADDR = "224.0.0.3";
    private final static int PORT = 8888;

    public void send(ServerInfo serverInfo) throws IOException {
        InetAddress addr = InetAddress.getByName(INET_ADDR);
        String sendingMessage = serverInfo.getIp() + ";" + serverInfo.getPort()
                + ";" + serverInfo.getBindingname();
        try (DatagramSocket serverSocket = new DatagramSocket()) {
            DatagramPacket msgPacket = new DatagramPacket(sendingMessage.getBytes(),
                    sendingMessage.getBytes().length, addr, PORT);

            serverSocket.send(msgPacket);
        }
    }

    public ServerInfo receive() throws IOException {
        byte[] buf = new byte[256];

        // Create a new Multicast socket (that will allow other sockets/programs
        // to join it as well.
        try (MulticastSocket clientSocket = new MulticastSocket(PORT)) {
            //Joint the Multicast group.
            InetAddress address = InetAddress.getByName(INET_ADDR);
            clientSocket.joinGroup(address);

            // Receive the information and print it.
            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
            clientSocket.receive(msgPacket);

            String msg = new String(buf, 0, buf.length);
            msg = msg.trim();
            System.out.println("Socket 1 received msg: " + msg);

            String[] split = msg.split(";", -1);
            return new ServerInfo(split[0], Integer.parseInt(split[1]), split[2]);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
