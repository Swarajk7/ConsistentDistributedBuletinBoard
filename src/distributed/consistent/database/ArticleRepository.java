package distributed.consistent.database;

import distributed.consistent.model.Article;
import java.util.*;

import java.sql.*;
public class ArticleRepository {
    private String databaseFilePath;
    private boolean isPrimary;

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        //create tge database if does not exist.
        return DriverManager.getConnection("jdbc:sqlite:" + this.databaseFilePath + ".db");
    }

    public ArticleRepository(String databaseFilePath) {
        this.databaseFilePath = databaseFilePath;
    }

    public void InitiateDatabase(boolean isPrimary) throws ClassNotFoundException, SQLException {
        //reference : https://www.tutorialspoint.com/sqlite/sqlite_java.htm
        Connection connection = this.getConnection();
        //add a self foreign key for better constraint checking
        this.isPrimary = isPrimary;
        String primaryString = "";
        if (isPrimary) primaryString = "PRIMARY KEY AUTOINCREMENT";
        String sql = String.format("CREATE TABLE IF NOT EXISTS Article (ID INTEGER %s NOT NULL,Content VARCHAR(100) NOT NULL,ParentID INT)", primaryString);
        Statement stmt = connection.createStatement();
        System.out.println(sql);
        stmt.executeUpdate(sql);
        stmt.close();
        connection.close();
    }
    public Article ReadArticle(int id) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection();
        String sql = String.format("SELECT ID, Content From Article Where ID = %d", id);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        //if no row is present return NULL to signal no data with this ID is present in the data.
        Article article = null;
        if (rs.isBeforeFirst()) {
            article = new Article(rs.getInt("ID"), rs.getString("Content"));
        }
        stmt.close();
        connection.close();
        return article;
    }

    public Article[] ReadArticles(int id) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection();
        Article[] articleArray = new Article[8];

        Statement stmt = connection.createStatement();
        for (int i = 0; i < 8; i++){
            String sql = String.format("SELECT ID, Content From Article Where ID = %d", id+i);

            ResultSet rs = stmt.executeQuery(sql);
            //if no row is present return NULL to signal no data with this ID is present in the data.
            Article article = null;
            if (rs.isBeforeFirst()) {
                article = new Article(rs.getInt("ID"), rs.getString("Content"));
            }
            articleArray[i] = article;

        }

        stmt.close();


        connection.close();


        return articleArray;
    }

    public HashMap<Integer,String> Readreplies(int id) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection();

        HashMap<Integer,String> result=new HashMap<Integer,String>();
        

        Statement stmt = connection.createStatement();

        String sql = String.format("SELECT ID, Content From Article Where ParentId = %d", id);

        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            result.put(rs.getInt("ID"), rs.getString("Content"));
        }
        stmt.close();
        connection.close();

        return result;
    }
    public void WriteArticle(int id, String content) throws SQLException, ClassNotFoundException {
        //returns -1 for replica which is not primary.
        //else it returns recently generated article id.
        WriteArticle(id, content, -1);
    }

    public void WriteArticle(int id, String content, int parentId) throws SQLException, ClassNotFoundException {
        String parentIDStr = parentId == -1 ? "NULL" : Integer.toString(parentId);
        String sql = String.format("Insert Into ARTICLE (ID, Content, ParentId) VALUES(%d,'%s','%s')", id, content, parentIDStr);
        Connection connection = this.getConnection();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public int WriteArticleAndGenerateID(String content) throws SQLException, ClassNotFoundException {
        return WriteArticleAndGenerateID(content, -1);
    }

    public int WriteArticleAndGenerateID(String content, int parentId) throws SQLException, ClassNotFoundException {
        String parentIDStr = parentId == -1 ? "NULL" : Integer.toString(parentId);
        String sql = String.format("Insert Into ARTICLE (Content, ParentId) VALUES('%s','%s')", content, parentIDStr);
        System.out.println(sql);
        Connection connection = this.getConnection();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        int newid = -1;
        ResultSet resultSet = stmt.getGeneratedKeys();
        if (resultSet.isBeforeFirst()) newid = resultSet.getInt(1);
        stmt.close();
        connection.close();
        return newid;
    }
}
