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

import com.thoughtworks.mindit.mindit.constant.Constants;
import com.thoughtworks.mindit.mindit.constant.Fields;
import com.thoughtworks.mindit.mindit.constant.MindIt;
import com.thoughtworks.mindit.mindit.constant.NetworkMessage;
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
        new WaitForTree().execute(this.tree);
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
        if (tree != null) {
            tree.removeAllNodes();
            this.tree = null;
        }
        instance = null;
        meteor.disconnect();
    }

    public void subscribe(String rootId) {
        meteor.subscribe(MindIt.SUBSCRIPTION_NAME, new String[]{rootId});
    }

    public boolean isConnected() {
        return meteor.isConnected();
    }

    public void findTree(String rootId) {
        meteor.call(MindIt.FIND_TREE, new String[]{rootId}, new ResultListener() {
            @Override
            public void onSuccess(String jsonResponse) {
                tree = JsonParserService.parse(jsonResponse);
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
    }

    public void addChild(final Node node) {
        Map<String, Object> addValues = getValueMap(node);
        meteor.insert(MindIt.COLLECTION, addValues, new ResultListener() {
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
        updateQuery.put(MindIt.ID, node.getId());
        Map<String, Object> updateValues = getValueMap(node);
        meteor.update(MindIt.COLLECTION, updateQuery, updateValues, null, new ResultListener() {
            @Override
            public void onSuccess(String s) {
                tree.updateNode(node, Fields.NAME, node.getName());
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
    }

    private void updateParentInDB(Node node) {
        Node parent = tree.getNode(node.getParentId());
        Map<String, Object> updateQuery = new HashMap<String, Object>();
        updateQuery.put(MindIt.ID, parent.getId());
        Map<String, Object> updateValues = getValueMap(parent);
        meteor.update(MindIt.COLLECTION, updateQuery, updateValues, null, new ResultListener() {
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
        addValues.put(Fields.NAME, node.getName());
        addValues.put(Fields.LEFT, node.getLeft());
        addValues.put(Fields.RIGHT, node.getRight());
        addValues.put(Fields.POSITION, node.getPosition());
        if (node.isNotARoot()) {
            addValues.put(Fields.CHILD_SUBTREE, node.getChildSubTree());
        } else {
            addValues.put(Fields.CHILD_SUBTREE, new ArrayList<String>());
        }
        addValues.put(Fields.PARENT_ID, node.getParentId());
        addValues.put(Fields.ROOT_ID, node.getRootId());
        addValues.put(Fields.INDEX, node.getIndex());
        return addValues;
    }

    @Override
    public void onConnect(boolean b) {
        this.findTree(rootId);
        meteor.subscribe(MindIt.SUBSCRIPTION_NAME, new String[]{rootId});
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
        System.out.println("on added::   " + documentID + "    " + node.getName());
        node.set_id(documentID);
        if (tree != null && !tree.isAlreadyExists(node)) {
            tree.addNodeFromWeb(node);
        }
    }

    @Override
    public void onChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        Node node = tree.getNode(documentID);
        System.out.println("****** on changed:  " + documentID + "     " + node.getName() + "      " + updatedValuesJson);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonFields = JsonParserService.rawParse(updatedValuesJson);
            if (jsonFields.has(Fields.NAME)) {
                updateNodeName(node, jsonFields);
            }
            if (jsonFields.has(Fields.CHILD_SUBTREE)) {
                updateChildSubTree(node, jsonFields);
            }
            if (jsonFields.has(Fields.LEFT)) {
                updateLeftTree(node, jsonFields);
            }
            if (jsonFields.has(Fields.RIGHT)) {
                updateRightTree(node, jsonFields);
            }
            if (jsonFields.has(Fields.PARENT_ID)) {
                updateParentId(node, jsonFields);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateParentId(Node node, JSONObject jsonFields) throws JSONException {
        String parentId = jsonFields.getString(Fields.PARENT_ID);
        tree.updateNode(node, Fields.PARENT_ID, parentId);
    }

    private void updateRightTree(Node node, JSONObject jsonFields) throws JSONException {
        JSONArray jsonRightTree = (JSONArray) jsonFields.get(Fields.RIGHT);
        ArrayList<String> rightTree = new ArrayList<String>();
        Node root = tree.getRoot();
        for (int i = 0; i < jsonRightTree.length(); i++) {
            rightTree.add(jsonRightTree.getString(i));
            String newNodeId = jsonRightTree.getString(i);

            if (tree.getNode(newNodeId) == null) {
                Node newNode = new Node(newNodeId, Constants.EMPTY_STRING, root, root.getId(), i);
                tree.addNodeFromWeb(newNode);
            }
        }
        tree.updateNode(node, Fields.RIGHT, rightTree);
    }

    private void updateLeftTree(Node node, JSONObject jsonFields) throws JSONException {
        Node root = tree.getRoot();
        JSONArray jsonLeftTree = (JSONArray) jsonFields.get(Fields.LEFT);
        ArrayList<String> leftTree = new ArrayList<String>();
        for (int i = 0; i < jsonLeftTree.length(); i++) {
            leftTree.add(jsonLeftTree.getString(i));
            String newNodeId = jsonLeftTree.getString(i);

            if (tree.getNode(newNodeId) == null) {
                Node newNode = new Node(newNodeId, Constants.EMPTY_STRING, root, root.getId(), i);
                tree.addNodeFromWeb(newNode);
            }
        }
        tree.updateNode(node, Fields.LEFT, leftTree);
    }

    private void updateChildSubTree(Node node, JSONObject jsonFields) throws JSONException {
        Node parent = node;
        ArrayList<String> oldChildSubTree = parent.getChildSubTree();
        JSONArray jsonChildSubTree = (JSONArray) jsonFields.get(Fields.CHILD_SUBTREE);
        ArrayList<String> newChildSubTree = new ArrayList<String>();
        for (int i = 0; i < jsonChildSubTree.length(); i++) {
            newChildSubTree.add(jsonChildSubTree.getString(i));
        }

        if (newChildSubTree.size() - oldChildSubTree.size() == 1) {
            ArrayList<String> clonedChildSubTree = (ArrayList<String>) newChildSubTree.clone();
            clonedChildSubTree.removeAll(oldChildSubTree);
            String newNodeId = clonedChildSubTree.get(0);
//            if (tree.getNode(newNodeId) == null) {
//                Node newNode = new Node(newNodeId, Constants.EMPTY_STRING, parent, parent.getRootId(), newChildSubTree.indexOf(newNodeId));
//                tree.addNodeFromWeb(newNode);
//            }
            while (tree.getNode(newNodeId)==null){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Node newNode=tree.getNode(newNodeId);
            tree.addNodeFromWeb(newNode);
        }
        if (newChildSubTree.equals(oldChildSubTree)) {
            return;
        }
        //separate condition in case of repositioning of nodes
        tree.updateNode(node, Fields.CHILD_SUBTREE, newChildSubTree);
    }

    private void updateNodeName(Node node, JSONObject jsonFields) throws JSONException {
        String name = jsonFields.getString(Fields.NAME);
        tree.updateNode(node, Fields.NAME, name);
    }

    @Override
    public void onRemoved(String collectionName, String documentID) {
    }

    public void registerThisToTree(Presenter presenter) {
        tree.register(presenter);
    }

    class WaitForTree extends AsyncTask<Tree, Void, String> {
        //        AlertDialog progressDialog;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
//            progressDialog = new SpotsDialog(context,"Please wait");
            progressDialog = new ProgressDialog(context);
//            progressDialog.setTitle(NetworkMessage.CONNECTION_CHECK.toString());
            progressDialog.setTitle(NetworkMessage.WAIT);
            progressDialog.setMessage(NetworkMessage.CONNECTION_CHECK);
            progressDialog.setCancelable(false);
//            progressDialog.setProgressStyle(R.style.myProgressDialog);
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
                return downLoadMindmap();
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String message = NetworkMessage.CONNECTION_ERROR;
                return message;
            }
        }

        @NonNull
        private String downLoadMindmap() {

            ((HomeActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    progressDialog.setMessage(NetworkMessage.DOWNLOAD);
                }
            });


            while (tree == null) {
                if (JsonParserService.isErrorOccurred())
                    break;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return NetworkMessage.SUCCESS;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result.equals(NetworkMessage.CONNECTION_ERROR)) {
                resetTree();
                Toast toast = Toast.makeText(context, result, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 100);
                toast.show();

            } else if (JsonParserService.isErrorOccurred()) {
                resetTree();
                JsonParserService.resetErrorOccurredFlag();
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setMessage(MindIt.INVALID_ID_ERROR);
                alertDialog.setButton(Constants.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            } else {
                Intent intent = new Intent(context, MindmapActivity.class);
                context.startActivity(intent);
            }

        }
    }
}
