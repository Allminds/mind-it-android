package com.thoughtworks.mindit.mindit.presenter;

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
    private CustomAdapter customAdapter;
    private ArrayList<UINode> nodes;

    public Presenter(){
        this.nodes=null;
        tree=new Tree(null);
    }

    public Presenter(ArrayList<UINode> nodes){
        this.nodes=nodes;

    }
    public Tree getTree() {
        return tree;
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }

    public UINode convertModelNodeToUINode( Node node){
        UINode uiNode=new UINode(node.getName(),node.getDepth()*20);
        uiNode.setId(node.getId());
        updateUIChildSubtree(node, uiNode);

        return uiNode;
    }

    public Node convertUINodeToModelNode(UINode uiNode , Node parent){
        Node node;
        node=new Node(uiNode.getId(),uiNode.getName(),parent,parent.getRootId(),0);
        return node;
    }

    public void buildTree(){
        mockDB= new MockDB();
        jsonParserService=new JsonParserService();
        tree=jsonParserService.parse(mockDB.getData());
        Node node=tree.getNode("0");
//        tree.updateChildSubtree();
        UINode uiNode=convertModelNodeToUINode(node);
        nodes.add(0, uiNode);
    }


    public void updateUIChildSubtree(Node node, UINode uiNode){
        ArrayList<String> keys=node.getChildSubTree();
        ArrayList<UINode> childSubTree =new ArrayList<>();
        for(int i=0;i<keys.size();i++){
            Node node1=tree.getNode(keys.get(i));
            UINode uiNode1=new UINode(node1.getName(),node1.getDepth()*20);
            uiNode1.setId(node1.getId());
            for(int j=0;j<node1.getChildSubTree().size();j++){
                updateUIChildSubtree(node1, uiNode1);
            }
            childSubTree.add(i,uiNode1 );
        }
        uiNode.setChildSubTree(childSubTree);
    }

    public ArrayList<UINode> getArrayList(){
        buildTree();
        return nodes;
    }

}
