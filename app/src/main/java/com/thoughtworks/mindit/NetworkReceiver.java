package com.thoughtworks.mindit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.thoughtworks.mindit.presenter.Presenter;
import com.thoughtworks.mindit.view.MindmapActivity;
import com.thoughtworks.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.view.model.UINode;

import java.util.ArrayList;

public class NetworkReceiver {
private BroadcastReceiver broadcastReceiver;
    private Tracker tracker;
    private boolean networkFlag = false;
    private Presenter presenter;
    private CustomAdapter adapter;

    public NetworkReceiver(Presenter presenter, CustomAdapter adapter) {
        this.presenter = presenter;
        this.adapter=adapter;
        this.tracker = Tracker.getInstance();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                final String rootId;
                rootId = tracker.getRootId();

                if (intent.getExtras() != null) {
                    NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (ni != null && ni.getState() == NetworkInfo.State.DISCONNECTED && tracker != null) {

                        doWhenDisconnected(context);

                    }
                    if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED && networkFlag) {
                        doWhenConnected(context, rootId);
                    }
                }
            }
        };
    }
    public BroadcastReceiver getBroadcastReceiver() {
        return broadcastReceiver;
    }

    private void doWhenConnected(final Context context, final String rootId) {
        final Thread inConnected = new Thread(new Runnable() {
            @Override
            public void run() {
                Tracker.getInstance().resetTree();
                Tracker instance = Tracker.getInstance(context, rootId);

                while (instance.getTree() == null) {
                    if (!instance.isConnected())
                        instance = Tracker.getInstance(context, rootId);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                presenter.registerForTree();
                ArrayList<UINode> nodes = presenter.buildNodeListFromTree();
                UINode root = nodes.get(0);
                adapter.getNodeList().clear();
                adapter.getNodeList().add(root);
                adapter.updateNodeList(root);
                ((MindmapActivity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        inConnected.start();
        networkFlag =false;
        Toast.makeText(context, "Connected to network", Toast.LENGTH_SHORT).show();

    }

    private void doWhenDisconnected(Context context) {
        networkFlag = true;
        Toast.makeText(context, "No Network connection", Toast.LENGTH_SHORT).show();
    }
}