package com.thoughtworks.mindit.mindit.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.Tracker;
import com.thoughtworks.mindit.mindit.helper.JsonParserService;

import org.json.JSONException;

public class HomeActivity extends AppCompatActivity {

    Tracker tracker;
    String rootId;
    Button importMindmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        Intent intent = getIntent();
        Uri data;
        data = intent.getData();

        importMindmap = (Button) findViewById(R.id.importMindmap);
        importMindmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importMindMap();

            }
        });
        if (data != null) {
            String[] uri = data.toString().split("/");
            new WaitForTree().execute(uri[uri.length - 1]);
        }


    }

    public void importMindMap() {
        final Dialog importDialog = new Dialog(this);
        importDialog.setTitle("Enter Url");
        importDialog.setContentView(R.layout.import_dialog);
        importDialog.show();
        Button imports = (Button) importDialog.findViewById(R.id.imports);
        imports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editUrl = (EditText) importDialog.findViewById(R.id.editUrl);
                String url = editUrl.getText().toString();
                new WaitForTree().execute(url);
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
        Button paste = (Button) importDialog.findViewById(R.id.paste);
        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager myClipboard;
                myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData abc = myClipboard.getPrimaryClip();
                ClipData.Item item = abc.getItemAt(0);
                String text = item.getText().toString();
                EditText editUrl = (EditText) importDialog.findViewById(R.id.editUrl);
                editUrl.setText(text);
                editUrl.setSelection(editUrl.getText().length());
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
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.imports) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (tracker != null)
            tracker.resetTree();
    }


    private class WaitForTree extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog=new ProgressDialog(HomeActivity.this);
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Checking network connection....");
            progressDialog.show();

        }
        @Override
        protected String doInBackground(String... params) {

            rootId = params[0];
            ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if(activeNetwork != null && activeNetwork.isConnected()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                HomeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage("Downloading mindmap... ");
                    }
                });
                tracker = Tracker.getInstance(getApplicationContext(), rootId);
                while (tracker.getTree() == null){
                    if(JsonParserService.isErrorOccurred())
                        break;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println(rootId + "   " + tracker.getTree());
                return rootId;
            }
            else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String message="Please check your network connection.";
                return message;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if(result.equals("Please check your network connection.")){

              Toast toast=  Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 100);
                toast.show();

            }
            else if(JsonParserService.isErrorOccurred()){
                tracker.resetTree();
                JsonParserService.resetErrorOccurredFlag();
                final AlertDialog alertDialog=new AlertDialog.Builder(HomeActivity.this).create();
                alertDialog.setMessage("Oops!!!!!\n" + "Looks like you were led astray with an incorrect URL..");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }

            else{
                rootId = result;
                Intent intent = new Intent(getApplicationContext(), MindmapActivity.class);
                startActivity(intent);
            }

        }
    }
}
