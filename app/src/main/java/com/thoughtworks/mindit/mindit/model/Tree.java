package com.thoughtworks.mindit.mindit.model;

import com.thoughtworks.mindit.mindit.PublishSubscribe.IObserver;
import com.thoughtworks.mindit.mindit.PublishSubscribe.ISubject;
import com.thoughtworks.mindit.mindit.exceptions.NodeAlreadyDeletedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observer;

public class Tree implements Serializable, ISubject{
    private static final Object MUTEX = new Object();
    private static Tree instance;
    private HashMap<String, Node> nodes;
    private Node root;
    private List<IObserver> observers;
    private boolean changed = true;

    public Node getLastUpdatedNode() {
        return lastUpdatedNode;
    }

    public void setLastUpdatedNode(Node lastUpdatedNode) {
        this.lastUpdatedNode = lastUpdatedNode;
    }

    private Node lastUpdatedNode;
    private void setRoot() {
        for (String nodeId : this.nodes.keySet()) {
            Node node = this.getNode(nodeId);
            if(node.isARoot()) {
                this.root = node;
                break;
            }
        }
    }

    private boolean isNodeAlreadyDeleted(String nodeId) {
        return (this.getNode(nodeId) == null);
    }

    public boolean isNodeExists(Node node) {
        return this.nodes.containsKey(node.getId());
    }

    private Tree(HashMap<String, Node> nodes) {
        this.nodes = nodes;
        this.setRoot();
        lastUpdatedNode=null;
        observers = new ArrayList<IObserver>();
    }

    public static Tree getInstance(HashMap<String, Node> nodes) {
        if(instance == null){
            instance = new Tree(nodes);
        }
        return instance;
    }

    public Node getRoot() {
        return this.root;
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Tree addNode(Node node) {
        Node parent = this.getNode(node.getParentId());
        int index = parent.getChildSubTree().size();
        lastUpdatedNode = node;
        parent.addThisChild(node, index);
        nodes.put(node.getId(), node);
        this.notifyObservers();
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
        this.notifyObservers();
        return this;
    }

    @Override
    public String toString() {
        return "Tree{" +
                "nodes=" + nodes +
                '}';
    }

    public void fillRootChildSubtree () {
        Node root = this.getRoot();
        ArrayList<String> temp = (ArrayList<String>) root.getLeft().clone();
        temp.addAll(root.getRight());
        root.setChildSubTree(temp);
    }

    public void updateDepthOfAllNodes (Node node, int parentDepth) {
        node.setDepth(parentDepth + 1);
        for (String nodeId : node.getChildSubTree()) {
            updateDepthOfAllNodes(this.getNode(nodeId), node.getDepth());
        }
    }

    public void updatePositionOfAllNodes (Node node, String position) {
        node.setPosition(position);
        if (node.getParentId() == null) {
            for (String leftChild : node.getLeft()) {
                updatePositionOfAllNodes(this.getNode(leftChild), "left");
            }
            for (String rightChild : node.getRight()) {
                updatePositionOfAllNodes(this.getNode(rightChild), "right");
            }
        }
        else {
            for (String child : node.getChildSubTree()) {
                updatePositionOfAllNodes(this.getNode(child), node.getPosition());
            }
        }
    }

    @Override
    public void register(IObserver obj) {
        if(obj == null) throw new NullPointerException("Null Observer");
        synchronized (MUTEX) {
            if(!observers.contains(obj)) observers.add(obj);
        }
    }

    @Override
    public void unregister(IObserver obj) {
        synchronized (MUTEX) {
            observers.remove(obj);
        }
    }

    @Override
    public void notifyObservers() {
        List<IObserver> observersLocal = null;
        //synchronization is used to make sure any observer registered after message is received is not notified
        synchronized (MUTEX) {
            if (!changed)
                return;
            observersLocal = new ArrayList<IObserver>(this.observers);
            this.changed=false;
        }
        for (IObserver obj : observersLocal) {
            this.changed = true;
            obj.update();
        }
    }
}
