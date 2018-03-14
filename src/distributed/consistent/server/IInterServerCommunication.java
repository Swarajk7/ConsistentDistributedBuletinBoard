package distributed.consistent.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IInterServerCommunication extends Remote {
    //here we can add logic to make one server as main server
    void joinMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException;

    void leaveMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException;

    void PostArticleAtMainServer(String rmi_registry_address, String rmi_binding_name, int portnum, String content) throws RemoteException;
}
