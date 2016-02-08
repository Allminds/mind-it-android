package com.thoughtworks.mindit.mindit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.Toast;

import com.thoughtworks.mindit.mindit.helper.ITracker;
import com.thoughtworks.mindit.mindit.helper.JsonParserService;
import com.thoughtworks.mindit.mindit.helper.Meteor;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.presenter.Presenter;
import com.thoughtworks.mindit.mindit.view.HomeActivity;
import com.thoughtworks.mindit.mindit.view.MindmapActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;

public class Tracker implements MeteorCallback, ITracker {
    private static Tracker instance;
    private Meteor meteor;
    private String rootId;
    private Tree tree;
    private Context context;
    private Tracker(Context context, String rootId) {
        this.rootId = rootId;
        this.context = context;
        //Meteor.setLoggingEnabled(true);
        meteor = new Meteor(context, "ws://www.mindit.xyz/websocket", this);
        meteor.setCallback(this);
    }

    public static Tracker getInstance(Context context, String rootId) {
        if (instance == null)
            instance = new Tracker(context, rootId);
        return instance;
    }


    public static Tracker getInstance() {
        return instance;
    }

    public Tree getTree() {
        return tree;
    }

    public void resetTree() {
        System.out.println("**** in reset....");
        if (tree != null) {
            tree.removeAllNodes();
            this.tree = null;
        }

        instance = null;
        meteor.disconnect();

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
                tree = JsonParserService.parse(jsonResponse);
                System.out.println("OnSuccess:***********"+tree);

            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
        new WaitForTree().execute(this.tree);
    }

    public void addChild(final Node node) {
        Map<String, Object> addValues = getValueMap(node);
        meteor.insert("Mindmaps", addValues, new ResultListener() {
            @Override
            public void onSuccess(String s) {
                //ignore first([) and last character(])
                s = s.substring(1, s.length() - 1);
                Node tempNode = JsonParserService.parseNode(s);
                System.out.println("Added:    " + tempNode);
                node.set_id(tempNode.getId());
                tree.addNode(node);
                updateParentInDB(node);
            }

            @Override
            public void onError(String s, String s1, String s2) {
            }
        });
    }

    public void deleteNode(String nodeID) {
        Node node = tree.getNode(nodeID);
        try {
            tree.deleteNode(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateParentInDB(node);
    }

    public void updateNode(final Node node) {
        Map<String, Object> updateQuery = new HashMap<String, Object>();
        updateQuery.put("_id", node.getId());
        Map<String, Object> updateValues = getValueMap(node);
        meteor.update("Mindmaps", updateQuery, updateValues, null, new ResultListener() {
            @Override
            public void onSuccess(String s) {
                tree.updateNode(node, "name", node.getName());
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
        if (node.isNotARoot()) {
            addValues.put("childSubTree", node.getChildSubTree());
        } else {
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
        meteor.subscribe("mindmap", new String[]{rootId});
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

    @Override
    public void onAdded(String collectionName, String documentID, String fieldsJson) {
        Node node = JsonParserService.parseNode(fieldsJson);
        node.set_id(documentID);
        if (tree != null && !tree.isAlreadyExists(node)) {
            tree.addNodeFromWeb(node);
        }
    }

    @Override
    public void onChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        Node node = tree.getNode(documentID);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Npde name::   "+node.getName()+"      onChanged:   " + updatedValuesJson);
        try {
            JSONObject fields = JsonParserService.rawParse(updatedValuesJson);
            if (fields.has("name")) {
                String name = fields.getString("name");
                tree.updateNode(node, "name", name);
            }
            if (fields.has("childSubTree")) {
                Node parent = node;
                ArrayList<String> oldChildSubTree = parent.getChildSubTree();

                JSONArray jsonChildSubTree = (JSONArray) fields.get("childSubTree");
                ArrayList<String> newChildSubTree = new ArrayList<String>();
                for (int i = 0; i < jsonChildSubTree.length(); i++) {
                    newChildSubTree.add(jsonChildSubTree.getString(i));
                }

                if (newChildSubTree.size() - oldChildSubTree.size() == 1) {
                    ArrayList<String> clonedChildSubTree = (ArrayList<String>) newChildSubTree.clone();
                    clonedChildSubTree.removeAll(oldChildSubTree);
                    String newNodeId = clonedChildSubTree.get(0);
                    if (tree.getNode(newNodeId) == null) {
                        Node newNode = new Node(newNodeId, "", parent, parent.getRootId(), newChildSubTree.indexOf(newNodeId));
                        tree.addNodeFromWeb(newNode);
                    }
                }
                if (newChildSubTree.equals(oldChildSubTree)) {
                    return;
                }
                //separate condition in case of repositioning of nodes
                tree.updateNode(node, "childSubTree", newChildSubTree);
            }
            if (fields.has("left")) {
                Node root = tree.getRoot();
                JSONArray jsonLeftTree = (JSONArray) fields.get("left");
                ArrayList<String> leftTree = new ArrayList<String>();
                for (int i = 0; i < jsonLeftTree.length(); i++) {
                    leftTree.add(jsonLeftTree.getString(i));
                    String newNodeId = jsonLeftTree.getString(i);

                    if (tree.getNode(newNodeId) == null) {
                        Node newNode = new Node(newNodeId, "", root, root.getId(), i);
                        tree.addNodeFromWeb(newNode);
                    }
                }
                System.out.println(leftTree);
                tree.updateNode(node, "left", leftTree);
            }
            if (fields.has("right")) {
                JSONArray jsonRightTree = (JSONArray) fields.get("right");
                ArrayList<String> rightTree = new ArrayList<String>();
                Node root = tree.getRoot();
                for (int i = 0; i < jsonRightTree.length(); i++) {
                    rightTree.add(jsonRightTree.getString(i));
                    String newNodeId = jsonRightTree.getString(i);

                    if (tree.getNode(newNodeId) == null) {
                        Node newNode = new Node(newNodeId, "", root, root.getId(), i);
                        tree.addNodeFromWeb(newNode);
                    }
                }
                tree.updateNode(node, "right", rightTree);
            }

            if (fields.has("parentId")) {
                String parentId = fields.getString("parentId");
                tree.updateNode(node, "parentId", parentId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRemoved(String collectionName, String documentID) {
        System.out.println("removed : " + documentID);
    }

    public void registerThisToTree(Presenter presenter) {
        tree.register(presenter);
    }

    class WaitForTree extends AsyncTask<Tree, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Checking network connection....");
            progressDialog.setCancelable(false);
            System.out.println("Context:"+context   );
            ((HomeActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });


        }

        @Override
        protected String doInBackground(Tree... params) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((HomeActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage("Importing MindMap... ");
                    }
                });

                while (tree == null) {
                    System.out.println("In DoInBack>>>>>>"+tree);
                    if (JsonParserService.isErrorOccurred())
                        break;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //    System.out.println(rootId + "   " + tracker.getTree());
                return "success";
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String message = "Please check your network connection.";
                return message;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result.equals("Please check your network connection.")) {

                Toast toast = Toast.makeText(context, result, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 100);
                toast.show();

            } else if (JsonParserService.isErrorOccurred()) {
                resetTree();
                JsonParserService.resetErrorOccurredFlag();
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setMessage("Oops!!!!!\n" + "Looks like you were led astray with an incorrect URL..");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            } else {
                String rootId = result;
                Intent intent = new Intent(context, MindmapActivity.class);
                context.startActivity(intent);
            }

        }
    }
}
