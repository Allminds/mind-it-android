package com.thoughtworks.mindit.mindit.model;

import com.thoughtworks.mindit.mindit.exceptions.NodeDoesNotExistException;

import java.io.Serializable;
import java.util.ArrayList;


public class Node implements Serializable {
    private String _id;
    private String name;

    public ArrayList<String> getLeft() {
        return left;
    }

    public ArrayList<String> getRight() {
        return right;
    }

    private ArrayList<String> left;
    private ArrayList<String> right;

    public void setChildSubTree(ArrayList<String> childSubTree) {
        this.childSubTree = childSubTree;
    }

    private ArrayList<String> childSubTree;
    private String parentId;
    private String rootId;

    public void setDepth(int depth) {
        this.depth = depth;
    }

    private int depth;

    public int getIndex() {
        return index;
    }

    private int index;

    public void set_id(String _id) {
        this._id = _id;
    }

    public Node(String id,String text,Node parent,String rootId, int index){
        this._id=id;
        this.name =text;
        this.left = new ArrayList<String>();
        this.right = new ArrayList<String>();
        this.childSubTree = new ArrayList<String>();
        this.parentId = (parent != null) ? parent.getId() : null;
        this.rootId = rootId;
        this.depth=(parent != null) ? parent.getDepth()+1 : 0;
        this.index=index;
    }

    private boolean isChildAlreadyExists(Node node, ArrayList<String> siblings) {
        return siblings.contains(node.getId());
    }

    public boolean isNotARoot() {
        return this.getParentId() != null;
    }
    public boolean isARoot() {
        return this.getParentId() == null;
    }

    public Node addThisChild(Node node, int index) {
        ArrayList<String> siblings = this.getChildSubTree();
        if (this.isChildAlreadyExists(node, siblings)) {
            siblings.remove(node.getId());
        }
        siblings.add(index, node.getId());
        node.setParentId(this.getId());
        return this;
    }

    public Node removeThisChild(Node node) throws NodeDoesNotExistException {
        if (this.isChildAlreadyExists(node,this.childSubTree))
            this.getChildSubTree().remove(node.getId());
        else
            throw new NodeDoesNotExistException();
        return node;
    }

    public Node updateParent(Node newParent, Node oldParent, int index) throws Exception {
        oldParent.removeThisChild(this);
        newParent.addThisChild(this, index);
        this.setParentId(newParent.getId());
        return this;
    }

    public String getId() {
        return _id;
    }

    private void setId(String id) {
        this._id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getChildSubTree() {
        return childSubTree;
    }

    public String getParentId() {
        return parentId;
    }

    private void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRootId() {
        return rootId;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", left='" + left + '\'' +
                ", right='" + right + '\'' +
                ", childSubTree=" + childSubTree +
                ", parentId='" + parentId + '\'' +
                ", rootId='" + rootId + '\'' +
                ", depth=" + depth +
                ", index=" + index +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return getId().equals(node.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}