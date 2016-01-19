package com.thoughtworks.mindit.mindit.presenter;

import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.CustomAdapter;
import com.thoughtworks.mindit.mindit.JsonParserService;
import com.thoughtworks.mindit.mindit.MockDB;
import com.thoughtworks.mindit.mindit.UINode;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.model.Node;


import java.util.ArrayList;

public class Presenter {
    private MockDB mockDB;
    private JsonParserService jsonParserService;

    private Tree tree;
    private ArrayList<UINode> nodeList;

    private CustomAdapter customAdapter;

    public Presenter() {
        this.nodeList = null;
    }

    public Presenter(ArrayList<UINode> nodeList) {
        this.nodeList = nodeList;
    }

    public Presenter(Tree tree) {
        this.tree = tree;
        nodeList = new ArrayList<>();
    }

    public UINode convertModelNodeToUINode(Node node) {
        UINode uiNode = new UINode(node.getName(), node.getDepth() * Constants.paddingForDepth);
        uiNode.setId(node.getId());
        updateUIChildSubtree(node, uiNode);
        return uiNode;
    }

    public Node convertUINodeToModelNode(UINode uiNode, Node parent) {
        Node node;
        node = new Node(uiNode.getId(), uiNode.getName(), parent, parent.getRootId(), 0);
        return node;
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }

    public Tree getTree() {
        return tree;
    }

    public ArrayList<UINode> buildNodeListFromTree() {
        UINode uiNode = convertModelNodeToUINode(this.tree.getRoot());
        nodeList.add(0, uiNode);
        return nodeList;
    }


    public void updateUIChildSubtree(Node node, UINode uiNode) {
        ArrayList<String> keys = node.getChildSubTree();

        ArrayList<UINode> childSubTree = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            Node node1 = tree.getNode(keys.get(i));
            UINode uiNode1 = new UINode(node1.getName(), node1.getDepth() * Constants.paddingForDepth);
            uiNode1.setId(node1.getId());
            for (int j = 0; j < node1.getChildSubTree().size(); j++) {
                updateUIChildSubtree(node1, uiNode1);
            }
            childSubTree.add(i, uiNode1);
        }
        uiNode.setChildSubTree(childSubTree);
    }

    public ArrayList<UINode> getArrayList() {
        return nodeList;
    }

}
