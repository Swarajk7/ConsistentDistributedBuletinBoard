package distributed.consistent.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Graph {
    private ArrayList<ArrayList<Integer>> graph;
    private HashMap<Integer, String> contentDictionary;
    public Graph() {
        graph = new ArrayList<>();
        contentDictionary = new HashMap<>();
    }

    public void add_content(int id, String content) {
        contentDictionary.put(id, content);
    }

    public void add_relation(int child, int parent) {
        graph.get(parent).add(child);
    }

    public void dfs() {

    }
}
