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
        System.out.println(String.format("readArticle(articleid = %d)", id));
        try {
            return getProtocol().ReadArticle(id, maxidseenyet);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public Article[] readArticles(int id, int maxidseenyet) throws RemoteException {
        System.out.println(String.format("readArticles(startid = %d)", id));
        try {
            return getProtocol().ReadArticles(id, maxidseenyet);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int postArticle(String content, int parentReplyId, int parentArticleId) throws RemoteException {
        System.out.println(String.format("postArticle(content=%s parent=%d preply=%d)", content, parentArticleId, parentReplyId));
        int returnedid;
        try {
            IProtocol protocol = getProtocol();
            returnedid = protocol.RequestMainServerForWrite(content, parentReplyId, parentArticleId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
        return returnedid;
    }

    private IProtocol getProtocol() throws IOException {
        //based on configuration create appropriate objects
        //return new SequentialProtocol();
        //return new QuorumProtocol();
        ConfigManager configManager = ConfigManager.create();
        String protocol = configManager.getValue(ConfigManager.SERVER_PROTOCOL);
        switch (protocol) {
            case "quorum":
                return new QuorumProtocol();
            case "readYourWrite":
                return new ReadYourWriteProtocol();
            default:
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
        System.out.println("getListOfServers()");
        try {
            ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
            return serverInfoRepository.getConnectedServerList();
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }
}
