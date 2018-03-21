package distributed.consistent.server;

import distributed.consistent.database.ArticleRepository;
import distributed.consistent.model.Article;
import distributed.consistent.server.interfaces.IClientServerCommunication;
import distributed.consistent.server.interfaces.IProtocol;


import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientServerCommunication  extends UnicastRemoteObject implements IClientServerCommunication {
    ClientServerCommunication() throws RemoteException {
        super();
    }

    @Override
    public ArrayList<Article> readArticle(int id, int maxidseenyet) throws RemoteException {
        try {
            return getProtocol().ReadArticle(id, maxidseenyet);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public Article[] readArticles(int id, int maxidseenyet) throws RemoteException {
        try {
            return getProtocol().ReadArticles(id, maxidseenyet);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int postArticle(String content, int parentReplyId, int parentArticleId) throws RemoteException {
        System.out.println(content);
        try {
            IProtocol protocol = getProtocol();
            protocol.RequestMainServerForWrite(content, parentReplyId, parentArticleId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
        return 0;
    }

    private IProtocol getProtocol() throws IOException {
        //based on configuration create appropriate objects
        //return new SequentialProtocol();
        //return new QuorumProtocol();
        ConfigManager configManager = ConfigManager.create();
        String protocol = configManager.getValue(ConfigManager.SERVER_PROTOCOL);
        if(protocol.equals("quorum")){
            return new QuorumProtocol();
        }
        else if(protocol.equals("readYourWrite")){
            return new ReadYourWriteProtocol();
        }
        else{
            return new SequentialProtocol();
        }
    }


    public void releaseLocks() throws RemoteException {
        try {
            IProtocol protocol = getProtocol();
            protocol.releaseLocks();
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }


    @Override
    public ArrayList<ServerInfo> getListOfServers() throws RemoteException {
        //client can call this function to get a list of available servers.
        //only the leader will have this information.
        //all other servers will redirect the getList requests to server.
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            return serverInfoRepository.getConnectedServerList();
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }


    }

    @Override
    public List<Article> getListOfArticles() throws RemoteException {
        throw new RemoteException("Will be implemented!");
    }
}
