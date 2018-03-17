package distributed.consistent.server.interfaces;

import distributed.consistent.model.Article;

import java.io.IOException;
import java.sql.SQLException;

public interface IProtocol {
    void RequestMainServerForWrite(String content, int parentReplyId, int parentArticleId) throws Exception;
    Article ReadArticle(int id) throws SQLException, ClassNotFoundException, IOException;
    Article[] ReadArticles(int id) throws SQLException, ClassNotFoundException, IOException;
}
