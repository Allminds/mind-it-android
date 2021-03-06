package com.thoughtworks.mindit.model;

import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.Fields;
import com.thoughtworks.mindit.exception.NodeDoesNotExistException;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {

    private static boolean directionToggler = true;
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

    public Node(String id, String text, Node parent, String rootId, int index) {
        this._id = id;
        this.name = text;
        this.left = new ArrayList<>();
        this.right = new ArrayList<>();
        this.childSubTree = new ArrayList<>();
        this.parentId = (parent != null) ? parent.getId() : null;
        this.rootId = rootId;
        this.depth = (parent != null) ? parent.getDepth() + 1 : 0;
        this.index = index;
        this.position = (parent != null) ? ((this.rootId.equals(this.parentId) ? this.getFirstLevelChildPosition() : parent.getPosition())) : null;
    }

    public static void setDirectionToggler(boolean directionToggler) {
        Node.directionToggler = directionToggler;
    }

    //Getters-setters start
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public ArrayList<String> getLeft() {
        return left;
    }

    public void setLeft(ArrayList<String> left) {
        this.left = left;
    }

    public ArrayList<String> getRight() {
        return right;
    }

    public void setRight(ArrayList<String> right) {
        this.right = right;
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

    public void setChildSubTree(ArrayList<String> childSubTree) {
        this.childSubTree = childSubTree;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRootId() {
        return rootId;
    }
    //Getters-setters end

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    private String getFirstLevelChildPosition() {
        if (directionToggler) {
            directionToggler = false;
            return Fields.RIGHT;
        } else {
            directionToggler = true;
            return Fields.LEFT;
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

        if (this.isARoot()) {
            this.addThisFirstLevelChildToSubTree(node);
        }
        return this;
    }

    private void addThisFirstLevelChildToSubTree(Node node) {
        if (node.getPosition().equals(Constants.POSITION.RIGHT.toString())) {
            ArrayList<String> rightSubTree = this.getRight();
            if (this.isChildAlreadyExists(node, rightSubTree))
                rightSubTree.remove(node.getId());
            rightSubTree.add(rightSubTree.size(), node.getId());
        }
        if (node.getPosition().equals(Constants.POSITION.LEFT.toString())) {
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
        } else if (node.getPosition().equals(Constants.POSITION.LEFT.toString()) && this.isChildAlreadyExists(node, this.getLeft())) {
            this.getLeft().remove(node.getId());
        } else
            throw new NodeDoesNotExistException();
    }

    @Override
    public String toString() {

        String jsonString = "";
        jsonString = "{" +
                "\"" + "_id" + "\"" + ":" + "\"" + _id + "\"" +
                "," + "\"" + "name" + "\"" + ":" + "\"" + name + "\"" +
                "," + "\"" + "left" + "\"" + ":" + left +
                "," + "\"" + "right" + "\"" + ":" + right +
                "," + "\"" + "childSubTree" + "\"" + ":" + childSubTree +
                "," + "\"" + "position" + "\"" + ":" + position +
                "," + "\"" + "parentId" + "\"" + ":" + parentId +
                "," + "\"" + "rootId" + "\"" + ":" + parentId +
                "," + "\"" + "index" + "\"" + ":" + parentId +
                "}";
        return jsonString;
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