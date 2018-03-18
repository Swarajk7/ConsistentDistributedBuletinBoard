package distributed.consistent.model;

import java.io.Serializable;

public class Article implements Serializable{
    private int id;
    private String content;
    private int indentCount = 0;
    private int parentreplyid,parentarticleid;

    public Article(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public Article(int id, String content,int indentCount) {
        this.id = id;
        this.content = content;
        this.indentCount = indentCount;
    }

    public Article(int id, String content, int parentreplyid, int parentarticleid) {
        this(id, content);
        this.parentarticleid = parentarticleid;
        this.parentarticleid = parentarticleid;
    }

    public int getID() {
        return this.id;
    }

    public String getContent() {
        return this.content;
    }

    public int getIndentCount() {
        return indentCount;
    }
}
