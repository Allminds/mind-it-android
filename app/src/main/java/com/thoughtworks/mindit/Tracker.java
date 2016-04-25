package com.thoughtworks.mindit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.thoughtworks.mindit.authentication.MindmapRequest;
import com.thoughtworks.mindit.authentication.OnAuthenticationChanged;
import com.thoughtworks.mindit.authentication.SessionManager;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.Error;
import com.thoughtworks.mindit.constant.Fields;
import com.thoughtworks.mindit.constant.MeteorMethods;
import com.thoughtworks.mindit.constant.MindIt;
import com.thoughtworks.mindit.constant.NetworkMessage;
import com.thoughtworks.mindit.constant.Operation;
import com.thoughtworks.mindit.helper.ITracker;
import com.thoughtworks.mindit.helper.JsonParserService;
import com.thoughtworks.mindit.helper.Meteor;
import com.thoughtworks.mindit.model.Node;
import com.thoughtworks.mindit.model.Tree;
import com.thoughtworks.mindit.presenter.Presenter;
import com.thoughtworks.mindit.view.HomeActivity;
import com.thoughtworks.mindit.view.MindmapActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;

public class Tracker implements MeteorCallback, ITracker {
    private static Tracker instance;
    private Meteor meteor;
    private String rootId;
    private Tree tree;
    private Context context;
    private String accessError = Error.NO_ERROR;

    private Tracker(Context context, String rootId) {

        this.rootId = rootId;
        this.context = context;
        Meteor.setLoggingEnabled(true);
        startAsyncTask();
        meteor = new Meteor(context, MindIt.WEB_SOCKET, this);

        meteor.setCallback(this);
    }

    public static Tracker getInstance(Context context, String rootId, Operation operation) {
        if (instance == null) {
            if (operation == Operation.OPEN) {
                instance = new Tracker(context, rootId);
            }
            if (operation == Operation.CREATE) {
                instance = new Tracker(context, null);
            }
        }
        return instance;
    }

    public static Tracker getInstance() {
        return instance;
    }

