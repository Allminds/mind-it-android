package com.thoughtworks.mindit.mindit.model;

import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.exception.NodeDoesNotExistException;
import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {
    private String _id;
    private String name;
    private ArrayList<String> childSubTree;
    private String parentId;
    private String rootId;
    private ArrayList<String> left;
    private ArrayList<String> right;
    private int depth;
    private int index;
    private String position;
    private static boolean directionToggler = true;

    public Node(String id, String text, Node parent, String rootId, int index){
        this._id = id;
        this.name = text;
        this.left = new ArrayList<String>();
        this.right = new ArrayList<String>();
        this.childSubTree = new ArrayList<String>();
        this.parentId = (parent != null) ? parent.getId() : null;
        this.rootId = rootId;
        this.depth = (parent != null) ? parent.getDepth()+1 : 0;
        this.index = index;
        this.position = (parent != null) ? ((this.rootId == this.parentId ) ? this.getFirstLevelChildPosition() : parent.getPosition()) : null;
    }

    //Getters-setters start
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public ArrayList<String> getLeft() {
        return left;
    }

    public ArrayList<String> getRight() {
        return right;
    }

    public void setChildSubTree(ArrayList<String> childSubTree) {
        this.childSubTree = childSubTree;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getIndex() {
        return index;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getId() {
        return _id;
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

    public String getRootId() {
        return rootId;
    }

    public int getDepth() {
        return depth;
    }
    //Getters-setters end

    public Node updateParent(Node newParent, Node oldParent, int index) throws Exception {
        oldParent.removeThisChild(this);
        newParent.addThisChild(this, index);
        this.setParentId(newParent.getId());
        return this;
    }

    private String getFirstLevelChildPosition () {
        if(directionToggler) {
            directionToggler = false;
            return "right";
        }
        else {
            directionToggler = true;
            return "left";
        }
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
        if (!this.isChildAlreadyExists(node, siblings)) {
            siblings.add(index, node.getId());
        }
        node.setParentId(this.getId());

        if(this.isARoot()) {
            this.addThisFirstLevelChildToSubTree(node);
        }
        return this;
    }

    private void addThisFirstLevelChildToSubTree(Node node) {
        if (node.getPosition().equals(Constants.POSITION.RIGHT)) {
            ArrayList<String> rightSubTree = this.getRight();
            if (this.isChildAlreadyExists(node, rightSubTree))
                rightSubTree.remove(node.getId());
            rightSubTree.add(rightSubTree.size(), node.getId());
        }
        if (node.getPosition().equals(Constants.POSITION.LEFT)) {
            ArrayList<String> leftSubTree = this.getLeft();
            if (this.isChildAlreadyExists(node, leftSubTree))
                leftSubTree.remove(node.getId());
            leftSubTree.add(leftSubTree.size(), node.getId());
        }
    }

    public Node removeThisChild(Node node) throws NodeDoesNotExistException {
        if (this.isARoot()) {
            this.removeThisFirstLevelChildFromSubtree(node);
        }
        if (this.isChildAlreadyExists(node, this.childSubTree))
            this.getChildSubTree().remove(node.getId());
        else
            throw new NodeDoesNotExistException();
        return node;
    }

    private void removeThisFirstLevelChildFromSubtree(Node node) throws NodeDoesNotExistException {
        if (node.getPosition().equals(Constants.POSITION.RIGHT.toString()) && this.isChildAlreadyExists(node, this.getRight())) {
            this.getRight().remove(node.getId());
        }
        else if (node.getPosition().equals(Constants.POSITION.LEFT.toString()) && this.isChildAlreadyExists(node, this.getLeft())) {
            this.getLeft().remove(node.getId());
        }
        else
            throw new NodeDoesNotExistException();
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
                ", position=" + position +
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

    public void setRight(ArrayList<String> right) {
        this.right = right;
    }

    public void setLeft(ArrayList<String> left) {
        this.left = left;
    }
}