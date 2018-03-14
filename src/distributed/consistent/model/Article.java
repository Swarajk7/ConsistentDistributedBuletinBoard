package distributed.consistent.model;

import java.io.Serializable;

public class Article implements Serializable{
    private int id;
    private String content;

    public Article(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getID() {
        return this.id;
    }

    public String getContent() {
        return this.content;
    }
}
