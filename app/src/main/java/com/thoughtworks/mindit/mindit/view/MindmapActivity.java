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

import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.presenter.Presenter;
import com.thoughtworks.mindit.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import java.util.ArrayList;

public class MindmapActivity extends AppCompatActivity {

    ListView listView;
    CustomAdapter adapter;
    Presenter presenter;
    UINode clipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindmap);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView=(ListView)findViewById(R.id.listView);
        registerForContextMenu(listView);

        presenter = new Presenter();

        adapter = new CustomAdapter(this, presenter);
        listView.setAdapter(adapter);
        presenter.setCustomAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, 1, 0, "Add");
        menu.add(0, 2, 0, "Delete");
        menu.add(0, 3, 0, "Copy");
        menu.add(0, 4, 0, "Cut");
        if(clipboard!=null)
            menu.add(0, 5, 0, "Paste");


    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId()){
            case 1:
                addNewNode(item);
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
        int position=getPosition(item);
        UINode node=adapter.getNodeArrayList().get(position);
        clipboard=node;
        deleteNode(item);
        clipboard.setId("");
        clipboard.setStatus("collapse");
    }

    private void pasteNode(MenuItem item) {
        int position = getPosition(item);
        int childPosition=position;
        UINode parent=adapter.getNodeArrayList().get(position);
        while (++childPosition < adapter.getNodeArrayList().size() && adapter.getNodeArrayList().get(childPosition).getDepth() > adapter.getNodeArrayList().get(position).getDepth()) ;
        clipboard.setDepth(adapter.getNodeArrayList().get(position).getDepth() + 20);

        clipboard.setParentId(adapter.getNodeArrayList().get(position).getId());

        parent.getChildSubTree().add(clipboard);


        new WaitForTree().execute(clipboard);

        if(parent.getStatus().equals("collapse"))
        {
            parent.setStatus("expand");
            adapter.expand(position, parent);

        }
        else
            adapter.getNodeArrayList().add(childPosition,clipboard);
        UINode tmp=clipboard;

        adapter.notifyDataSetChanged();
        clipboard=new UINode(clipboard.getName(),0,"");
        copyChildSubTree(tmp, clipboard);
    }

    private void updateChildSubTree(UINode nodeInBuffer) {
        for(UINode node:nodeInBuffer.getChildSubTree())
        {
            node.setDepth(nodeInBuffer.getDepth()+20);
            node.setParentId(nodeInBuffer.getId());
            new WaitForTree().execute(node);
        }
    }

    private void copyNode(MenuItem item) {
        int position = getPosition(item);
        UINode node = adapter.getNodeArrayList().get(position);
        clipboard=new UINode(node.getName(),0,"");
        copyChildSubTree(node, clipboard);

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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        View view = info.targetView;
        ArrayList<UINode> nodeArrayList = adapter.getNodeArrayList();
        UINode uiNode = nodeArrayList.get(position);
        for (int i = position + 1; i < nodeArrayList.size(); ) {
            if (nodeArrayList.get(i).getDepth() > nodeArrayList.get(position).getDepth()) {
                nodeArrayList.remove(i);
            } else {
                break;
            }
        }
        presenter.deleteNode(uiNode, position);
        nodeArrayList.remove(position);
        //remove from parent childsubtree
        UINode parent = null;
        for (UINode node : nodeArrayList) {
            if (node.getId().equals(uiNode.getParentId())){
                parent = node;
                break;
            }
        }
        parent.removeChild(uiNode);
        adapter.notifyDataSetChanged();
    }

    private void addNewNode(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        View view = info.targetView;
        int childPosition=position;
//        while (++childPosition < nodeArrayList.size() && nodeArrayList.get(childPosition).getDepth() > nodeArrayList.get(position).getDepth()) ;
//        adapter.showInputDialog(position,childPosition);
    }
    private class WaitForTree extends AsyncTask<UINode,Void,UINode>
    {
        @Override
        protected void onPreExecute(){

        }
        @Override
        protected UINode doInBackground(UINode... params) {
            UINode node = params[0];
            node.setId("");
            presenter.addChild(node);
            while (node.getId().equals(""));
            return node;
        }
        @Override
        protected void onPostExecute(UINode result) {
            updateChildSubTree(result);
        }
    }

}
