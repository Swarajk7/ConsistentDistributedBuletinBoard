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

    private IInterServerCommunication getRMIStub() throws Exception {
        ConfigManager clientManager = ConfigManager.create();

        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + clientManager.getValue(ConfigManager.LEADER_IP_ADDRESS)
                + ":" + clientManager.getValue(ConfigManager.LEADER_PORT_NUMBER) + "/" +
                clientManager.getValue(ConfigManager.LEADER_BINDING_NAME);
        return (IInterServerCommunication) Naming.lookup(serverEndPoint);
    }




    @Override
    public ArrayList<Article> ReadArticle(int id) throws SQLException, ClassNotFoundException, IOException {
        return null;
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
            int quorumReadMemberCount = configManager.getIntegerValue(ConfigManager.QUORUM_READ_MEMBER_COUNT);

            ArrayList<ServerInfo> allReplicaServers = getJoinedServerListFromPrimary();
            ServerInfo maxIdServerInfo = null;
            int maxId = 0;

            while(readQuorum.size() != quorumReadMemberCount){
                ArrayList<ServerInfo> notChosenReplicas = new ArrayList<ServerInfo>();
                for(int i = 0; i < allReplicaServers.size();i++){
                    ServerInfo serverDetails =  allReplicaServers.get(i);
                    if(serverDetails.lock()){
                        readQuorum.add(serverDetails);
                        ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverDetails.getPort()));
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

            System.out.println("Do quorum read from the following server");
            System.out.println(maxIdServerInfo.getIp() + "  " + maxIdServerInfo.getPort() + " " + maxIdServerInfo.getLockStatus());

            ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(maxIdServerInfo.getPort()));


            for(int i = 0; i < readQuorum.size();i++){
                ServerInfo serverDetails =  readQuorum.get(i);

                System.out.println(serverDetails.getIp() + "  " + serverDetails.getPort() + " " + serverDetails.getLockStatus());
                serverDetails.unLock();
            }

            return  repository.ReadArticles(id);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return new Article[0];

    }

    public void WriteArticlesToQuorum(int id) throws SQLException, ClassNotFoundException, IOException {
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
                    if(serverDetails.lock()){
                        //Will this work for DBs in another computer?
                        ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverDetails.getPort()));
                        int currId = repository.findMaxId();
                        ServerInfoWithMaxId serverMaxIdInfo = new ServerInfoWithMaxId(serverDetails.getIp(),
                                serverDetails.getPort(),serverDetails.getBindingname(),currId);

                        writeQuorum.add(serverMaxIdInfo);
                        if(currId > maxId){
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

            //maxIdServerInfo, writeQuorum --> Your function input
            //Call your function here

            for(int i = 0; i < writeQuorum.size();i++){
                ServerInfoWithMaxId serverMaxIdInfo =  writeQuorum.get(i);
                System.out.println(serverMaxIdInfo.getServerInfo().getIp() + "  " + serverMaxIdInfo.getServerInfo().getPort()
                        + " " + serverMaxIdInfo.getServerInfo().getLockStatus());
                serverMaxIdInfo.getServerInfo().unLock();
            }



        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void RequestMainServerForWrite(String content, int parentReplyId, int parentArticleId) throws Exception {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        // initiate write request at main server by calling InitiatePost method.
        // after this message main server will start propagating messages to other servers.
        getRMIStub().InitiatePostArticleAtMainServer(serverInfoRepository.getOwnInfo().getIp(), serverInfoRepository.getOwnInfo().getBindingname(),
                serverInfoRepository.getOwnInfo().getPort(), content, parentReplyId, parentArticleId);
    }

    public ServerInfo RequestMainServerForReadQuorumLeader(int id) throws Exception {

        // initiate write request at main server by calling InitiatePost method.
        // after this message main server will start propagating messages to other servers.
        ServerInfo maxIdServerInfo = getRMIStub().findQuorumLeader();
//        System.out.println("SDSDSDSDSDSDSD");
        System.out.println(maxIdServerInfo.getPort());
        return maxIdServerInfo;

    }

    public ArrayList<ServerInfo> getJoinedServerListFromPrimary() throws Exception{
        return getRMIStub().getConnectedServers();
    }
}
