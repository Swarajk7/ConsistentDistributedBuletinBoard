package distributed.consistent.server.interfaces;

import distributed.consistent.server.ServerInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IInterServerCommunication extends Remote {
    //here we can add logic to make one server as main server
    void joinMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException;

    void leaveMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException;

    void InitiatePostArticleAtMainServer(String rmi_registry_address, String rmi_binding_name,
                                         int portnum, String content, int parentReplyId, int parentArticleId) throws RemoteException;

    public void WriteArticleAtReplica(int id, String content, int parentReplyId, int parentArticleId) throws RemoteException;

    ServerInfo findQuorumLeader() throws RemoteException;

    ArrayList<ServerInfo> getConnectedServers() throws RemoteException;
}
