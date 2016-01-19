package com.thoughtworks.mindit.mindit;

import java.util.ArrayList;
import com.google.gson.Gson;
import com.thoughtworks.mindit.mindit.model.Node;


public class MockDB {
    private String json;

    public MockDB() {
        json = "";
    }

    public String getData() {
        ArrayList<Node> nodes = new ArrayList<Node>();
        Node node = new Node(""+0, "node"+0,null,""+0, 0);
        nodes.add(node);
        for(int i=1; i<10; i++) {
            Node node1 = new Node(""+i, "node"+i, nodes.get((i-1)),""+0, i);
            Node parent = nodes.get((i-1));
            parent.getChildSubTree().add(parent.getChildSubTree().size(),node1.getId());
            nodes.add(node1);
        }
        Gson gson = new Gson();
        json = gson.toJson(nodes);
        return json;
    }

}
