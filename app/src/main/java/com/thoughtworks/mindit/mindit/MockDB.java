package com.thoughtworks.mindit.mindit;


import java.util.HashMap;
import com.google.gson.Gson;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;


public class MockDB {
    private String json;

    public MockDB() {
        json = "";
    }

    public String getData() {
        HashMap<String, Node> nodes = new HashMap<String, Node>();
        Node node = new Node(""+0, "node"+0,null,""+0, 0);
        nodes.put(node.getId(),node);
        for(int i=1; i<10; i++) {
            Node node1 = new Node(""+i, "node"+i, nodes.get(""+(i-1)),""+0, i);
            Node parent=nodes.get(""+(i-1));
            parent.getChildSubTree().add(parent.getChildSubTree().size(),node1.getId());
            nodes.put(node1.getId(), node1);
        }
        Tree tree = new Tree(nodes);
        Gson gson = new Gson();
        json = gson.toJson(tree);
        System.out.println(json);
        return json;
    }

}
