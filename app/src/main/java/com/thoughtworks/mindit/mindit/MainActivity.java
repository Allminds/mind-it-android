package com.thoughtworks.mindit.mindit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.View;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;


import com.thoughtworks.mindit.mindit.presenter.Presenter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<UINode>nodeArrayList;
    ListView listView;
    CustomAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        nodeArrayList=new ArrayList<>();
      //  UINode node=new UINode("Root",0);
       // nodeArrayList.add(node);
        listView=(ListView)findViewById(R.id.listView);
        registerForContextMenu(listView);
        Presenter presenter =new Presenter(nodeArrayList);
        adapter=new CustomAdapter(this, presenter);
        listView.setAdapter(adapter);

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
        for (int i = position + 1; i < nodeArrayList.size(); ) {
            if (nodeArrayList.get(i).getDepth() > nodeArrayList.get(position).getDepth()) {
                nodeArrayList.remove(i);
            } else {

                break;
            }
        }
        nodeArrayList.remove(position);
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
