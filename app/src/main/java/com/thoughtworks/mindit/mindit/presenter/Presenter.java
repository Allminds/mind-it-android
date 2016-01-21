package com.thoughtworks.mindit.mindit.presenter;

import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.CustomAdapter;
import com.thoughtworks.mindit.mindit.JsonParserService;
import com.thoughtworks.mindit.mindit.MockDB;
import com.thoughtworks.mindit.mindit.PublishSubscribe.IObserver;
import com.thoughtworks.mindit.mindit.Tracker;
import com.thoughtworks.mindit.mindit.UINode;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.model.Node;


import java.util.ArrayList;


public class Presenter implements IObserver{
    private Tree tree;
    private Tracker tracker;

    private ArrayList<UINode> nodeList;

    public void setCustomAdapter(CustomAdapter customAdapter) {
        this.customAdapter = customAdapter;
    }

    private CustomAdapter customAdapter;

    public Presenter() {
        nodeList = new ArrayList<>();
        tracker = Tracker.getInstance();
        tree = tracker.getTree();
        tree.register(this);
    }

    public UINode convertModelNodeToUINode(Node node) {
        UINode uiNode = new UINode(node.getName(), node.getDepth() * Constants.PADDING_FOR_DEPTH,node.getParentId());
        uiNode.setId(node.getId());
        updateUIChildSubtree(node, uiNode);
        return uiNode;
    }

    public Node convertUINodeToModelNode(UINode uiNode, Node parent) {
        Node node;
        String rootId = parent.getRootId();
        if(parent.isARoot())
            rootId = parent.getId();
        node = new Node(uiNode.getId(), uiNode.getName(), parent, rootId, parent.getChildSubTree().size());
        return node;
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
            UINode uiNode1 = new UINode(node1.getName(), node1.getDepth() * Constants.PADDING_FOR_DEPTH,node.getId());
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

    @Override
    public void update() {
        this.nodeList = this.buildNodeListFromTree();
        System.out.println("presenter is calling update");
        if (customAdapter != null)
            customAdapter.notifyDataSetChanged();
    }

    public void addChild(int position) {
        UINode uiNode=nodeList.get(position);
        Node parent = tree.getNode(uiNode.getParentId());
        tracker.addChild(this.convertUINodeToModelNode(uiNode, parent));
    }
}
