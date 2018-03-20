package distributed.consistent.server;

import distributed.consistent.Utility;
import distributed.consistent.database.ArticleRepository;
import distributed.consistent.model.Article;
import distributed.consistent.model.ServerInfoWithMaxId;
import distributed.consistent.server.interfaces.IInterServerCommunication;
import distributed.consistent.server.interfaces.IProtocol;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

public class QuorumProtocol implements IProtocol {
    private ArticleRepository articleRepository;

    QuorumProtocol() throws IOException {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        Utility utility = new Utility();
        String dbpath = utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort());
        //db is already created in server startup. so no need to worry about that.
        articleRepository = new ArticleRepository(dbpath);
    }

    public void releaseLocks() throws Exception {
        ArrayList<ServerInfo> allReplicaServers = getJoinedServerListFromPrimary();
        for(int i = 0; i < allReplicaServers.size();i++){
            ServerInfo serverDetails =  allReplicaServers.get(i);
            serverDetails.unLock();
        }
    }

    //Gets stub of the primary server
    private IInterServerCommunication getPrimaryRMIStub() throws Exception {
        ConfigManager clientManager = ConfigManager.create();

        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + clientManager.getValue(ConfigManager.LEADER_IP_ADDRESS)
                + ":" + clientManager.getValue(ConfigManager.LEADER_PORT_NUMBER) + "/" +
                clientManager.getValue(ConfigManager.LEADER_BINDING_NAME);
        return (IInterServerCommunication) Naming.lookup(serverEndPoint);
    }


    private IInterServerCommunication getQuorumLeaderRMIStub(ServerInfo maxIdServerInfo) throws Exception {
        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + maxIdServerInfo.getIp()
                + ":" + maxIdServerInfo.getPort() + "/" +
                maxIdServerInfo.getBindingname();
        System.out.println(serverEndPoint);
        return (IInterServerCommunication) Naming.lookup(serverEndPoint);
    }



    private IInterServerCommunication getRMIStub(ServerInfo maxIdServerInfo) throws Exception {
        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + maxIdServerInfo.getIp()
                + ":" + maxIdServerInfo.getPort() + "/" +
                maxIdServerInfo.getBindingname();
        System.out.println(serverEndPoint);
        return (IInterServerCommunication) Naming.lookup(serverEndPoint);
    }



    @Override
    public ArrayList<Article> ReadArticle(int id) throws SQLException, ClassNotFoundException, IOException {
        Utility utility = new Utility();
        try{
            ArrayList<ServerInfo> readQuorum = new  ArrayList<ServerInfo>();
            ConfigManager configManager = ConfigManager.create();
            //Get the number of quorum read members from the config file
            int quorumReadMemberCount = configManager.getIntegerValue(ConfigManager.QUORUM_READ_MEMBER_COUNT);

            ArrayList<ServerInfo> allReplicaServers = getJoinedServerListFromPrimary();
            ServerInfo maxIdServerInfo = null;
            int maxId = 0;

            while(readQuorum.size() != quorumReadMemberCount){
                ArrayList<ServerInfo> notChosenReplicas = new ArrayList<ServerInfo>();
                for(int i = 0; i < allReplicaServers.size();i++){
                    ServerInfo serverDetails =  allReplicaServers.get(i);
                    IInterServerCommunication serverStub = getRMIStub(serverDetails);
                    if(serverStub.lock()){
                        System.out.println("Locked server " + serverDetails.getIp() + ":" + serverDetails.getPort());
                        readQuorum.add(serverDetails);
                        ArticleRepository repository = serverStub.getRepository(serverDetails.getPort());
                        int currId = repository.findMaxId();
                        if(currId > maxId){
                            maxId = currId;
                            maxIdServerInfo = serverDetails;
                        }
                        if(readQuorum.size() == quorumReadMemberCount){
                            break;
                        }
                    }
                    else{
                        notChosenReplicas.add(serverDetails);
                    }

                }
                allReplicaServers = notChosenReplicas;
            }

            if(maxId == 0){
                releaseQuorumMembers(readQuorum);
                return new ArrayList<Article>();
            }

            IInterServerCommunication serverStub = getRMIStub(maxIdServerInfo);

            //Get the repository object of the server associated with  maxIdServerInfo
            ArticleRepository repository = serverStub.getRepository(maxIdServerInfo.getPort());

            //Release lock on the quorum members
            releaseQuorumMembers(readQuorum);

            return  repository.ReadArticle(id);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return new ArrayList<Article>();

    }

    @Override
    public Article[] ReadArticles(int id) throws SQLException, ClassNotFoundException, IOException {
        return  ReadArticlesfromQuorum(id);
    }


    public Article[] ReadArticlesfromQuorum(int id) throws SQLException, ClassNotFoundException, IOException {
        Utility utility = new Utility();
        try{
            ArrayList<ServerInfo> readQuorum = new  ArrayList<ServerInfo>();
            ConfigManager configManager = ConfigManager.create();
            //Get the number of quorum read members from the config file
            int quorumReadMemberCount = configManager.getIntegerValue(ConfigManager.QUORUM_READ_MEMBER_COUNT);

            ArrayList<ServerInfo> allReplicaServers = getJoinedServerListFromPrimary();
            ServerInfo maxIdServerInfo = null;
            int maxId = 0;

            while(readQuorum.size() != quorumReadMemberCount){
                ArrayList<ServerInfo> notChosenReplicas = new ArrayList<ServerInfo>();
                for(int i = 0; i < allReplicaServers.size();i++){
                    ServerInfo serverDetails =  allReplicaServers.get(i);
                    IInterServerCommunication serverStub = getRMIStub(serverDetails);
                    if(serverStub.lock()){
                        System.out.println("Locked server " + serverDetails.getIp() + ":" + serverDetails.getPort());
                        readQuorum.add(serverDetails);
                        ArticleRepository repository = serverStub.getRepository(serverDetails.getPort());
                        int currId = repository.findMaxId();
                        if(currId > maxId){
                            maxId = currId;
                            maxIdServerInfo = serverDetails;
                        }
                        if(readQuorum.size() == quorumReadMemberCount){
                            break;
                        }
                    }
                    else{
                        notChosenReplicas.add(serverDetails);
                    }

                }
                allReplicaServers = notChosenReplicas;
            }

            if(maxId == 0){
                releaseQuorumMembers(readQuorum);
                return new Article[0];
            }

            IInterServerCommunication serverStub = getRMIStub(maxIdServerInfo);

            //Get the repository object of the server associated with  maxIdServerInfo
            ArticleRepository repository = serverStub.getRepository(maxIdServerInfo.getPort());

            //Release lock on the quorum members
            releaseQuorumMembers(readQuorum);

            return  repository.ReadArticles(id);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return new Article[1];

    }

    public void releaseQuorumMembers(ArrayList<ServerInfo> quorum){
        try{
            for(int i = 0; i < quorum.size();i++){
                ServerInfo serverDetails =  quorum.get(i);
                IInterServerCommunication serverStub = getRMIStub(serverDetails);
                serverStub.unLock();
                System.out.println("Released lock on " + serverDetails.getIp() + "  " + serverDetails.getPort() + " " +
                        serverStub.getLockStatus());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    public void releaseWriteQuorumMembers(ArrayList<ServerInfoWithMaxId> quorum){
        try{
            for(int i = 0; i < quorum.size();i++){
                ServerInfoWithMaxId serverDetails =  quorum.get(i);
                IInterServerCommunication serverStub = getRMIStub(serverDetails.getServerInfo());
                serverStub.unLock();
                System.out.println("Released lock on " + serverDetails.getServerInfo().getIp() + "  " +
                        serverDetails.getServerInfo().getPort() + " " +
                        serverStub.getLockStatus());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void WriteArticlesToQuorum(String content, int parentReplyId, int parentArticleId) throws SQLException, ClassNotFoundException, IOException {
        Utility utility = new Utility();
        try{
            ConfigManager configManager = ConfigManager.create();
            int quorumWriteMemberCount = configManager.getIntegerValue(ConfigManager.QUORUM_WRITE_MEMBER_COUNT);

            if(quorumWriteMemberCount < 1){
                throw new RemoteException("Write Quorum should have atleast 1 server");
            }

            ArrayList<ServerInfo> allReplicaServers = getJoinedServerListFromPrimary();
            ServerInfoWithMaxId maxIdServerInfo = null;
            int maxId = 0;

            ArrayList<ServerInfoWithMaxId> writeQuorum = new ArrayList<ServerInfoWithMaxId>();

            //  Stay in the while loop until  quorumWriteMemberCount servers are locked for write quorum
            while(writeQuorum.size() != quorumWriteMemberCount){
                ArrayList<ServerInfo> notChosenReplicas = new ArrayList<ServerInfo>();
                for(int i = 0; i < allReplicaServers.size();i++){
                    ServerInfo serverDetails =  allReplicaServers.get(i);

                    IInterServerCommunication serverStub = getRMIStub(serverDetails);
                    if(serverStub.lock()){
                        System.out.println("Locked server " + serverDetails.getIp() + ":" + serverDetails.getPort());
                        ArticleRepository repository = serverStub.getRepository(serverDetails.getPort());
                        int currId = repository.findMaxId();
                        ServerInfoWithMaxId serverMaxIdInfo = new ServerInfoWithMaxId(serverDetails.getIp(),
                                serverDetails.getPort(),serverDetails.getBindingname(),currId);

                        writeQuorum.add(serverMaxIdInfo);
                        if(currId >= maxId){
                            maxIdServerInfo = serverMaxIdInfo;
                        }

                        if(writeQuorum.size() == quorumWriteMemberCount){
                            break;
                        }
                    }
                    else{
                        notChosenReplicas.add(serverDetails);
                    }

                }
                allReplicaServers = notChosenReplicas;
            }

            //Remove leader from writeQuorum list so that we have the
            //list of servers to be updated
            writeQuorum.remove(maxIdServerInfo);

            //Get Quorum Leader stub
            IInterServerCommunication quorumLeaderStub = getQuorumLeaderRMIStub(maxIdServerInfo.getServerInfo());
            //Insert new values in quorum leader server.
            //Assumption: System is fault tolerant. In case the system isn't fault tolerant
            //we should first make all the the quorum members consistent and then push in the update
            quorumLeaderStub.WriteArticleAtQuorumLeader(content, parentReplyId, parentArticleId);
            //Update other quorum members
            quorumLeaderStub.UpdateQuorumMembers(writeQuorum);

            //Add leader quorum member to the write quorum
            writeQuorum.add(maxIdServerInfo);

            //Release lock on other quorum members
            releaseWriteQuorumMembers(writeQuorum);

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void RequestMainServerForWrite(String content, int parentReplyId, int parentArticleId) throws Exception {
        WriteArticlesToQuorum(content, parentReplyId,parentArticleId);
    }

//    public ServerInfo RequestMainServerForReadQuorumLeader(int id) throws Exception {
//
//        // initiate write request at main server by calling InitiatePost method.
//        // after this message main server will start propagating messages to other servers.
//        ServerInfo maxIdServerInfo = getRMIStub().findQuorumLeader();
////        System.out.println("SDSDSDSDSDSDSD");
//        System.out.println(maxIdServerInfo.getPort());
//        return maxIdServerInfo;

//    }

    public ArrayList<ServerInfo> getJoinedServerListFromPrimary() throws Exception{
        return getPrimaryRMIStub().getConnectedServers();
    }
}
