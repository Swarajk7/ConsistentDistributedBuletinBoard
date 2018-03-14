package distributed.consistent.server.interfaces;

import distributed.consistent.model.Article;

import java.io.IOException;
import java.sql.SQLException;

public interface IProtocol {
    void RequestMainServerForWrite(String content) throws Exception;

    Article ReadArticle(int id) throws SQLException, ClassNotFoundException, IOException;
}
