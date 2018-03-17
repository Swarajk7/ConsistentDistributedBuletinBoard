package distributed.consistent.database;

import distributed.consistent.model.Article;
import java.util.*;

class GraphNode {
    int id;
    List<GraphNode> childList;
    String content;

    GraphNode(int id) {
        this.id = id;
        this.childList = new ArrayList<>();
    }

    public void addChildArticle(GraphNode article) {
        this.childList.add(article);
    }
}

public class Graph {
    private HashMap<Integer, GraphNode> graph;

    Graph() {
        graph = new HashMap<>();
    }

    public void add_relation(int child, int parent, String contentAtChild) {

        if (!graph.containsKey(child)) {
            graph.put(child, new GraphNode(child));
            graph.get(child).content = contentAtChild;
        }
        if (parent == -1) return;
        if (!graph.containsKey(parent)) {
            graph.put(parent, new GraphNode(parent));
        }
        graph.get(parent).addChildArticle(graph.get(child));
    }

    public ArrayList<Article> dfs(int root) {
        ArrayList<Article> replyList = new ArrayList<>();
        Article article = new Article(graph.get(root).id, graph.get(root).content, 0);
        replyList.add(article);
        for (GraphNode graphNode : graph.get(root).childList) {
            dfs_util(graphNode, replyList, 1);
        }
        return replyList;
    }

    private void dfs_util(GraphNode graphNode, ArrayList<Article> replyList, int level) {
        replyList.add(new Article(graphNode.id, graphNode.content, level));
        for (GraphNode graphNode1 : graphNode.childList) {
            dfs_util(graphNode1, replyList, level + 1);
        }
    }
}
