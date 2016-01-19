package com.thoughtworks.mindit.mindit;

import android.content.Context;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;
import java.util.HashMap;
import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;

public class Tracker implements MeteorCallback{
    private Meteor meteor;
    private Tree tree;

    public Tracker(Context context) {
        Meteor.setLoggingEnabled(true);
        meteor = new Meteor(context, "ws://192.168.1.106:3000/websocket");
        meteor.setCallback(this);
    }

    public boolean isConnected() {
        return meteor.isConnected();
    }

    public boolean isNull(){
        return tree==null;
    }
    @Override
    public void onConnect(boolean b) {
        System.out.println("Connected boss");
        meteor.subscribe("rootnode", new String[]{"z86i9NMJ8LM7FTwR8"});
        meteor.subscribe("mindmap",new String []{"z86i9NMJ8LM7FTwR8"});
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String fieldsJson) {
        System.out.println("Data added to <"+collectionName+"> in document <"+documentID+">");
        System.out.println("    Added: " + fieldsJson);
        Node node = JsonParserService.parseNode(fieldsJson);

        if(tree == null){
            HashMap<String,Node> map=new HashMap<String,Node>();
            node.set_id(documentID);
            map.put(node.getId(),node);
            tree = new Tree(map);
        }
        else {
            node.set_id(documentID);
            tree.addNode(node,node.getIndex());
        }
        System.out.println(tree);
    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        System.out.println("Data Changed to <"+collectionName+"> in document <"+documentID+">");
        System.out.println("    Chnaged: " + updatedValuesJson);
        System.out.println("s3: " + removedValuesJson);
        Node node = tree.getNode(documentID);
    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        System.out.println("Data removed to <"+collectionName+"> in document <"+documentID+">");
    }

    @Override
    public void onException(Exception e) {

    }
}
