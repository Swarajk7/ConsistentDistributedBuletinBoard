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
        this.parentreplyid = -1;
        this.parentarticleid = -1;
    }

    public Article(int id, String content,int indentCount) {
        this(id, content);
        this.indentCount = indentCount;
    }

    public Article(int id, String content, int parentreplyid, int parentarticleid) {
        this(id, content);
        this.parentreplyid = parentreplyid;
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

    public int getParentreplyid() {
        return parentreplyid;
    }

    public int getParentarticleid() {
        return parentarticleid;
    }
}
