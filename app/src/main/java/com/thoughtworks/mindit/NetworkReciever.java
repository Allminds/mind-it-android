package com.thoughtworks.mindit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.presenter.Presenter;
import com.thoughtworks.mindit.view.IMindmapView;
import com.thoughtworks.mindit.view.MindmapActivity;
import com.thoughtworks.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.view.model.UINode;

import java.util.ArrayList;

public class NetworkReciever {



    private BroadcastReceiver broadcastReceiver;
    private Tracker tracker;
    private boolean networkflag = false;
    private Presenter presenter;
    private CustomAdapter adapter;

    public NetworkReciever(Presenter presenter, CustomAdapter adapter) {
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
                    if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED && networkflag == true) {
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
        System.out.println("*********on connected");
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
                presenter.registerforTree();
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
        networkflag=false;
        Toast.makeText(context, "please refresh...", Toast.LENGTH_SHORT).show();

    }

    private void updateList(UINode parent, ArrayList<UINode> nodes) {
        System.out.println("In updateList" + parent.getName());
        for (UINode uiNode :
                parent.getChildSubTree()) {
            nodes.add(uiNode);
            if (uiNode.getStatus().equals(Constants.STATUS.EXPAND.toString())) {
                updateList(uiNode, nodes);
            }
//            updateList(uiNode, nodes);
        }
    }

    private void doWhenDisconnected(Context context) {
        System.out.println("*********on disconnected");
        networkflag = true;
        Toast.makeText(context, "No Network connection", Toast.LENGTH_SHORT).show();
    }
}