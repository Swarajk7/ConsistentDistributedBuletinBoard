package distributed.consistent.server.interfaces;

import distributed.consistent.model.Article;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientServerCommunication extends Remote {
    Article readArticle(int id) throws RemoteException;
    int postArticle(String content) throws RemoteException;
    int reply(String content, int parentId) throws RemoteException;
}
