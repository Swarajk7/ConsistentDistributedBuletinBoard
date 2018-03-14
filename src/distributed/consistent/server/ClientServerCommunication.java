package distributed.consistent.server;

import distributed.consistent.database.ArticleRepository;
import distributed.consistent.model.Article;
import distributed.consistent.server.interfaces.IClientServerCommunication;
import distributed.consistent.server.interfaces.IProtocol;
import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;

public class ClientServerCommunication  extends UnicastRemoteObject implements IClientServerCommunication {
    ClientServerCommunication() throws RemoteException {
        super();
    }

    @Override
    public Article readArticle(int id) throws RemoteException {
        try {
            return getProtocol().ReadArticle(id);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public int postArticle(String content) throws RemoteException {
        System.out.println(content);
        try {
            IProtocol protocol = getProtocol();
            protocol.RequestMainServerForWrite(content);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
        return 0;
    }

    private IProtocol getProtocol() throws IOException {
        //based on configuration create appropriate objects
        return new SequentialProtocol();
    }

    @Override
    public int reply(String content, int parentId) throws RemoteException {
        return 0;
    }

    @Override
    public List<String> getListOfServers() throws RemoteException {
        //client can call this function to get a list of available servers.
        //only the leader will have this information.
        //all other servers will redirect the getList requests to server.
        throw new RemoteException("Will be implemented!");
    }

    @Override
    public List<Article> getListOfArticles() throws RemoteException {
        throw new RemoteException("Will be implemented!");
    }
}