    private String createMindMap() {
        final Node newNode = new Node("", "New Mindmap", null, null, 0);
        Map<String, Object> addValues = getValueMap(newNode);

        String[] data = new String[1];
        SessionManager sessionManager = SessionManager.getInstance(context);

        if (sessionManager.isLoggedIn()) {
            data[0] = sessionManager.getUserDetails().getEmail();
        } else {
            data[0] = "*";
        }
        meteor.call(MeteorMethods.CREATE_ROOT_NODE, data, new ResultListener() {
            @Override
            public void onSuccess(String _id) {
                _id = _id.substring(1, _id.length() - 1);
                rootId = _id;
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
        return null;
    }

    private void startAsyncTask() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new WaitForTree().execute(this.tree);
        }
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

    private void findTree(String rootId) {
        String[] data = new String[3];
        data[0] = rootId;
        SessionManager sessionManager = SessionManager.getInstance(context);
        if (sessionManager.isLoggedIn()) {
            data[1] = sessionManager.getUserDetails().getEmail();
        } else {
            data[1] = "*";
        }
        meteor.call(MeteorMethods.FIND_TREE, data, new ResultListener() {
            @Override
            public void onSuccess(String jsonResponse) {
                tree = JsonParserService.parse(jsonResponse);
            }

            @Override
            public void onError(String errorCode, String s1, String s2) {
                if (errorCode.equalsIgnoreCase(Error.PRIVATE_MINDMAP)) {
                    accessError = Error.PRIVATE_MINDMAP;
                } else if (errorCode.equalsIgnoreCase(Error.OTHER_USERS_MINDMAP)) {
                    accessError = Error.OTHER_USERS_MINDMAP;
                }

            }
        });
    }

    public void addChild(final Node node) {
        Map<String, Object> addValues = getValueMap(node);
        meteor.call(MeteorMethods.CREATE_NODE, new String[]{node.getName(), node.getParentId(), node.getRootId(), node.getPosition()}, new ResultListener() {
            @Override
            public void onSuccess(String _id) {
                _id = _id.substring(1, _id.length() - 1);
                node.set_id(_id);
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
        Map<String, Object> updateQuery = new HashMap<>();
        updateQuery.put(MindIt.ID, node.getId());
        Map<String, Object> updateValues = getValueMap(node);
        meteor.call(MeteorMethods.UPDATE_NODE, new Object[]{node.getId(), updateValues}, new ResultListener() {
            @Override
            public void onSuccess(String s) {
            }

            @Override
            public void onError(String s, String s1, String s2) {

            }
        });
    }

    private void updateParentInDB(Node node) {
        Node parent = tree.getNode(node.getParentId());
        Map<String, Object> updateQuery = new HashMap<>();
        updateQuery.put(MindIt.ID, parent.getId());
        Map<String, Object> updateValues = getValueMap(parent);

        meteor.call(MeteorMethods.UPDATE_NODE, new Object[]{node.getParentId(), updateValues}, new ResultListener() {
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
        Map<String, Object> addValues = new HashMap<>();
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
        return addValues;
    }

    @Override
    public void onConnect(boolean b) {
        if (rootId == null) {
            rootId = createMindMap();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (rootId == null) ;
                findTree(rootId);
                String[] data = new String[2];
                data[0] = rootId;
                SessionManager sessionManager = SessionManager.getInstance(context);
                if (sessionManager.isLoggedIn()) {
                    data[1] = sessionManager.getUserDetails().getEmail();
                } else {
                    data[1] = "*";
                }
                meteor.subscribe(MindIt.SUBSCRIPTION_NAME, data);
            }
        }).start();
    }

    public ArrayList<Node> allRootNodes(String email) {

        return null;
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
            Thread.sleep(500);
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
        ArrayList<String> rightTree = new ArrayList<>();
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
        ArrayList<String> leftTree = new ArrayList<>();
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
        ArrayList<String> oldChildSubTree = node.getChildSubTree();
        JSONArray jsonChildSubTree = (JSONArray) jsonFields.get(Fields.CHILD_SUBTREE);
        ArrayList<String> newChildSubTree = new ArrayList<>();
        for (int i = 0; i < jsonChildSubTree.length(); i++) {
            newChildSubTree.add(jsonChildSubTree.getString(i));
        }

        if (newChildSubTree.size() - oldChildSubTree.size() == 1) {
            ArrayList<String> clonedChildSubTree = (ArrayList<String>) newChildSubTree.clone();
            clonedChildSubTree.removeAll(oldChildSubTree);
            String newNodeId = clonedChildSubTree.get(0);
            if (tree.getNode(newNodeId) == null) {
                Node newNode = new Node(newNodeId, Constants.EMPTY_STRING, node, node.getRootId(), newChildSubTree.indexOf(newNodeId));
                tree.addNodeFromWeb(newNode);
            }

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

    public String getRootId() {
        return rootId;
    }

    private void requestLogin() {
        resetTree();
        final Dialog loginDialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loginDialog = new Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            loginDialog = new Dialog(context);
        }
        loginDialog.setTitle(Constants.LOGIN_DIALOG_TITLE);
        loginDialog.setContentView(R.layout.login_dialog);
      //  final SignInButton login = (SignInButton) loginDialog.findViewById(R.id.login_button);
        final Button login = (Button) loginDialog.findViewById(R.id.login_button);
        login.setFocusable(true);
        loginDialog.show();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
                OnAuthenticationChanged authenticationChanged = (OnAuthenticationChanged) context;
                MindmapRequest mindmapRequest = new MindmapRequest(rootId, false);
                authenticationChanged.onSignInRequest(mindmapRequest);
            }
        });
        final Button cancel = (Button) loginDialog.findViewById(R.id.login_error_ok);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
            }
        });
    }

    private void showNonAccessibleError() {
        resetTree();
        final Dialog errorDialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            errorDialog = new Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            errorDialog = new Dialog(context);
        }
        errorDialog.setTitle(Constants.LOGIN_DIALOG_TITLE);
        errorDialog.setContentView(R.layout.invalid_access_dialog);
        final Button cancel = (Button) errorDialog.findViewById(R.id.login_error_ok);
        errorDialog.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorDialog.dismiss();
            }
        });
    }

    class WaitForTree extends AsyncTask<Tree, Void, String> {

        AlertDialog progressDialog;

        @Override
        protected void onPreExecute() {

            progressDialog = new SpotsDialog(context, NetworkMessage.DOWNLOAD);
            progressDialog.setCancelable(false);
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

                return NetworkMessage.CONNECTION_ERROR;
            }
        }

        @NonNull
        private String downLoadMindmap() {

            while (tree == null && accessError.equalsIgnoreCase(Error.NO_ERROR)) {
                if (JsonParserService.isErrorOccurred())
                    break;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!accessError.equalsIgnoreCase(Error.NO_ERROR)) {
                return accessError;
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

            } else {
                if (JsonParserService.isErrorOccurred()) {
                    resetTree();
                    JsonParserService.resetErrorOccurredFlag();
                    final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setMessage(Error.INVALID_ID_ERROR);
                    alertDialog.setButton(Constants.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                } else {
                    if (result.equalsIgnoreCase(Error.PRIVATE_MINDMAP)) {
                        requestLogin();
                    } else if (result.equalsIgnoreCase(Error.OTHER_USERS_MINDMAP)) {
                        showNonAccessibleError();
                    } else {
                        Intent intent = new Intent(context, MindmapActivity.class);
                        ((HomeActivity)context).startActivityForResult(intent,MindIt.RC_ROOT_NODE);
                    }
                }
            }

        }

    }

}
