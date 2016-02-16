package com.thoughtworks.mindit.view;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.thoughtworks.mindit.Config;
import com.thoughtworks.mindit.NetworkReceiver;
import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.presenter.Presenter;
import com.thoughtworks.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.view.model.UINode;

import java.util.ArrayList;

public class MindmapActivity extends AppCompatActivity implements IMindmapView {

    private CustomAdapter adapter;
    private Presenter presenter;
    private UINode clipboard;
    private ArrayList<UINode> nodeList;
    private Toolbar toolbar;
    private NetworkReceiver networkReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindmap);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setIcon(R.drawable.mindit_logo);
            getSupportActionBar().setTitle(Constants.EMPTY_STRING);
        }
        ListView listView = (ListView) findViewById(R.id.listView);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);

        registerForContextMenu(listView);
        presenter = new Presenter(this);
        adapter = new CustomAdapter(this, presenter, presenter.buildNodeListFromTree());
        listView.setAdapter(adapter);
        nodeList = adapter.getNodeList();

        networkReceiver =new NetworkReceiver(presenter,adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver.getBroadcastReceiver());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver.getBroadcastReceiver(), filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.actions, menu);
        if (Config.FEATURE_ADD) {
            MenuItem add = menu.getItem(Constants.ADD);
            add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            add.setVisible(true);
        }
        if (Config.FEATURE_DELETE) {
            MenuItem delete = menu.getItem(Constants.DELETE);
            delete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            delete.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int newSelectionPosition;
        int positionOfSelectedNode = adapter.getSelectedNodePosition();
        switch (item.getItemId()) {
            case R.id.add:
                newSelectionPosition = addNode(positionOfSelectedNode);
                break;
            case R.id.delete:
                newSelectionPosition = deleteSelectedNode(positionOfSelectedNode);
                break;
            default:
                return true;
        }
        adapter.setSelectedNodePosition(newSelectionPosition);
        toolbar.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        return true;
    }

    private int deleteSelectedNode(int positionOfSelectedNode) {
        int newSelectionPosition;
        deleteNode(positionOfSelectedNode);
        newSelectionPosition = (positionOfSelectedNode == 0) ? 0 : positionOfSelectedNode - 1;
        return newSelectionPosition;
    }

    private int addNode(int positionOfSelectedNode) {
        int newSelectionPosition;
        UINode parent = nodeList.get(positionOfSelectedNode);
        adapter.collapse(nodeList.indexOf(parent), parent);
        adapter.expand(nodeList.indexOf(parent), parent);
        UINode newNode = adapter.addChild(positionOfSelectedNode, parent);
        newSelectionPosition = nodeList.indexOf(newNode);
        return newSelectionPosition;
    }

    private void cutNode(int position) {
        clipboard = nodeList.get(position);
        deleteNode(position);
        clipboard.setId(Constants.EMPTY_STRING);
        clipboard.setStatus(Constants.STATUS.COLLAPSE.toString());
    }

    private void copyNode(int position) {
        UINode node = nodeList.get(position);
        clipboard = new UINode(node.getName(), 0, Constants.EMPTY_STRING);
        copyChildSubTree(node, clipboard);

    }

    private void pasteNode(int position) {

        UINode parent = nodeList.get(position);
        int childPosition = parent.getChildSubTree().size();

        clipboard.setDepth(parent.getDepth() + 20);
        clipboard.setParentId(parent.getId());
        parent.getChildSubTree().add(clipboard);

        new WaitForTree().execute(clipboard);

        if (parent.getStatus().equals(Constants.STATUS.COLLAPSE.toString())) {
            parent.setStatus(Constants.STATUS.EXPAND.toString());
            adapter.expand(position, parent);
        } else
            nodeList.add(childPosition, clipboard);

        UINode temporary = clipboard;
        clipboard = new UINode(clipboard.getName(), 0, Constants.EMPTY_STRING);
        copyChildSubTree(temporary, clipboard);

        adapter.notifyDataSetChanged();
    }

    private void updateChildSubTree(UINode nodeInBuffer) {
        for (UINode node : nodeInBuffer.getChildSubTree()) {
            node.setDepth(nodeInBuffer.getDepth() + 20);
            node.setParentId(nodeInBuffer.getId());
            new WaitForTree().execute(node);
        }
    }

    private int getPosition(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        return info.position;
    }

    private void copyChildSubTree(UINode node, UINode nodeInBuffer) {
        for (UINode node1 : node.getChildSubTree()) {
            UINode child = new UINode(node1.getName(), 0, node.getId());
            nodeInBuffer.getChildSubTree().add(child);
            copyChildSubTree(node1, child);
        }
    }

    private void deleteNode(int position) {
        if (position == 0) {
            Toast.makeText(getApplicationContext(), Constants.ROOT_DELETE_ERROR, Toast.LENGTH_SHORT).show();
            return;
        }
        UINode uiNode = nodeList.get(position);
        for (int i = position + 1; i < nodeList.size(); ) {
            if (nodeList.get(i).getDepth() > uiNode.getDepth()) {
                nodeList.remove(i);
            } else {
                break;
            }
        }
        presenter.deleteNode(uiNode);

        //remove from parent childsubtree

        UINode parent = null;
        for (UINode node : nodeList) {
            if (node.getId().equals(uiNode.getParentId())) {
                parent = node;
                break;
            }
        }
        boolean result = parent != null && parent.removeChild(uiNode);
        if (result) {
            if (parent.getChildSubTree().size() == 0) {
                parent.setStatus(Constants.STATUS.COLLAPSE.toString());
            }
            nodeList.remove(position);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataChanged() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateChildTree(UINode existingParent) {
        adapter.updateChildSubTree(existingParent);
    }


    private class WaitForTree extends AsyncTask<UINode, Void, UINode> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected UINode doInBackground(UINode... params) {
            UINode node = params[0];
            node.setId(Constants.EMPTY_STRING);
            presenter.addNode(node);
            while (node.getId().equals(Constants.EMPTY_STRING)) ;
            return node;
        }

        @Override
        protected void onPostExecute(UINode result) {
            updateChildTree(result);
        }
    }
}
