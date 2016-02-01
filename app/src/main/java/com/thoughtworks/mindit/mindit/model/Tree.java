package com.thoughtworks.mindit.mindit.model;

import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.PublishSubscribe.IObserver;
import com.thoughtworks.mindit.mindit.PublishSubscribe.ISubject;
import com.thoughtworks.mindit.mindit.exception.NodeAlreadyDeletedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tree implements Serializable, ISubject {
    private static Tree instance;
    private HashMap<String, Node> nodes;
    private Node root;
    private List<IObserver> observers;
    private int updateOption;
    private Node lastUpdatedNode;
    private String updateParameter;

    private Tree(HashMap<String, Node> nodes) {
        this.nodes = nodes;
        this.setRoot();
        lastUpdatedNode = null;
        observers = new ArrayList<IObserver>();
        updateParameter = "childSubTree";
    }

    public static Tree getInstance(HashMap<String, Node> nodes) {
        if (instance == null) {
            instance = new Tree(nodes);
        }
        return instance;
    }

    public Node getLastUpdatedNode() {
        return lastUpdatedNode;
    }

    private void setRoot() {
        for (String nodeId : this.nodes.keySet()) {
            Node node = this.getNode(nodeId);
            if (node.isARoot()) {
                this.root = node;
                break;
            }
        }
    }

    public Node getRoot() {
        return this.root;
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    private boolean isNodeAlreadyDeleted(String nodeId) {
        return (this.getNode(nodeId) == null);
    }

    public boolean isAlreadyExists(Node node) {
        return (this.getNode(node.getId()) != null);
    }

    public void fillRootChildSubtree() {
        Node root = this.getRoot();
        ArrayList<String> temp = (ArrayList<String>) root.getLeft().clone();
        temp.addAll(root.getRight());
        root.setChildSubTree(temp);
    }

    public void updateDepthOfAllNodes(Node node, int parentDepth) {
        node.setDepth(parentDepth + 1);
        for (String nodeId : node.getChildSubTree()) {
            updateDepthOfAllNodes(this.getNode(nodeId), node.getDepth());
        }
    }

    public void updatePositionOfAllNodes(Node node, String position) {
        node.setPosition(position);
        if (node.isARoot()) {
            for (String leftChild : node.getLeft()) {
                updatePositionOfAllNodes(this.getNode(leftChild), "left");
            }
            for (String rightChild : node.getRight()) {
                updatePositionOfAllNodes(this.getNode(rightChild), "right");
            }
        } else {
            for (String child : node.getChildSubTree()) {
                updatePositionOfAllNodes(this.getNode(child), node.getPosition());
            }
        }
    }

    public Tree addNode(Node node) {
        lastUpdatedNode = node;
        updateOption = Constants.TREE_UPDATE_OPTIONS.ADD.getValue();

        Node parent = this.getNode(node.getParentId());
        node.setDepth(parent.getDepth() + 1);
        int index = parent.getChildSubTree().size();
        parent.addThisChild(node, index);

        nodes.put(node.getId(), node);

        this.notifyObservers();
        return this;
    }

    public Tree addNodeFromWeb(Node node) {
        lastUpdatedNode = node;
        updateOption = Constants.TREE_UPDATE_OPTIONS.ADD.getValue();
        //if(!nodes.containsKey(node.getId()))
             nodes.put(node.getId(), node);

        Node parent = this.getNode(node.getParentId());
        node.setDepth(parent.getDepth() + 1);

        this.notifyObservers();
        return this;
    }

    public Tree updateNode(Node node, String attribute, Object data) {
        updateOption = Constants.TREE_UPDATE_OPTIONS.UPDATE.getValue();
        updateParameter = attribute;
        switch (attribute) {
            case "name":
                node.setName((String) data);
                break;
            case "childSubTree":
                node.setChildSubTree((ArrayList<String>) data);
                break;
            case "left":
                node.setLeft((ArrayList<String>) data);
                this.fillRootChildSubtree();
                break;
            case "right":
                node.setRight((ArrayList<String>) data);
                this.fillRootChildSubtree();
                break;
            case "parentId":
                node.setParentId((String) data);
                Node parent = this.getNode(node.getParentId());
                updateDepthOfAllNodes(node, parent.getDepth());
                break;
        }

        lastUpdatedNode = node;
        this.notifyObservers();
        return this;
    }

    public Tree deleteNode(Node node) throws Exception {
        updateOption = Constants.TREE_UPDATE_OPTIONS.DELETE.getValue();

        String nodeId = node.getId();
        if (isNodeAlreadyDeleted(nodeId)) {
            throw new NodeAlreadyDeletedException();
        }

        Node parent = this.getNode(node.getParentId());
        if (node.isNotARoot()) {
            parent.removeThisChild(node);
            nodes.remove(nodeId);
        }

        this.notifyObservers();
        return this;
    }
    @Override
    public String toString() {
        return "Tree{" +
                "nodes=" + nodes +
                '}';
    }

    @Override
    public void register(IObserver obj) {
        if (obj == null) throw new NullPointerException("Null Observer");
        if (!observers.contains(obj)) observers.add(obj);
    }

    @Override
    public void unregister(IObserver obj) {
        observers.remove(obj);
    }

    @Override
    public void notifyObservers() {
        List<IObserver> observersLocal = null;
        observersLocal = new ArrayList<IObserver>(this.observers);
        for (IObserver obj : observersLocal) {
            obj.update(updateOption, updateParameter);
        }
    }

    public void removeAllNodes() {
        nodes.clear();
        instance = null;
    }
}
