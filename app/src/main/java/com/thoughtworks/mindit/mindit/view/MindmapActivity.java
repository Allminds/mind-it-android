package com.thoughtworks.mindit.mindit.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.thoughtworks.mindit.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.view.model.UINode;
import com.thoughtworks.mindit.mindit.presenter.Presenter;

import java.util.ArrayList;

public class MindmapActivity extends AppCompatActivity {

    ListView listView;
    CustomAdapter adapter;
    Presenter presenter;

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
        menu.add(0, v.getId(), 0, "Add");
        menu.add(0, v.getId(), 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle()=="Add"){
            addNewNode(item);
        }
        else if(item.getTitle()=="Delete"){
            deleteNode(item);
        }else{
            return false;
        }
        return true;
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
        System.out.println("ct:- "+parent.getChildSubTree());
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

}
