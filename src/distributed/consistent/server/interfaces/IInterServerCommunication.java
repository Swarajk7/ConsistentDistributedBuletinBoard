package distributed.consistent.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IInterServerCommunication extends Remote {
    //here we can add logic to make one server as main server
    public void joinMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException;

    public void leaveMainServer(String rmi_registry_address, String rmi_binding_name, int portnum) throws RemoteException;

    public void PostArticleAtMainServer(String content) throws RemoteException;
}
