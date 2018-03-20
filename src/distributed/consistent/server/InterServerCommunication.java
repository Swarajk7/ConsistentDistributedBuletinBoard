package distributed.consistent.server;

import distributed.consistent.Utility;
import distributed.consistent.database.ArticleRepository;
import distributed.consistent.model.Article;
import distributed.consistent.model.ServerInfoWithMaxId;
import distributed.consistent.server.interfaces.IInterServerCommunication;
import distributed.consistent.server.threads.CallReplicaServerThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
                                                int portnum, String content, int parentReplyId, int parentArticleId) throws RemoteException {
        System.out.println(rmi_registry_address + ":" + portnum + "/" + rmi_binding_name + " -- " + content);
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            Utility utility = new Utility();

            ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
            int generatedArticleId = repository.WriteArticleAndGenerateID(content, parentReplyId, parentArticleId);

            System.out.println(generatedArticleId + " : " + content);

            // Generate multiple threads and call WriteArticleAtReplica function
            ConfigManager configManager = ConfigManager.create();
            int numer_of_publisher_threads = configManager.getIntegerValue(ConfigManager.NUMBER_OF_PUBLISH_THREADS);

            ArrayList<ServerInfo> allReplicaServers = serverInfoRepository.getConnectedServerList();

            int index = 0;
            while (index < allReplicaServers.size()) {
                CallReplicaServerThread[] threads = new CallReplicaServerThread[numer_of_publisher_threads];
                for (int i = 0; i < numer_of_publisher_threads && index < allReplicaServers.size(); i++) {
                    threads[i] = new CallReplicaServerThread(allReplicaServers.get(index), generatedArticleId, content, parentReplyId, parentArticleId);
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
    public void WriteArticleAtQuorumLeader(String content, int parentReplyId, int parentArticleId)throws RemoteException {
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            Utility utility = new Utility();

            ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
            int generatedArticleId = repository.WriteArticleAndGenerateID(content, parentReplyId, parentArticleId);

            System.out.println(generatedArticleId + " : " + content);
        }catch(Exception ex){
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public boolean getLockStatus()throws Exception{
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            return serverInfoRepository.getLockStatus();
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public ArticleRepository getRepository(int port) throws Exception{
        try{
            Utility utility = new Utility();
            ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(port));
            return repository;
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }

    }


    @Override
    public void unLock() throws Exception{
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            serverInfoRepository.unLock();
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public boolean lock()throws Exception {
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            if(!serverInfoRepository.getLockStatus()){
                serverInfoRepository.lock();
                return true;
            }
            else
                return false;
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public ArrayList<Article> GetDeltaArticles(int maxidindatabase) throws RemoteException {
        ArrayList<Article> articlesToUpdate = null;
        try {
            Utility utility = new Utility();
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            ArticleRepository articleRepository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));

            articlesToUpdate = articleRepository.GetDeltaArticles(maxidindatabase);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        }
        return articlesToUpdate;
    }

    @Override
    public void WriteArticleAtReplica(int id, String content, int parentReplyId, int parentArticleId) throws RemoteException {
        System.out.println(id + " -- " + content + " -- " + parentReplyId + " - " + parentArticleId);
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            if (serverInfoRepository.isLeader()) throw new RemoteException("Not supported for leader!");

            Utility utility = new Utility();
            ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
            repository.WriteArticle(id, content, parentReplyId, parentArticleId);
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }


    public ServerInfo findQuorumLeader() throws RemoteException {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        Utility utility = new Utility();
        ArrayList<ServerInfo> allReplicaServers = serverInfoRepository.getConnectedServerList();
        int maxId = 0;
        ServerInfo maxIdServerInfo = serverInfoRepository.getOwnInfo();
        try {
            ConfigManager configManager = ConfigManager.create();
            int quorumReadMemberCount = configManager.getIntegerValue(ConfigManager.QUORUM_READ_MEMBER_COUNT);

            //  If required number of read quorum members aren't there, then throw exception
            if (allReplicaServers.size() < quorumReadMemberCount)
                throw new RemoteException("Not enough Read Quorum Servers");

            for (int i = 0; i < quorumReadMemberCount; i++) {
                ServerInfo serverDetails = allReplicaServers.get(i);

                ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverDetails.getPort()));
                int currId = repository.findMaxId();
                if (currId > maxId) {
                    maxId = currId;
                    maxIdServerInfo = serverDetails;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteException(ex.getMessage());
        }

        return maxIdServerInfo;
    }

    public ArrayList<ServerInfo> getConnectedServers() {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        return serverInfoRepository.getConnectedServerList();
    }

    @Override
    public void UpdateQuorumMembers(ArrayList<ServerInfoWithMaxId> serverInfoWithMaxIdArrayList) throws RemoteException {
        System.out.println("UpdateQuorumMembers(): Count -> " + serverInfoWithMaxIdArrayList.size());
        try {
            Utility utility = new Utility();

            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            ArticleRepository articleRepository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));

            // Use these articles to update other servers in server
            for (ServerInfoWithMaxId serverInfoWithMaxId : serverInfoWithMaxIdArrayList) {
                // call other server endpoint and pass articlesToUpdate list.
                // select appropriate messages from the list to filter
                // we can make below calls in parallel using multiple thread
                ArrayList<Article> articlesToUpdate = articleRepository.GetDeltaArticles(serverInfoWithMaxId.getMaximum_id());
                if (articlesToUpdate == null || articlesToUpdate.size() == 0) continue;
                String serverEndPoint = "rmi://" + serverInfoWithMaxId.getServerInfo().getIp()
                        + ":" + serverInfoWithMaxId.getServerInfo().getPort() + "/" +
                        serverInfoWithMaxId.getServerInfo().getBindingname();
                IInterServerCommunication stub = (IInterServerCommunication) Naming.lookup(serverEndPoint);
                stub.InsertBulkForConsistency(articlesToUpdate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    public void InsertBulkForConsistency(ArrayList<Article> articleArrayList) throws RemoteException {
        System.out.println("InsertBulkForConsistency(): Count -> " + articleArrayList.size());
        try {
            Utility utility = new Utility();

            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            ArticleRepository articleRepository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));

            articleRepository.WriteArticles(articleArrayList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int findMaxId() throws RemoteException {
        int maxid;
        try {
            Utility utility = new Utility();
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            ArticleRepository articleRepository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));

            maxid = articleRepository.findMaxId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
        System.out.println("Found Max Id = " + maxid);
        return maxid;
    }
}
