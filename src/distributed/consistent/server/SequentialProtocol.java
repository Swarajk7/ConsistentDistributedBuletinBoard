package distributed.consistent.server;

import distributed.consistent.Utility;
import distributed.consistent.database.ArticleRepository;
import distributed.consistent.model.Article;
import distributed.consistent.server.interfaces.IInterServerCommunication;
import distributed.consistent.server.interfaces.IProtocol;

import java.io.IOException;
import java.rmi.Naming;
import java.sql.SQLException;
import java.util.ArrayList;

public class SequentialProtocol implements IProtocol {
    private ArticleRepository articleRepository;

    SequentialProtocol() throws IOException {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        Utility utility = new Utility();
        String dbpath = utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort());
        //db is already created in server startup. so no need to worry about that.
        articleRepository = new ArticleRepository(dbpath);
    }

    private IInterServerCommunication getRMIStub() throws Exception {
        Thread.sleep(1000);
        ConfigManager clientManager = ConfigManager.create();

        // get stub for calling InterServer RMI functions.
        String serverEndPoint = "rmi://" + clientManager.getValue(ConfigManager.LEADER_IP_ADDRESS)
                + ":" + clientManager.getValue(ConfigManager.LEADER_PORT_NUMBER) + "/" +
                clientManager.getValue(ConfigManager.LEADER_BINDING_NAME);
        return (IInterServerCommunication) Naming.lookup(serverEndPoint);
    }

    public int RequestMainServerForWrite(String content, int parentReplyId, int parentArticleId) throws Exception {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        // initiate write request at main server by calling InitiatePost method.
        // after this message main server will start propagating messages to other servers.
        getRMIStub().InitiatePostArticleAtMainServer(serverInfoRepository.getOwnInfo().getIp(),
                serverInfoRepository.getOwnInfo().getBindingname(),
                serverInfoRepository.getOwnInfo().getPort(), content, parentReplyId, parentArticleId);
        return 0;
    }


    @Override
    public ArrayList<Article> ReadArticle(int id) throws SQLException, ClassNotFoundException, IOException {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        Utility utility = new Utility();

        // Read article from own database using current article id.
        // hoping that article would already have been propagated.
        ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
        return repository.ReadArticle(id);
    }

    @Override
    public Article[] ReadArticles(int id) throws SQLException, ClassNotFoundException, IOException {
        ServerInfoRepository serverInfoRepository = ServerInfoRepository.create();
        Utility utility = new Utility();

        // Read article from own database using current article id.
        // hoping that article would already have been propagated.
        ArticleRepository repository = new ArticleRepository(utility.getDatabaseName(serverInfoRepository.getOwnInfo().getPort()));
        return repository.ReadArticles(id);
    }

    public void releaseLocks() throws Exception {

    }

    @Override
    public ArrayList<Article> ReadArticle(int id, int maxidseentilltime) throws SQLException, ClassNotFoundException, IOException {
        return ReadArticle(id);
    }

    @Override
    public Article[] ReadArticles(int id, int maxidseentilltime) throws SQLException, ClassNotFoundException, IOException {
        return ReadArticles(id);
    }
}
