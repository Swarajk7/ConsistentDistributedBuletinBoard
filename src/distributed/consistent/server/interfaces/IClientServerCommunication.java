package distributed.consistent.server.interfaces;

import distributed.consistent.model.Article;
import distributed.consistent.server.ServerInfo;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IClientServerCommunication extends Remote {
    List<Article> readArticle(int id, int maxidseenyet) throws RemoteException;
    Article[] readArticles(int id,  int maxidseenyet) throws RemoteException;
    int postArticle(String content, int parentReplyId, int parentArticleId) throws RemoteException;
    ArrayList<ServerInfo> getListOfServers() throws RemoteException;
    List<Article> getListOfArticles() throws RemoteException;
    void releaseLocks() throws RemoteException;
}
