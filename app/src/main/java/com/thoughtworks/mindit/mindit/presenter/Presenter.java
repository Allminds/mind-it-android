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

    public Presenter(Tracker tracker, Tree tree) {
        nodeList = new ArrayList<UINode>();
        nodeTree = new HashMap<String, UINode>();
        this.tracker = tracker;
        this.tree = tree;
        //    tree.register(this);
        uiNode = null;
    }

    public void setCustomAdapter(CustomAdapter customAdapter) {
        this.customAdapter = customAdapter;
    }

    public UINode convertModelNodeToUINode(Node node) {
        int depth = node.getDepth() * Constants.PADDING_FOR_DEPTH;
        UINode uiNode = new UINode(node.getName(), depth, node.getParentId());
        uiNode.setId(node.getId());
        nodeTree.put(node.getId(), uiNode);
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
        } else
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
        if (rootNode.getChildSubTree().size() != 0)
            rootNode.setStatus(Constants.STATUS.EXPAND.toString());
        else
            rootNode.setStatus(Constants.STATUS.COLLAPSE.toString());
        nodeList.add(0, rootNode);
        return nodeList;
    }

    public void updateUIChildSubtree(Node node, UINode uiNode) {
        ArrayList<String> keys = node.getChildSubTree();
        ArrayList<UINode> childSubTree = new ArrayList<UINode>();

        for (int i = 0; i < keys.size(); i++) {
            Node node1 = tree.getNode(keys.get(i));
            int depth = node1.getDepth() * Constants.PADDING_FOR_DEPTH;
            UINode uiNode1 = new UINode(node1.getName(), depth, node.getId());
            uiNode1.setId(node1.getId());

            for (int j = 0; j < node1.getChildSubTree().size(); j++) {
                updateUIChildSubtree(node1, uiNode1);
            }

            childSubTree.add(i, uiNode1);
            nodeTree.put(uiNode1.getId(), uiNode1);
        }

        uiNode.setChildSubTree(childSubTree);
    }

    public void addNode(UINode uiNode) {
        Node parent = tree.getNode(uiNode.getParentId());

        String rootId = parent.getRootId();
        if(parent.isARoot())
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

    private ArrayList<UINode> addNewNodeFromWebToParent(Node parent) {
        ArrayList<String> temp = parent.getChildSubTree();
        ArrayList<UINode> childSubTree = new ArrayList<UINode>();
        for (int i = 0; i < temp.size(); i++) {
            UINode uiNode = nodeTree.get(temp.get(i));
            childSubTree.add(uiNode);
        }

        return childSubTree;
    }

    @Override
    public void update(int updateOption, String updateParameter) {
        switch (updateOption) {
            case 1:
                if (uiNode == null) {
                    uiNode = convertModelNodeToUINode(tree.getLastUpdatedNode());
                } else {
                    this.uiNode.setId(tree.getLastUpdatedNode().getId());
                    nodeTree.put(uiNode.getId(), uiNode);
                }
                this.uiNode = null;
                break;
            case 2:
                switch (updateParameter) {
                    case "name":
                        UINode tempUINode = nodeTree.get(tree.getLastUpdatedNode().getId());
                        tempUINode.setName(tree.getLastUpdatedNode().getName());
                        break;
                    case "childSubTree":
                        UINode existingParent = nodeTree.get(tree.getLastUpdatedNode().getId());
                        existingParent.setChildSubTree(this.addNewNodeFromWebToParent(tree.getLastUpdatedNode()));
                        if(nodeList.indexOf(existingParent) != -1) {
                            customAdapter.collapse(nodeList.indexOf(existingParent), existingParent);
                            customAdapter.expand(nodeList.indexOf(existingParent), existingParent);
                            existingParent.setStatus(Constants.STATUS.EXPAND.toString());
                            System.out.println("updated childsubtree " + existingParent.getChildSubTree());
                        }
                        break;
                    case "left":
                    case "right":
                        UINode root = nodeTree.get(tree.getLastUpdatedNode().getId());
                        root.setChildSubTree(this.addNewNodeFromWebToParent(tree.getLastUpdatedNode()));
                        customAdapter.collapse(nodeList.indexOf(root), root);
                        customAdapter.expand(nodeList.indexOf(root), root);
                        root.setStatus(Constants.STATUS.EXPAND.toString());
                        break;
                    case "parentId":
                        break;
                }
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
