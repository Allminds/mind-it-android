package com.thoughtworks.mindit.mindit.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
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

public class MindmapActivity extends AppCompatActivity {

    private ListView listView;
    private CustomAdapter adapter;
    private Presenter presenter;
    private UINode clipboard;
    private ArrayList<UINode> nodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindmap);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle("Hi Guest");

        listView = (ListView) findViewById(R.id.listView);
        registerForContextMenu(listView);


        presenter = new Presenter();

        adapter = new CustomAdapter(this, presenter);
        listView.setAdapter(adapter);
        presenter.setCustomAdapter(adapter);
        nodeList = adapter.getNodeList();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");//        menu.add(0, 1, 0, "Add");

        /*menu.add(0, 1, 0, "Delete");
        menu.add(0, 3, 0, "Copy");
        menu.add(0, 4, 0, "Cut");
        if (clipboard != null)
            menu.add(0, 5, 0, "Paste");*/

        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = adapterContextMenuInfo.position;
        if (position != 0) {
            menu.add(0, 2, 0, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                break;
            case 2:
                deleteNode(item);
                break;
            case 3:
                copyNode(item);
                break;
            case 4:
                cutNode(item);
                break;
            case 5:
                pasteNode(item);
                break;
            default:
                return false;
        }
        return true;
    }

    private void cutNode(MenuItem item) {
        int position = getPosition(item);
        UINode node = nodeList.get(position);
        clipboard = node;
        deleteNode(item);
        clipboard.setId("");
        clipboard.setStatus("collapse");
    }

    private void copyNode(MenuItem item) {
        int position = getPosition(item);
        UINode node = nodeList.get(position);
        clipboard = new UINode(node.getName(), 0, "");
        copyChildSubTree(node, clipboard);

    }

    private void pasteNode(MenuItem item) {
        int position = getPosition(item);
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

    private void deleteNode(MenuItem item) {
        int position = getPosition(item);
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
            updateChildSubTree(result);
        }
    }


}
