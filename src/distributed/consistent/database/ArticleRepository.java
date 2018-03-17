package distributed.consistent.database;

import distributed.consistent.model.Article;
import java.util.*;

import java.sql.*;
public class ArticleRepository {
    private String databaseFilePath;

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        //create tge database if does not exist.
        return DriverManager.getConnection("jdbc:sqlite:" + this.databaseFilePath + ".db");
    }

    public ArticleRepository(String databaseFilePath) {
        this.databaseFilePath = databaseFilePath;
    }

    public void InitiateDatabase() throws ClassNotFoundException, SQLException {
        //reference : https://www.tutorialspoint.com/sqlite/sqlite_java.htm
        Connection connection = this.getConnection();
        String sql = "CREATE TABLE IF NOT EXISTS Article (ID INTEGER NOT NULL,Content VARCHAR(100) NOT NULL,ParentReplyID INT, ParentArticleID INT)";
        Statement stmt = connection.createStatement();
        System.out.println(sql);
        stmt.executeUpdate(sql);
        stmt.close();
        connection.close();
    }

    public ArrayList<Article> ReadArticle(int id) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection();
        String sql = String.format("SELECT ID, Content, ParentReplyID From Article Where ID = %d union select ID," +
                " Content,ParentReplyID From Article Where ParentArticleID = %d ", id, id);
        System.out.println(sql);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        //if no row is present return NULL to signal no data with this ID is present in the data.
        if(!rs.isBeforeFirst()) return null;
        //build graph and do shit here

        Graph graph = new Graph();
        while (rs.next()) {
            int tid = rs.getInt("ID");
            int tparentreplyid = rs.getInt("ParentReplyID");
            String content = rs.getString("Content");
            if (tid == id) tparentreplyid = -1;
            graph.add_relation(tid, tparentreplyid, content);
        }
        ArrayList<Article> articleList = graph.dfs(id);
        stmt.close();
        connection.close();
        return articleList;
    }

    public Article[] ReadArticles(int id) throws SQLException, ClassNotFoundException {
        Connection connection = this.getConnection();
        Article[] articleArray = new Article[8];

        Statement stmt = connection.createStatement();

        String sql = String.format("SELECT ID, Content From Article Where ID >= %d AND ParentArticleID = 'NULL' LIMIT 8", id);

        ResultSet rs = stmt.executeQuery(sql);

        int i = 0;
        while (rs.next()) {
            articleArray[i++] = new Article(rs.getInt("ID"), rs.getString("Content"));
        }

        stmt.close();
        connection.close();
        return articleArray;
    }

    public void WriteArticle(int id, String content, int parentReplyId, int parentArticleId) throws SQLException, ClassNotFoundException {
        String parentArticleIDStr = parentArticleId == -1 ? "NULL" : Integer.toString(parentArticleId);
        String parentReplyIDStr = parentReplyId == -1 ? "NULL" : Integer.toString(parentReplyId);
        String sql = String.format("Insert Into ARTICLE (ID, Content, ParentReplyID, ParentArticleID) VALUES(%d,'%s','%s','%s')",
                id, content, parentReplyIDStr, parentArticleIDStr);
        Connection connection = this.getConnection();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
    }

    public int findMaxId()throws Exception {
        Connection connection = this.getConnection();
        connection.setAutoCommit(false);
        Statement stmt = connection.createStatement();
        int maxId = 0;
        try{
        String getMaxIdSql = "Select max(id) as ID from Article";
        ResultSet rs = stmt.executeQuery(getMaxIdSql);
        maxId = rs.getInt("ID");
        } catch (Exception ex) {
        ex.printStackTrace();
        } finally {
        stmt.close();
        connection.close();
        }
        return maxId;
    }

    public int WriteArticleAndGenerateID(String content, int parentReplyId, int parentArticleId) throws Exception {
        String parentArticleIDStr = parentArticleId == -1 ? "NULL" : Integer.toString(parentArticleId);
        String parentReplyIDStr = parentReplyId == -1 ? "NULL" : Integer.toString(parentReplyId);

        Connection connection = this.getConnection();
        connection.setAutoCommit(false);
        Statement stmt = connection.createStatement();
        int nextid;

        try {

            String getMaxIdSql = "Select max(id) as ID from Article";
            ResultSet rs = stmt.executeQuery(getMaxIdSql);
            int maxid = rs.getInt("ID");
            nextid = maxid + 1;

            String sql = String.format("Insert Into ARTICLE (ID, Content, ParentReplyID, ParentArticleID) VALUES(%d,'%s','%s','%s')",
                    nextid, content, parentReplyIDStr, parentArticleIDStr);

            System.out.println(sql);
            stmt.executeUpdate(sql);

            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
            stmt.close();
            connection.close();
        }
        return nextid;
    }
}
