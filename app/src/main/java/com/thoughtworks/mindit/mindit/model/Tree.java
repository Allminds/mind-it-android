package com.thoughtworks.mindit.mindit.model;

import com.thoughtworks.mindit.mindit.exceptions.NodeAlreadyDeletedException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Tree {
    private HashMap<String, Node> nodes;
    private Node root;

    private void setRoot() {
        Iterator it = this.nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Node node = (Node)pair.getValue();
            if(node.isARoot()){
                this.root = node;
                break;
            }
        }
    }

    private boolean isNodeAlreadyDeleted(String nodeId) {
        return (this.getNode(nodeId) == null);
    }

    public Node getRoot() {
        return this.root;
    }

    public Tree(HashMap<String, Node> nodes) {
        this.nodes = nodes;
        this.setRoot();
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Tree addNode(Node node, int index) {
        Node parent = this.getNode(node.getParentId());
        if(node.isNotARoot())
            parent.addThisChild(node, index);
        nodes.put(node.getId(), node);
        return this;
    }

    public Tree deleteNode(Node node) throws Exception {
        String nodeId = node.getId();
        if(isNodeAlreadyDeleted(nodeId)) {
            throw new NodeAlreadyDeletedException();
        }
        Node parent = this.getNode(node.getParentId());
        if (node.isNotARoot()) {
            parent.removeThisChild(node);
            nodes.remove(nodeId);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Tree{" +
                "nodes=" + nodes +
                '}';
    }
}
