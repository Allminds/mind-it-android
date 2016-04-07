package com.thoughtworks.mindit.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.thoughtworks.mindit.Config;
import com.thoughtworks.mindit.NetworkReceiver;
import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.Setting;
import com.thoughtworks.mindit.presenter.Presenter;
import com.thoughtworks.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.view.model.UINode;

import java.util.ArrayList;

public class MindmapActivity extends AppCompatActivity implements IMindmapView {

    public SharedPreferences sharedPreferences;
    boolean menuOptionFlag = true;
    private Menu menu;
    private CustomAdapter adapter;
    private Presenter presenter;
    private UINode clipboard;
    private ArrayList<UINode> nodeList;
    private Toolbar toolbar;
    private NetworkReceiver networkReceiver;
    private ListView listView;
    private String workingMindmapId;

    public ListView getListView() {
        return listView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindmap);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(R.drawable.mindit_logo);
            getSupportActionBar().setTitle(Constants.EMPTY_STRING);
        }
        listView = (ListView) findViewById(R.id.listView);
        registerForContextMenu(listView);
        presenter = new Presenter(this);
        adapter = new CustomAdapter(this, presenter, presenter.buildNodeListFromTree());
        listView.setAdapter(adapter);
        nodeList = adapter.getNodeList();
        networkReceiver = new NetworkReceiver(presenter, adapter);
        showcase();
    }
    private void showcase() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(Setting.SETTING_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (!sharedPreferences.getAll().containsKey(Setting.VERSION_2_FIRST_TIME)) {
                editor.putBoolean(Setting.VERSION_2_FIRST_TIME, true);
                editor.commit();
            }
        }

        if (sharedPreferences.getBoolean(Setting.VERSION_2_FIRST_TIME, false)) {
            final Target homeTarget = new Target() {
                @Override
                public Point getPoint() {
                    int actionBarSize = getSupportActionBar().getHeight();
                    int x = getResources().getDisplayMetrics().widthPixels - actionBarSize / 2 + actionBarSize / 10;
                    int y = actionBarSize - actionBarSize / 10;
                    return new Point(x, y);
                }
            };
            final ShowcaseView showcaseView1 = new ShowcaseView.Builder(this).build();
            showcaseView1.setTarget(homeTarget);
            showcaseView1.setContentTitle("     ADD ");
            showcaseView1.setContentText("     Add child to the selected node on one click.");
            showcaseView1.setStyle(R.style.CustomShowcaseTheme2);
            showcaseView1.setButtonText("Got it..");

            Target homeTarget1 = new Target() {
                @Override
                public Point getPoint() {
                    int actionBarSize = getSupportActionBar().getHeight();
                    int x = getResources().getDisplayMetrics().widthPixels - actionBarSize - actionBarSize / 10;
                    int y = actionBarSize - actionBarSize / 10;
                    return new Point(x, y);
                }
            };
            final ShowcaseView showcaseView2 = new ShowcaseView.Builder(this).build();
            showcaseView2.setTarget(homeTarget1);
            showcaseView2.setContentTitle("     DELETE ");
            showcaseView2.setContentText("     Delete the selected node.");
            showcaseView2.setStyle(R.style.CustomShowcaseTheme2);
            showcaseView2.setButtonText("Got it..");
            showcaseView2.hide();

            Target homeTarget2 = new Target() {
                @Override
                public Point getPoint() {
                    int actionBarSize = getSupportActionBar().getHeight();
                    int x = getResources().getDisplayMetrics().widthPixels / 2 - actionBarSize;
                    int y = actionBarSize / 2 + actionBarSize + actionBarSize / 3;
                    return new Point(x, y);
                }
            };
            final ShowcaseView showcaseView3 = new ShowcaseView.Builder(this).build();
            showcaseView3.setTarget(homeTarget2);
            showcaseView3.setContentTitle("     UPDATE ");
            showcaseView3.setContentText("      Double click on selected node and rename it.");
            showcaseView3.setStyle(R.style.CustomShowcaseTheme2);
            showcaseView3.setButtonText("Got it..");
            showcaseView3.hide();

            showcaseView1.overrideButtonClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showcaseView1.hide();
                    showcaseView2.show();


                }
            });
            showcaseView2.overrideButtonClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showcaseView2.hide();
                    showcaseView3.show();


                }
            });
            showcaseView3.overrideButtonClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showcaseView3.hide();
                    menuOptionFlag = false;
                    invalidateOptionsMenu();
                    Config.SHOULD_NOT_SHOW_TUTORIAL = true;
                    adapter.notifyDataSetChanged();
                }
            });
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(Setting.VERSION_2_FIRST_TIME);
            editor.putBoolean(Setting.VERSION_2_FIRST_TIME, false);
            editor.commit();

        } else {
            Config.SHOULD_NOT_SHOW_TUTORIAL = true;
            menuOptionFlag = false;
            invalidateOptionsMenu();
        }
    }
    @Override
    public void onBackPressed() {
        final AlertDialog.Builder alertExit = new AlertDialog.Builder(this);
        alertExit.setMessage("Do you want to exit?");
        alertExit.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });

        alertExit.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        AlertDialog dialog = alertExit.show();
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        dialog.show();
        messageText.setGravity(Gravity.CENTER);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver.getBroadcastReceiver());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        if (networkReceiver != null) {
            registerReceiver(networkReceiver.getBroadcastReceiver(), filter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.actions, menu);
        if (Config.FEATURE_ADD) {
            MenuItem add = menu.getItem(Constants.ADD);
            add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            add.setEnabled(false);
            add.setVisible(true);
        }
        if (Config.FEATURE_DELETE) {
            MenuItem delete = menu.getItem(Constants.DELETE);
            delete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            delete.setEnabled(false);
            delete.setVisible(true);
        }
        MenuItem info = menu.getItem(Constants.DELETE);
        info.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        info.setEnabled(true);
        info.setVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menuOptionFlag == false) {
            menu.getItem(Constants.ADD).setEnabled(true);
            menu.getItem(Constants.DELETE).setEnabled(true);
            menu.getItem(Constants.INFO).setEnabled(true);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int newSelectionPosition;
        int positionOfSelectedNode = adapter.getSelectedNodePosition();
        switch (item.getItemId()) {
            case R.id.add:
                newSelectionPosition = addNode(positionOfSelectedNode);

                break;
            case R.id.delete:
                newSelectionPosition = deleteSelectedNode(positionOfSelectedNode);
                break;
            case R.id.info:
                showInformationDialog();

            default:
                return true;
        }
        adapter.setSelectedNodePosition(newSelectionPosition);
        toolbar.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        listView.setSelection(adapter.getSelectedNodePosition());
        listView.requestFocus();
        return true;
    }

    private void showInformationDialog() {
        String id = nodeList.get(0).getId();
        final Dialog information;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            information = new Dialog(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            information = new Dialog(this);
        }
        information.setTitle("Mindmap URL");
        information.setContentView(R.layout.info_dialog);
        information.show();
        final EditText url = (EditText) information.findViewById(R.id.url_link);
        url.setText("http://www.mindit.xyz/create/" + id);
        final Button cancelUrlDialog = (Button) information.findViewById(R.id.cancel_url_dialog);
        cancelUrlDialog.setFocusable(true);
        cancelUrlDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                information.dismiss();
            }
        });
        final Button openInBrowser = (Button) information.findViewById(R.id.open_in_browser);
        openInBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = url.getText().toString();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
            }
        });
        final Button copyUrl = (Button) information.findViewById(R.id.copy_url);
        copyUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(url.getText().toString());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", url.getText().toString());
                    clipboard.setPrimaryClip(clip);
                }
                information.dismiss();
                Toast.makeText(getApplicationContext(), "URL copied", Toast.LENGTH_SHORT).show();
            }
        });
        information.show();
    }

    private int deleteSelectedNode(int positionOfSelectedNode) {
        int newSelectionPosition;
        deleteNode(positionOfSelectedNode);
        newSelectionPosition = (positionOfSelectedNode == 0) ? 0 : positionOfSelectedNode - 1;
        return newSelectionPosition;
    }

    private int addNode(int positionOfParent) {
        int newSelectionPosition;
        listView.setSelection(positionOfParent);
        UINode parent = nodeList.get(positionOfParent);
        UINode newNode = adapter.addChild(positionOfParent, parent);
        newSelectionPosition = nodeList.indexOf(newNode);
        return newSelectionPosition;
    }

    private void cutNode(int position) {
        clipboard = nodeList.get(position);
        deleteNode(position);
        clipboard.setId(Constants.EMPTY_STRING);
        clipboard.setStatus(Constants.STATUS.COLLAPSE.toString());
    }

    private void copyNode(int position) {
        UINode node = nodeList.get(position);
        clipboard = new UINode(node.getName(), 0, Constants.EMPTY_STRING);
        copyChildSubTree(node, clipboard);

    }

    private void pasteNode(int position) {

        UINode parent = nodeList.get(position);
        int childPosition = parent.getChildSubTree().size();

        clipboard.setDepth(parent.getDepth() + 20);
        clipboard.setParentId(parent.getId());
        parent.getChildSubTree().add(clipboard);

        new WaitForTree().execute(clipboard);

        if (parent.getStatus().equals(Constants.STATUS.COLLAPSE.toString())) {
            parent.setStatus(Constants.STATUS.EXPAND.toString());
            adapter.expand(position, parent);
        } else
            nodeList.add(childPosition, clipboard);

        UINode temporary = clipboard;
        clipboard = new UINode(clipboard.getName(), 0, Constants.EMPTY_STRING);
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

    private void deleteNode(int position) {
        if (position == 0) {
            Toast.makeText(getApplicationContext(), Constants.ROOT_DELETE_ERROR, Toast.LENGTH_SHORT).show();
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
        presenter.deleteNode(uiNode);

        //remove from parent childsubtree

        UINode parent = null;
        for (UINode node : nodeList) {
            if (node.getId().equals(uiNode.getParentId())) {
                parent = node;
                break;
            }
        }
        boolean result = parent != null && parent.removeChild(uiNode);
        if (result) {
            if (parent.getChildSubTree().size() == 0) {
                parent.setStatus(Constants.STATUS.COLLAPSE.toString());
            }
            nodeList.remove(position);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataChanged() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateChildTree(UINode existingParent) {
        adapter.updateChildSubTree(existingParent);
    }


    private class WaitForTree extends AsyncTask<UINode, Void, UINode> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected UINode doInBackground(UINode... params) {
            UINode node = params[0];
            node.setId(Constants.EMPTY_STRING);
            presenter.addNode(node);
            while (node.getId().equals(Constants.EMPTY_STRING)) ;
            return node;
        }

        @Override
        protected void onPostExecute(UINode result) {
            updateChildTree(result);
        }
    }
}
