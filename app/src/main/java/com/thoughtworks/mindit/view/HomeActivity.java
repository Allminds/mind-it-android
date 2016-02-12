package com.thoughtworks.mindit.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.Tracker;
import com.thoughtworks.mindit.constant.Colors;
import com.thoughtworks.mindit.constant.Constants;

public class HomeActivity extends AppCompatActivity {
    private Tracker tracker;
    private boolean isNewIntent;
    private Dialog importDialog;

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if(importDialog.isShowing())
            importDialog.dismiss();
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
        if(getSupportActionBar() != null) {
            getSupportActionBar().setIcon(R.drawable.mindit_logo);
            getSupportActionBar().setTitle(Constants.EMPTY_STRING);
        }
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String[] url = data.toString().split("/");
            tracker = Tracker.getInstance(this, url[url.length - 1]);
        }
        Button importMindmap = (Button) findViewById(R.id.importMindmap);
        importMindmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importMindMap();

            }
        });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
      //  setContentView(R.layout.activity_home);


    }
    private void importMindMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            importDialog = new Dialog(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            importDialog = new Dialog(this);
        }
        importDialog.setTitle(Constants.IMPORT_DIALOG_TITLE);
        importDialog.setContentView(R.layout.import_dialog);
        importDialog.show();
        final Button imports = (Button) importDialog.findViewById(R.id.imports);
        imports.setFocusable(true);

        final EditText editUrl = (EditText) importDialog.findViewById(R.id.editUrl);
        editUrl.setSelection(editUrl.getText().length());
        editUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    imports.setEnabled(false);
                    imports.setTextColor(Color.parseColor(Colors.IMPORT_BUTTON_TEXT_COLOR_WHEN_DISABLED));
                }
                else {
                    imports.setEnabled(true);
                    imports.setTextColor(Color.parseColor(Colors.IMPORT_BUTTON_TEXT_COLOR_WHEN_ENABLED));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if(editUrl.getText().toString().equals("")) {
            imports.setEnabled(false);
            imports.setTextColor(Color.parseColor(Colors.IMPORT_BUTTON_TEXT_COLOR_WHEN_DISABLED));
        }
        else
        {
            imports.setEnabled(true);
            imports.setTextColor(Color.parseColor("#595858"));
        }
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
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (tracker != null && !isNewIntent)
            tracker.resetTree();
    }

}