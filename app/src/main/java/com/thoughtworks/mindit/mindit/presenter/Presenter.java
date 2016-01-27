package com.thoughtworks.mindit.mindit.presenter;

import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.PublishSubscribe.IObserver;
import com.thoughtworks.mindit.mindit.Tracker;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.view.MindmapActivity;
import com.thoughtworks.mindit.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import java.util.ArrayList;
import java.util.HashMap;


public class Presenter implements IObserver {
    private Tree tree;
    private Tracker tracker;
    private ArrayList<UINode> nodeList;
    private HashMap<String, UINode> nodeTree;
    private UINode uiNode;
    private CustomAdapter customAdapter;

    public Presenter() {
        nodeList = new ArrayList<UINode>();
        nodeTree = new HashMap<String, UINode>();
        tracker = Tracker.getInstance();
        tree = tracker.getTree();
        tree.register(this);
        uiNode = null;
    }

    public void setCustomAdapter(CustomAdapter customAdapter) {
        this.customAdapter = customAdapter;
    }

    public UINode convertModelNodeToUINode(Node node) {
        int depth = node.getDepth()*Constants.PADDING_FOR_DEPTH;
        UINode uiNode = new UINode(node.getName(), depth, node.getParentId());
        uiNode.setId(node.getId());
        updateUIChildSubtree(node, uiNode);
        return uiNode;
    }

    public Node convertUINodeToModelNode(UINode uiNode, Node parent) {
        Node node;
        String rootId = "";
        if (parent != null) {
            rootId = parent.getRootId();
            if (parent.isARoot())
                rootId = parent.getId();
                node = new Node(uiNode.getId(), uiNode.getName(), parent, rootId, parent.getChildSubTree().size());
        }
        else
            node = new Node(uiNode.getId(), uiNode.getName(), parent, null, 0);

        //----update child subtree---//
        for (int i = 0; i < uiNode.getChildSubTree().size(); i++) {
            node.getChildSubTree().add(i, uiNode.getChildSubTree().get(i).getId());
        }
        return node;
    }

    public ArrayList<UINode> buildNodeListFromTree() {
        UINode rootNode = convertModelNodeToUINode(this.tree.getRoot());
        if (nodeList.size() != 0)
            nodeList.clear();
        //---get expanded tree for the first time---//
        rootNode.setStatus("expand");
        nodeList.add(0, rootNode);
        nodeTree.put(rootNode.getId(), rootNode);
        return nodeList;
    }

    public void updateUIChildSubtree(Node node, UINode uiNode) {
        ArrayList<String> keys = node.getChildSubTree();
        ArrayList<UINode> childSubTree = new ArrayList<UINode>();

        for (int i = 0; i < keys.size(); i++) {

            Node node1 = tree.getNode(keys.get(i));
            int depth=node1.getDepth() * Constants.PADDING_FOR_DEPTH;
            UINode uiNode1 = new UINode(node1.getName(), depth , node.getId());
            uiNode1.setId(node1.getId());

            for (int j = 0; j < node1.getChildSubTree().size(); j++) {
                updateUIChildSubtree(node1, uiNode1);
            }

            childSubTree.add(i, uiNode1);
            nodeTree.put(uiNode1.getId(), uiNode1);
        }

        uiNode.setChildSubTree(childSubTree);
    }

    public void addChild(UINode uiNode) {
        Node parent = tree.getNode(uiNode.getParentId());

        String rootId = parent.getRootId();
        if (parent.isARoot())
            rootId = parent.getId();

        Node node = new Node("", uiNode.getName(), parent, rootId, 0);
        this.uiNode = uiNode;
        tracker.addChild(node);
    }

    public void updateNode(UINode uiNode) {
        Node node = tree.getNode(uiNode.getId());
        node.setName(uiNode.getName());
        tracker.updateNode(node);
    }

    public void deleteNode(UINode uiNode) {
        tracker.deleteNode(uiNode.getId());
    }

    public UINode getUiNode (String id) {
        for (UINode uiNode : nodeList) {
            if(uiNode.getId().equals(id))
                return uiNode;
        }
        return null;
    }

    @Override
    public void update(int updateOption) {
        switch (updateOption)
        {
            case 1:
                if(uiNode == null) {
                    uiNode = convertModelNodeToUINode(tree.getLastUpdatedNode());
                    UINode uiParent = this.getUiNode(uiNode.getParentId());
                    uiParent.getChildSubTree().add(tree.getLastUpdatedNode().getIndex(), uiNode);

                    customAdapter.collapse(nodeList.indexOf(uiParent), uiParent);
                    customAdapter.expand(nodeList.indexOf(uiParent), uiParent);
                    uiParent.setStatus(Constants.STATUS.EXPAND.toString());
                }
                else
                    this.uiNode.setId(tree.getLastUpdatedNode().getId());
                this.uiNode = null;
                break;
            case 2:
                UINode temp = convertModelNodeToUINode(tree.getLastUpdatedNode());
                uiNode = nodeTree.get(temp.getId());
                uiNode.setName(temp.getName());
                uiNode.setChildSubTree(temp.getChildSubTree());
                uiNode.setParentId(temp.getParentId());
                break;
            case 3:
                break;

        }
        if (customAdapter != null) {
            MindmapActivity mindmapActivity = (MindmapActivity) customAdapter.getContext();
            try {
                mindmapActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        customAdapter.notifyDataSetChanged();
                    }
                });
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
