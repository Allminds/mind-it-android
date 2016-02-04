package com.thoughtworks.mindit.mindit.view;

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

import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.presenter.Presenter;
import com.thoughtworks.mindit.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import java.util.ArrayList;

public class MindmapActivity extends AppCompatActivity implements IMindmapView {

    Menu myMenu;
    private ListView listView;
    private CustomAdapter adapter;
    private Presenter presenter;
    private UINode clipboard;
    private ArrayList<UINode> nodeList;
    private Toolbar toolbar;
    private MenuItem delete;
    private MenuItem add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindmap);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle("MindIt");

        listView = (ListView) findViewById(R.id.listView);
        registerForContextMenu(listView);
        presenter = new Presenter(this);
        adapter = new CustomAdapter(this, presenter,presenter.buildNodeListFromTree());

        listView.setAdapter(adapter);
        nodeList = adapter.getNodeList();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.actions, menu);
        myMenu = menu;
        add = myMenu.getItem(Constants.ADD);
        delete = myMenu.getItem(Constants.DELETE);
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        delete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        add.setVisible(true);
        delete.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int newSelectionPosition;
        int positionOfSelectedNode = adapter.getSelectedNodePosition();
        System.out.println(item.getTitle() + " " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.add:
                UINode parent =  nodeList.get(positionOfSelectedNode);
                UINode newNode = adapter.addChild(positionOfSelectedNode, parent);
                adapter.collapse(nodeList.indexOf(parent), parent);
                adapter.expand(nodeList.indexOf(parent),parent);
                newSelectionPosition = nodeList.indexOf(newNode);
                break;
            case R.id.delete:
                deleteNode(positionOfSelectedNode);
                newSelectionPosition = (positionOfSelectedNode == 0) ? 0 : positionOfSelectedNode - 1;
                break;
            default:
                return true;
        }
        adapter.resetSelectedNodePosition(newSelectionPosition);
        toolbar.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        return true;
    }

    private void cutNode(int position) {
        UINode node = nodeList.get(position);
        clipboard = node;
        deleteNode(position);
        clipboard.setId("");
        clipboard.setStatus("collapse");
    }

    private void copyNode(int position) {
        UINode node = nodeList.get(position);
        clipboard = new UINode(node.getName(), 0, "");
        copyChildSubTree(node, clipboard);

    }

    private void pasteNode(int position) {

        UINode parent = nodeList.get(position);
        int childPosition = parent.getChildSubTree().size();

        clipboard.setDepth(parent.getDepth() + 20);
        clipboard.setParentId(parent.getId());
        parent.getChildSubTree().add(clipboard);

        new WaitForTree().execute(clipboard);

        if (parent.getStatus().equals("collapse")) {
            parent.setStatus("expand");
            adapter.expand(position, parent);
        } else
            nodeList.add(childPosition, clipboard);

        UINode temporary = clipboard;
        clipboard = new UINode(clipboard.getName(), 0, "");
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
            Toast.makeText(getApplicationContext(), "Can not delete root node...", Toast.LENGTH_SHORT).show();
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
        System.out.println("*******" + uiNode.getName() + "*********" + nodeList.get(position).getName());
        presenter.deleteNode(uiNode);

        //remove from parent childsubtree

        UINode parent = null;
        for (UINode node : nodeList) {
            if (node.getId().equals(uiNode.getParentId())) {
                parent = node;
                break;
            }
        }
        boolean result = parent != null ? parent.removeChild(uiNode) : false;
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
            node.setId("");
            presenter.addNode(node);
            while (node.getId().equals("")) ;
            return node;
        }

        @Override
        protected void onPostExecute(UINode result) {
            updateChildTree(result);
        }
    }
}
