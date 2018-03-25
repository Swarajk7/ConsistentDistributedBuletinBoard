package distributed.consistent.server;

import distributed.consistent.Utility;
import distributed.consistent.database.ArticleRepository;
import distributed.consistent.model.Article;
import distributed.consistent.server.interfaces.IInterServerCommunication;
import distributed.consistent.server.interfaces.IProtocol;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReadYourWriteProtocol implements IProtocol {
    @Override
    public int RequestMainServerForWrite(String content, int parentReplyId, int parentArticleId) throws Exception {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        ArticleRepository repository = new ArticleRepository(new Utility().getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
        if (!serverInfoRepository.isLeader()) {
            IInterServerCommunication stub = getRMIStubFromServerInfo(serverInfoRepository.getLeaderInfo());
            boolean success = false;
            while (!success) {
                // if current leader is not the leader, it will be fixed during multicasting by last leader.
                // wait for few secs
                success = stub.ChangeLeaderMulticast(serverInfoRepository.getOwnInfo(),
                        repository.findMaxId());
                Thread.sleep(500);
            }
            serverInfoRepository.setLeaderInfo(serverInfoRepository.getOwnInfo());
            serverInfoRepository.setIsLeader(true);
        }
        return repository.WriteArticleAndGenerateID(content, parentReplyId, parentArticleId);
    }

    @Override
    public ArrayList<Article> ReadArticle(int id) throws SQLException, ClassNotFoundException, IOException {
        return new SequentialProtocol().ReadArticle(id);
    }

    @Override
    public Article[] ReadArticles(int id) throws SQLException, ClassNotFoundException, IOException {
        return new SequentialProtocol().ReadArticles(id);
    }

    @Override
    public void releaseLocks() throws Exception {

    }

    @Override
    public ArrayList<Article> ReadArticle(int id, int maxidseentilltime) throws Exception {
        UpdateServerWithRecentDataIfOutOfSync(maxidseentilltime);
        return ReadArticle(id);
    }

    @Override
    public Article[] ReadArticles(int id, int maxidseentilltime) throws Exception {
        UpdateServerWithRecentDataIfOutOfSync(maxidseentilltime);
        return ReadArticles(id);
    }

    private IInterServerCommunication getRMIStubFromServerInfo(ServerInfo serverInfo) throws RemoteException, NotBoundException, MalformedURLException {
        String serverEndPoint = "rmi://" + serverInfo.getIp()
                + ":" + serverInfo.getPort() + "/" +
                serverInfo.getBindingname();
        return (IInterServerCommunication) Naming.lookup(serverEndPoint);
    }

    private void UpdateServerWithRecentDataIfOutOfSync(int maxidseentilltime) throws Exception {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        if(serverInfoRepository.isLeader()) return;
        Utility utility = new Utility();
        ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
        // find maximum id from the database
        // make sure this maximum id is equal to the maxidseen by the client till time
        int maxindatabase = repository.findMaxId();
        if (maxidseentilltime > maxindatabase) {
            // sync with the current leader and insert into the database
            IInterServerCommunication stub = getRMIStubFromServerInfo(serverInfoRepository.getLeaderInfo());
            ArrayList<Article> articles = stub.GetDeltaArticles(maxindatabase);
            repository.WriteArticles(articles);
        }
    }
}
