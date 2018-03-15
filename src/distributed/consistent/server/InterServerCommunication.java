package distributed.consistent.server;

import distributed.consistent.Utility;
import distributed.consistent.database.ArticleRepository;
import distributed.consistent.server.interfaces.IInterServerCommunication;
import distributed.consistent.server.threads.CallReplicaServerThread;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;

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

            // Generate multiple threads and call WriteArticleAtReplica function
            ConfigManager configManager = ConfigManager.create();
            int numer_of_publisher_threads = configManager.getIntegerValue(ConfigManager.NUMBER_OF_PUBLISH_THREADS);

            ArrayList<ServerInfo> allReplicaServers = serverInfoRepository.getConnectedServerList();

            int index = 0;
            while (index < allReplicaServers.size()) {
                CallReplicaServerThread[] threads = new CallReplicaServerThread[numer_of_publisher_threads];
                for (int i = 0; i < numer_of_publisher_threads && index < allReplicaServers.size(); i++) {
                    threads[i] = new CallReplicaServerThread(allReplicaServers.get(index), generatedArticleId, content);
                    threads[i].start();
                    i++;
                    index++;
                }
                for (int i = 0; i < numer_of_publisher_threads; i++) if (threads[i] != null) threads[i].join();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public void WriteArticleAtReplica(int id, String content, int parentid) throws RemoteException {
        System.out.println(id + " -- " + content + " -- " + parentid);
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            if (serverInfoRepository.isLeader()) throw new RemoteException("Not supported for leader!");

            Utility utility = new Utility();
            ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
            repository.WriteArticle(id, content, parentid);
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }
}
