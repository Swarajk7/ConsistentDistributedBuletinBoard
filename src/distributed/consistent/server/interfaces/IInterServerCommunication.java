package distributed.consistent.server.interfaces;

import distributed.consistent.database.ArticleRepository;
import distributed.consistent.model.Article;
import distributed.consistent.model.ServerInfoWithMaxId;
import distributed.consistent.server.ServerInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IInterServerCommunication extends Remote {
    //here we can add logic to make one server as main server
    void joinMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException;

    void leaveMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException;

    void InitiatePostArticleAtMainServer(String rmi_registry_address, String rmi_binding_name,
                                         int portnum, String content, int parentReplyId, int parentArticleId) throws RemoteException;

    void WriteArticleAtReplica(int id, String content, int parentReplyId, int parentArticleId) throws RemoteException;

    ServerInfo findQuorumLeader() throws RemoteException;

    ArrayList<ServerInfo> getConnectedServers() throws RemoteException;

    void UpdateQuorumMembers(ArrayList<ServerInfoWithMaxId> serverInfoWithMaxIdArrayList) throws RemoteException;

    void InsertBulkForConsistency(ArrayList<Article> articleArrayList) throws RemoteException;

    int findMaxId() throws RemoteException;

    void WriteArticleAtQuorumLeader(String content, int parentReplyId, int parentArticleId) throws RemoteException;

    ArrayList<Article> GetDeltaArticles(int maxidindatabase) throws RemoteException;

    boolean lock()throws Exception;

    void unLock() throws Exception;

    boolean getLockStatus()throws Exception;

    ArticleRepository getRepository(int port) throws Exception;

    boolean ChangeLeaderMulticast(ServerInfo serverInfo, int maxidatnewleader) throws Exception;

}
