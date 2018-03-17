package distributed.consistent.server;

import distributed.consistent.model.Article;
import distributed.consistent.server.interfaces.IProtocol;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class QuorumProtocol implements IProtocol {

    @Override
    public void RequestMainServerForWrite(String content, int parentReplyId, int parentArticleId) throws Exception {

    }

    @Override
    public ArrayList<Article> ReadArticle(int id) throws SQLException, ClassNotFoundException, IOException {
        return null;
    }

    @Override
    public Article[] ReadArticles(int id) throws SQLException, ClassNotFoundException, IOException {
        return new Article[0];
    }
}
