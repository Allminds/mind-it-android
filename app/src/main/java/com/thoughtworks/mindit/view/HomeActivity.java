package com.thoughtworks.mindit.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.Tracker;
import com.thoughtworks.mindit.authentication.GoogleAuth;
import com.thoughtworks.mindit.authentication.OnAuthenticationChanged;
import com.thoughtworks.mindit.authentication.User;
import com.thoughtworks.mindit.constant.Colors;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.Operation;
import com.thoughtworks.mindit.constant.Setting;

import java.io.InputStream;
import java.net.URL;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnAuthenticationChanged {
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    public SharedPreferences sharedPreferences;
    private Tracker tracker;
    private boolean isNewIntent;
    private Dialog importDialog;
    private String root;
    private GoogleAuth googleAuth;
    private Bitmap bitmap;
    private ProgressDialog pDialog;
    private NavigationView navigationView;
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (importDialog != null && importDialog.isShowing())
            importDialog.dismiss();
        Uri data = intent.getData();
        if (data != null) {
            String[] url = data.toString().split("/");
            if (tracker != null) {
                isNewIntent = true;
                tracker.resetTree();
            }
            tracker = Tracker.getInstance(this, url[url.length - 1], Operation.OPEN);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(R.drawable.mindit_logo);
            getSupportActionBar().setTitle(Constants.EMPTY_STRING);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String[] url = data.toString().split("/");
            tracker = Tracker.getInstance(this, url[url.length - 1], Operation.OPEN);
        }
        Button importMindmap = (Button) findViewById(R.id.importMindmap);
        importMindmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importMindMap();
            }
        });
        if (sharedPreferences == null) {
            addVersionSettings();
        }
        final Button createMindmap = (Button) findViewById(R.id.createMindmap);
        createMindmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMindmap();
            }
        });
        googleAuth = new GoogleAuth((AppCompatActivity) this, (Context) this);

    }

    private void createMindmap() {
        if (tracker != null) {
            isNewIntent = true;
            tracker.resetTree();
        }
        tracker = Tracker.getInstance(this, "", Operation.CREATE);
    }

    private void addVersionSettings() {
        sharedPreferences = getSharedPreferences(Setting.SETTING_PREFERENCES, Context.MODE_PRIVATE);
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionCode = packageInfo.versionCode;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.getAll().containsKey(Setting.VERSION_CODE)) {
            editor.putInt(Setting.VERSION_CODE, versionCode);
            editor.commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
                } else {
                    imports.setEnabled(true);
                    imports.setTextColor(Color.parseColor(Colors.IMPORT_BUTTON_TEXT_COLOR_WHEN_ENABLED));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        if (editUrl.getText().toString().equals("")) {
            imports.setEnabled(false);
            imports.setTextColor(Color.parseColor(Colors.IMPORT_BUTTON_TEXT_COLOR_WHEN_DISABLED));
        } else {
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
                if (tracker != null) {
                    tracker.resetTree();
                }
                tracker = Tracker.getInstance(HomeActivity.this, url, Operation.OPEN);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            googleAuth.onActivityResult(requestCode, resultCode, data);
        }
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
        if (googleAuth.isSignedIn()){
            setUserProfile(googleAuth.getUser());
        }
        else {
            setDefaultProfile();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.login_logout) {
            Menu menu = navigationView.getMenu();
            MenuItem loginLogout = menu.getItem(0);
            if(loginLogout.getTitle().toString().equalsIgnoreCase("login")){
                googleAuth.signIn();
            }
            else {
                googleAuth.signOut();
            }
        } else if (id == R.id.nav_gallery) {
        } else if (id == R.id.nav_slideshow) {
            googleAuth.revokeAccess();
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (tracker != null && !isNewIntent)
            tracker.resetTree();
    }

    public void setRoot(String root) {
        this.root = root;
        if (tracker != null) {
            isNewIntent = true;
            tracker.resetTree();
        }
        tracker = Tracker.getInstance(this, root, Operation.OPEN);
    }

    @Override
    public void onSignedIn(User user) {
        Toast.makeText(getApplicationContext(), "Signed In As " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        setUserProfile(googleAuth.getUser());
        new LoadImage().execute(user.getPhotoUrl());
    }

    @Override
    public void onSignedOut() {
        setDefaultProfile();
    }

    private void setDefaultProfile() {
        TextView userName = (TextView) findViewById(R.id.username);
        userName.setText("MindIt");
        TextView emailId = (TextView) findViewById(R.id.email_id);
        emailId.setText("allminds@mindit.com");
        Menu menu = navigationView.getMenu();
        MenuItem loginLogout = menu.getItem(0);
        loginLogout.setTitle("Login");
    }

    @Override
    public void onRevokedAccess() {
        setDefaultProfile();
    }


    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomeActivity.this);
            pDialog.setMessage("Loading Image ....");
            // pDialog.show();

        }

        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
        }
    }
    public void setUserProfile(User user) {
        TextView userName = (TextView) findViewById(R.id.username);
        userName.setText(user.getDisplayName());
        TextView emailId = (TextView) findViewById(R.id.email_id);
        emailId.setText(user.getEmail());
        Menu menu = navigationView.getMenu();
        MenuItem loginLogout = menu.getItem(0);
        loginLogout.setTitle("Logout");
        Toast.makeText(getApplicationContext(),""+ loginLogout.getTitle(),Toast.LENGTH_SHORT).show();
    }
}
