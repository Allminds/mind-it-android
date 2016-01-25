package com.thoughtworks.mindit.mindit;

import android.content.Context;
import android.support.annotation.NonNull;

import com.thoughtworks.mindit.mindit.helper.JsonParserService;
import com.thoughtworks.mindit.mindit.helper.Meteor;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;

public class Tracker implements MeteorCallback{
    private Meteor meteor;
    private String rootId;
    private Tree tree;
    private static Tracker instance;

    private Tracker(Context context, String rootId) {
        this.rootId = rootId;
        Meteor.setLoggingEnabled(true);
        meteor = new Meteor(context, "ws://10.12.20.188:3000/websocket");
        meteor.setCallback(this);
    }

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
                System.out.println(jsonResponse);
                tree = JsonParserService.parse(jsonResponse);
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
    }

    public void addChild(final Node node) {
        Map<String, Object> addValues = getValueMap(node);
        meteor.insert("Mindmaps", addValues, new ResultListener() {
            @Override
            public void onSuccess(String s) {
                //ignore first([) and last character(])
                s = s.substring(1, s.length() - 1);
                Node tempNode = JsonParserService.parseNode(s);
                node.set_id(tempNode.getId());
                tree.addNode(node);

                updateParentInDB(node);
            }

            @Override
            public void onError(String s, String s1, String s2) {
            }
        });

    }

    public void deleteNode (String nodeID){
        Node node = tree.getNode(nodeID);
        try {
            tree.deleteNode(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateParentInDB(node);
    }

    public void updateNode(final Node node){
        Map<String, Object> updateQuery = new HashMap<String, Object>();
        updateQuery.put("_id", node.getId());
        Map<String, Object> updateValues = getValueMap(node);
        meteor.update("Mindmaps", updateQuery, updateValues, null, new ResultListener() {
            @Override
            public void onSuccess(String s) {
                tree.updateNode(node);
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
    }

    private void updateParentInDB(Node node) {
        Node parent = tree.getNode(node.getParentId());
        Map<String, Object> updateQuery = new HashMap<String, Object>();
        updateQuery.put("_id", parent.getId());
        Map<String, Object> updateValues = getValueMap(parent);
        meteor.update("Mindmaps", updateQuery, updateValues, null, new ResultListener() {
            @Override
            public void onSuccess(String s) {
            }

            @Override
            public void onError(String s, String s1, String s2) {
            }
        });
    }

    @NonNull
    private Map<String, Object> getValueMap(Node node) {
        Map<String, Object> addValues = new HashMap<String, Object>();
        addValues.put("name", node.getName());
        addValues.put("left", node.getLeft());
        addValues.put("right", node.getRight());
        addValues.put("position", node.getPosition());
        if (node.isNotARoot()){
            addValues.put("childSubTree", node.getChildSubTree());
        }
        else {
            addValues.put("childSubTree", new ArrayList<String>());
        }
        addValues.put("parentId", node.getParentId());
        addValues.put("rootId", node.getRootId());
        addValues.put("index", node.getIndex());
        return addValues;
    }

    @Override
    public void onConnect(boolean b) {
        System.out.println("Connected");
        this.findTree(rootId);
        //meteor.subscribe("mindmap", new String[]{rootId});
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onDataAdded(String s, String s1, String s2) {

    }

    @Override
    public void onDataChanged(String s, String s1, String s2, String s3) {

    }

    @Override
    public void onDataRemoved(String s, String s1) {

    }

    @Override
    public void onException(Exception e) {

    }
}
