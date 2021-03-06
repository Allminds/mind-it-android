package com.thoughtworks.mindit.model;

import com.thoughtworks.mindit.PublishSubscribe.IObserver;
import com.thoughtworks.mindit.PublishSubscribe.ISubject;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.Fields;
import com.thoughtworks.mindit.constant.UpdateOption;
import com.thoughtworks.mindit.exception.NodeAlreadyDeletedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tree implements Serializable, ISubject {
    private static Tree instance;
    private HashMap<String, Node> nodes;
    private Node root;
    private List<IObserver> observers;
    private String updateOption;
    private Node lastUpdatedNode;
    private String updateParameter;

    private Tree(HashMap<String, Node> nodes) {
        this.nodes = nodes;
        this.setRoot();
        lastUpdatedNode = null;
        observers = new ArrayList<>();
        updateParameter = Fields.CHILD_SUBTREE;
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

    public boolean isAlreadyExists(Node newNode) {
        boolean flag = false;
        Node node = this.getNode(newNode.getId());
        if (node != null) {
            flag = true;
            if (!node.getName().equals(newNode.getName())) {
                updateNode(node, Fields.NAME, newNode.getName());
            }
        }
        return flag;
    }

    public void fillRootChildSubtree() {
        Node root = this.getRoot();
        ArrayList<String> temp = (ArrayList<String>) root.getRight().clone();
        temp.addAll(root.getLeft());
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
                updatePositionOfAllNodes(this.getNode(leftChild), Fields.LEFT);
            }
            for (String rightChild : node.getRight()) {
                updatePositionOfAllNodes(this.getNode(rightChild), Fields.RIGHT);
            }
        } else {
            for (String child : node.getChildSubTree()) {
                updatePositionOfAllNodes(this.getNode(child), node.getPosition());
            }
        }
    }

    public Tree addNode(Node node) {
        lastUpdatedNode = node;
        updateOption = UpdateOption.ADD;

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
        updateOption = UpdateOption.ADD;
        //if(!nodes.containsKey(node.getId()))
        nodes.put(node.getId(), node);
        Node parent = this.getNode(node.getParentId());
        node.setDepth(parent.getDepth() + 1);

        this.notifyObservers();
        return this;
    }

    public Tree updateNode(Node node, String attribute, Object data) {
        updateOption = UpdateOption.UPDATE;
        updateParameter = attribute;
        switch (attribute) {
            case Fields.NAME:
                node.setName((String) data);
                break;
            case Fields.CHILD_SUBTREE:
                node.setChildSubTree((ArrayList<String>) data);
                break;
            case Fields.LEFT:
                node.setLeft((ArrayList<String>) data);
                this.fillRootChildSubtree();
                break;
            case Fields.RIGHT:
                node.setRight((ArrayList<String>) data);
                this.fillRootChildSubtree();
                break;
            case Fields.PARENT_ID:
                node.setParentId((String) data);
                Node parent = this.getNode(node.getParentId());
                if (parent != null)
                    updateDepthOfAllNodes(node, parent.getDepth());
                break;
        }

        lastUpdatedNode = node;
        this.notifyObservers();
        return this;
    }

    public Tree deleteNode(Node node) throws Exception {
        updateOption = UpdateOption.DELETE;

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
        if (obj == null) throw new NullPointerException(Constants.EXCEPTION_NULL_OBSERVER);
        if (!observers.contains(obj)) observers.add(obj);
    }

    @Override
    public void unregister(IObserver obj) {
        observers.remove(obj);
    }

    @Override
    public void notifyObservers() {
        List<IObserver> observersLocal;
        observersLocal = new ArrayList<>(this.observers);
        for (IObserver obj : observersLocal) {
            obj.update(updateOption, updateParameter);
        }
    }

    public void removeAllNodes() {
        nodes.clear();
        instance = null;
        nodes = null;
        lastUpdatedNode = null;
        root = null;
    }

    public static void reset() {
        instance = null;
    }
}
