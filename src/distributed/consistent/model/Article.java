package distributed.consistent.model;

public class Article {
    int id;
    String content;

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
