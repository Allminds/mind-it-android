package com.thoughtworks.mindit.mindit;

import android.content.Context;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.presenter.Presenter;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;

public class Tracker implements MeteorCallback{
    private Meteor meteor;
    private String rootId;

    private static Tracker instance;

    public static Tracker getInstance(Context context, String rootId) {
        if(instance == null)
            instance = new Tracker(context, rootId);
        return instance;
    }

    public static Tracker getInstance() {
        return instance;
    }

    public Tree getTree() {
        return tree;
    }

    private Tree tree;

    private Tracker(Context context, String rootId) {
        this.rootId = rootId;
        Meteor.setLoggingEnabled(true);
        meteor = new Meteor(context, "ws://10.12.23.153:3000/websocket");
        meteor.setCallback(this);
    }

    public void subscribe(String rootId) {
        meteor.subscribe("mindmap", new String[]{rootId});
    }

    public boolean isConnected() {
        return meteor.isConnected();
    }

    public void findTree(String rootId) {
        meteor.call("findTree", new String[]{rootId}, new ResultListener() {
            @Override
            public void onSuccess(String jsonResponse) {
                // This is where tree will be initialized
                System.out.println(jsonResponse);
                tree = JsonParserService.parse(jsonResponse);
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
    }

    public boolean isNull(){
        return tree==null;
    }

    @Override
    public void onConnect(boolean b) {
        System.out.println("Connected");
        meteor.subscribe("mindmap", new String[] {rootId});
        this.findTree(rootId);

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String fieldsJson) {
        System.out.println("Data added to <" + collectionName + "> in document <" + documentID + ">");
        System.out.println("    Added: " + fieldsJson);
        Node node = JsonParserService.parseNode(fieldsJson);
        node.set_id(documentID);
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

    public void addChild(final Node node) {
        Map<String, Object> addValues = new HashMap<String, Object>();
        addValues.put("name", node.getName());
        addValues.put("left", node.getLeft());
        addValues.put("right", node.getRight());
        addValues.put("childSubTree", node.getChildSubTree());
        addValues.put("parentId", node.getParentId());
        addValues.put("rootId", node.getRootId());
        addValues.put("index", node.getIndex());
        meteor.insert("Mindmaps", addValues, new ResultListener() {
            @Override
            public void onSuccess(String s) {
                System.out.println("s ::: " + s);
                node.set_id(s);
                tree.addNode(node);
                Node parent = tree.getNode(node.getParentId());
                Map<String, Object> updateQuery = new HashMap<String, Object>();
                updateQuery.put("_id", parent.getId());
                Map<String, Object> updateValues = new HashMap<String, Object>();

                String str="";
                for(int i=0;i<parent.getChildSubTree().size();i++)
                    str+=parent.getChildSubTree().get(i)+",";

                updateValues.put("childSubTree", str);

                System.out.println("ch : " + parent.getChildSubTree());

                updateValues.put("name", parent.getName());
                updateValues.put("left", parent.getLeft());
                updateValues.put("right", parent.getRight());
                updateValues.put("parentId", parent.getParentId());
                updateValues.put("rootId", parent.getRootId());
                updateValues.put("index", parent.getIndex());

                meteor.update("Mindmaps", updateQuery, updateValues, null, new ResultListener() {
                    @Override
                    public void onSuccess(String s) {
                        System.out.println("update ::: " + s);
                    }

                    @Override
                    public void onError(String s, String s1, String s2) {

                    }
                });
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });


    }
}
