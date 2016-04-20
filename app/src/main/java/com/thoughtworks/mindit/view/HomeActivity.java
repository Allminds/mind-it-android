package com.thoughtworks.mindit.view;

import android.app.AlertDialog;
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
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.pkmmte.view.CircularImageView;
import com.thoughtworks.mindit.Config;
import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.Tracker;
import com.thoughtworks.mindit.authentication.GoogleAuth;
import com.thoughtworks.mindit.authentication.MindmapRequest;
import com.thoughtworks.mindit.authentication.OnAuthenticationChanged;
import com.thoughtworks.mindit.authentication.User;
import com.thoughtworks.mindit.constant.Colors;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.MindIt;
import com.thoughtworks.mindit.constant.NetworkMessage;
import com.thoughtworks.mindit.constant.Operation;
import com.thoughtworks.mindit.constant.Setting;
import com.thoughtworks.mindit.helper.JsonParserService;
import com.thoughtworks.mindit.helper.MindmapsLoader;
import com.thoughtworks.mindit.helper.OnMindmapOpenRequest;
import com.thoughtworks.mindit.helper.OnMindmapsLoaded;
import com.thoughtworks.mindit.model.Node;
import com.thoughtworks.mindit.view.adapter.AllMindmapsAdapter;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnAuthenticationChanged, OnMindmapOpenRequest,
        SwipeRefreshLayout.OnRefreshListener, OnMindmapsLoaded {
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    public SharedPreferences sharedPreferences;
    FloatingActionMenu floatingActionMenu;
    private Tracker tracker;
    private boolean isNewIntent;
    private AllMindmapsAdapter allMindmapsAdapter;
    private Dialog importDialog;
    private String root;
    private GoogleAuth googleAuth;
    private Bitmap bitmap;
    private ProgressDialog pDialog;
    private NavigationView navigationView;
    private MindmapRequest mindmapRequest;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private ViewSwitcher switcher;
    private AlertDialog mindmapsLoader;

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (importDialog != null && importDialog.isShowing()) {
            importDialog.dismiss();
        }
        Uri data = intent.getData();
        if (data != null) {
            String[] url = data.toString().split("/");
            if (tracker != null) {
                tracker.resetTree();
                isNewIntent = true;
            }
            String mindmapId = url[url.length - 1];
            openMindmapById(mindmapId, Operation.OPEN);
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
            String[] link = data.toString().split("/");
            String url = link[link.length - 1];
            openMindmapById(url, Operation.OPEN);
        }
        googleAuth = new GoogleAuth((AppCompatActivity) this, (Context) this);
        switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher_signed_in);
        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);

        FloatingActionButton createButton = (FloatingActionButton) findViewById(R.id.menu_item_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
                createMindmap();
            }
        });
        FloatingActionButton importButton = (FloatingActionButton) findViewById(R.id.menu_item_import);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);

                importMindMap();
            }
        });
        if (Config.FEATURE_DASHBOARD) {
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(this);
            LinearLayout dashboardLayout = (LinearLayout) findViewById(R.id.dashboard_layout);
            listView = (ListView) dashboardLayout.findViewById(R.id.listView_root_nodes);
            ArrayList<Node> rootNodes = new ArrayList<Node>();
            allMindmapsAdapter = new AllMindmapsAdapter(HomeActivity.this, rootNodes);
            listView.setAdapter(allMindmapsAdapter);
            mindmapsLoader = new SpotsDialog(HomeActivity.this, NetworkMessage.DOWNLOAD);
            mindmapsLoader.setTitle(MindIt.LOADING_TITLE);
            mindmapsLoader.setMessage(MindIt.LOADING_MESSAGE);
            if (!googleAuth.isSignedIn()) {
                switcher.showNext();
                floatingActionMenu.hideMenu(false);
            } else {
                floatingActionMenu.showMenu(true);
                mindmapsLoader.show();
                loadMindmaps(googleAuth.getUser().getEmail());
            }
        }
        Button importMindmap = (Button) findViewById(R.id.importMindmap);
        importMindmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importMindMap();
            }
        });
        final Button createMindmap = (Button) findViewById(R.id.createMindmap);
        createMindmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMindmap();
            }
        });
        if (sharedPreferences == null) {
            addVersionSettings();
        }
    }

    private void loadMindmaps(String email) {
        if (email != null) {
            MindmapsLoader.loadMindmaps(HomeActivity.this, email);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            googleAuth.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == MindIt.RC_ROOT_NODE) {
            Node node = JsonParserService.parseNode(data.getStringExtra("node"));
            allMindmapsAdapter.addNodeToDashBoard(node);
            allMindmapsAdapter.notifyDataSetChanged();
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
        if (googleAuth.isSignedIn()) {
            setUserProfile(googleAuth.getUser());
        } else {
            setDefaultProfile();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
            if (loginLogout.getTitle().toString().equalsIgnoreCase("login")) {
                googleAuth.signIn();
            } else {
                googleAuth.signOut();
            }
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

    private void showAllMindmaps() {
        if (tracker != null) {

        }
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
                openMindmapById(url, Operation.OPEN);
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

    private void createMindmap() {
        openMindmapById("", Operation.CREATE);
    }

    private void openMindmapById(String url, Operation operation) {
        if (tracker != null) {
            tracker.resetTree();
        }
        tracker = Tracker.getInstance(HomeActivity.this, url, operation);
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
        Toast.makeText(getApplicationContext(), MindIt.SIGN_IN_AS + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        setUserProfile(googleAuth.getUser());
        if (googleAuth.getUser().getPhotoUrl() != null) {
            new LoadImage().execute(user.getPhotoUrl());
        } else {
            Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile);
            setUserProfilePhoto(defaultIcon);
        }
        if (this.mindmapRequest != null && !this.mindmapRequest.isResponded()) {


            this.mindmapRequest.setResponded(true);
            openMindmapById(mindmapRequest.getId(), Operation.OPEN);
        }
        if (Config.FEATURE_DASHBOARD) {
            mindmapsLoader.show();
            switcher.showPrevious();
            floatingActionMenu.showMenu(true);
            MindmapsLoader.loadMindmaps(HomeActivity.this, user.getEmail());
        }
    }

    @Override
    public void onSignedOut() {
        Toast.makeText(getApplicationContext(), MindIt.SIGNED_OUT_MESSAGE, Toast.LENGTH_SHORT).show();
        setDefaultProfile();
        if (Config.FEATURE_DASHBOARD) {
            switcher.showNext();
            floatingActionMenu.hideMenu(false);
            allMindmapsAdapter.setData(new ArrayList<Node>());
        }
    }

    @Override
    public void onRevokedAccess() {
        setDefaultProfile();
    }

    @Override
    public void onSignInRequest(MindmapRequest mindmapRequest) {
        this.mindmapRequest = mindmapRequest;
        googleAuth.signIn();
    }

    private void setUserProfilePhoto(Bitmap image) {
        CircularImageView circularImageView = (CircularImageView) findViewById(R.id.circular_profile_photo);
        circularImageView.setImageBitmap(image);
        circularImageView.setBorderWidth(1);
        circularImageView.addShadow();
    }

    public void setUserProfile(User user) {
        TextView userName = (TextView) findViewById(R.id.username);
        userName.setText(user.getDisplayName());
        TextView emailId = (TextView) findViewById(R.id.email_id);
        emailId.setText(user.getEmail());
        Menu menu = navigationView.getMenu();
        MenuItem loginLogout = menu.getItem(0);
        loginLogout.setTitle("Logout");
        loginLogout.setIcon(R.drawable.logout);
    }

    private void setDefaultProfile() {
        TextView userName = (TextView) findViewById(R.id.username);
        userName.setText(MindIt.DEFAULT_USER_NAME);
        TextView emailId = (TextView) findViewById(R.id.email_id);
        emailId.setText(MindIt.DEFAULT_EMAIL_ID);
        Menu menu = navigationView.getMenu();
        MenuItem loginLogout = menu.getItem(0);
        loginLogout.setTitle("Login");
        loginLogout.setIcon(R.drawable.login);
        CircularImageView circularImageView = (CircularImageView) findViewById(R.id.circular_profile_photo);
        circularImageView.setImageResource(R.drawable.circular_icon);
        circularImageView.setBorderWidth(0);
        circularImageView.addShadow();
    }

    @Override
    public void OnMindmapOpenRequest(String mindmapId) {
        openMindmapById(mindmapId, Operation.OPEN);
    }

    @Override
    public void onRefresh() {
        loadMindmaps(googleAuth.getUser().getEmail());
        swipeRefreshLayout.setRefreshing(true);

    }

    @Override
    public void onMindmapsLoaded(ArrayList<Node> rootNodes) {
        if (rootNodes != null ){
            TextView noMindmapsMessage = (TextView) findViewById(R.id.no_mindmap_message);
            if (rootNodes.size() == 0){
                noMindmapsMessage.setText(Constants.NO_MINDMAPS_MESSAGE);
            }
            else {
                noMindmapsMessage.setText(Constants.EMPTY_STRING);
            }
        }
            allMindmapsAdapter.setData(rootNodes);
        allMindmapsAdapter.notifyDataSetChanged();
        if (mindmapsLoader.isShowing()) {
            mindmapsLoader.dismiss();
        }
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onLoadingError(Error error) {
        if (mindmapsLoader.isShowing()) {
            mindmapsLoader.dismiss();
        }
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomeActivity.this);
            pDialog.setMessage("Loading Image ....");

        }

        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
                Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile);
                return null;
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
            setUserProfilePhoto(image);
        }
    }

}
