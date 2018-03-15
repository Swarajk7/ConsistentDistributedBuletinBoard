package distributed.consistent.server;

import distributed.consistent.Utility;
import distributed.consistent.database.ArticleRepository;
import distributed.consistent.server.interfaces.IInterServerCommunication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class InterServerCommunication extends UnicastRemoteObject implements IInterServerCommunication {
    InterServerCommunication() throws RemoteException {
        super();
    }

    @Override
    public void joinMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException {
        System.out.println(rmi_registry_address + ":" + portnum + "/" + rmi_binding_name);
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            serverInfoRepository.addServerAddress(rmi_registry_address, portnum, rmi_binding_name);
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public void leaveMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException {
        System.out.println(rmi_registry_address + ":" + portnum + "/" + rmi_binding_name);
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            serverInfoRepository.removeServerAddress(rmi_registry_address, portnum, rmi_binding_name);
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    public void InitiatePostArticleAtMainServer(String rmi_registry_address, String rmi_binding_name,
                                                int portnum, String content) throws RemoteException {
        System.out.println(rmi_registry_address + ":" + portnum + "/" + rmi_binding_name + " -- " + content);
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            Utility utility = new Utility();

            ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
            int generatedArticleId = repository.WriteArticleAndGenerateID(content);

            System.out.println(generatedArticleId + " : " + content);

            String INET_ADDR = "224.0.0.3";
            final int PORT = 8888;
            try (DatagramSocket serverSocket = new DatagramSocket()) {
                for (int i = 0; i < 5; i++) {
                    String msg = "Sent message no " + i;
                    // Create a packet that will contain the data
                    // (in the form of bytes) and send it.
                    DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                            msg.getBytes().length, InetAddress.getByName(INET_ADDR), PORT);
                    serverSocket.send(msgPacket);
                    System.out.println("Server sent packet with msg: " + msg);
                    Thread.sleep(500);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //tell other servers to update the data at their side asynchronously
            //by queuing and calling their endpoint synchornously
            //mean while client can poll the original replica for status update
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }
}
