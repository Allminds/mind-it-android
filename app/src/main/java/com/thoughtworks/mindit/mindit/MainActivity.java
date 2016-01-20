package com.thoughtworks.mindit.mindit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.View;
import android.widget.ProgressBar;

import com.thoughtworks.mindit.mindit.model.Tree;


public class MainActivity extends AppCompatActivity {
    Tracker tracker;
    ProgressBar progressBar;
    Tree tree;
    String rootId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }
    public void imports(View view){
        new WaitForTree().execute();
    }
    private class WaitForTree extends AsyncTask<Void,Void,Integer>
    {
        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected Integer doInBackground(Void... params) {
            rootId = "LLkN4HrcoqtorwkJz";
            tracker = new Tracker(getApplicationContext(), rootId);
            while (tracker.getTree() == null)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            System.out.println("after wait : " + tracker.getTree());
            return 1;
        }
        @Override
        protected void onPostExecute(Integer result) {
            rootId = "LLkN4HrcoqtorwkJz";
            tracker.subscribe(rootId);
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), MindmapActivity.class);
            intent.putExtra("Tracker", tracker);
            startActivity(intent);
        }


    }

}
