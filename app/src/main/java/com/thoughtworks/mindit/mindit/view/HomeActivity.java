package com.thoughtworks.mindit.mindit.view;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.Tracker;
import com.thoughtworks.mindit.mindit.constant.Colors;
import com.thoughtworks.mindit.mindit.constant.Constants;

public class HomeActivity extends AppCompatActivity {
    private Button importMindmap;
    private Tracker tracker;
    private boolean isNewIntent;
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null)
            return;
        Uri data = intent.getData();
        if (data != null) {
            String[] url = data.toString().split("/");
            if (tracker != null) {
                isNewIntent = true;
                tracker.resetTree();
            }
            tracker = Tracker.getInstance(this, url[url.length - 1]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.mindit_logo);
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String[] url = data.toString().split("/");
            tracker = Tracker.getInstance(this, url[url.length - 1]);
        }

        importMindmap = (Button) findViewById(R.id.importMindmap);
        importMindmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tracker != null)
                    tracker.resetTree();
                importMindMap();

            }
        });
    }

    public void importMindMap() {
        final Dialog importDialog = new Dialog(this);
        importDialog.setTitle("Enter URL");
        importDialog.setContentView(R.layout.import_dialog);
        importDialog.show();
        final EditText editUrl = (EditText) importDialog.findViewById(R.id.editUrl);
        editUrl.setSelection(editUrl.getText().length());
        final Button imports = (Button) importDialog.findViewById(R.id.imports);
        imports.setFocusable(true);
        if (imports.isFocused()) {
            imports.setBackgroundColor(Color.parseColor(Colors.IMPORT_DIALOG_BACKGROUND_COLOR_ON_FOCUS));
        }
        imports.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                imports.setBackgroundColor(Color.parseColor(Colors.IMPORT_DIALOG_BACKGROUND_COLOR_ON_FOCUS_CHANGED));
            }
        });
        imports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String input = editUrl.getText().toString();
                String inputArray[] = input.split("/");
                String url = inputArray[inputArray.length - 1];
                url = url.trim();
                tracker = Tracker.getInstance(HomeActivity.this, url);
                importDialog.dismiss();
            }
        });
        Button cancel = (Button) importDialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//
//            return true;
//        }
        if (id == R.id.imports) {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(tracker != null && isNewIntent == false)
            tracker.resetTree();
    }

}