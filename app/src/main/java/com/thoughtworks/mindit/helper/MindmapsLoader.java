package com.thoughtworks.mindit.helper;

import android.content.Context;
import android.widget.Toast;

import com.thoughtworks.mindit.constant.MindIt;
import com.thoughtworks.mindit.model.Node;

import java.util.ArrayList;

import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;

public class MindmapsLoader {
    public static void loadMindmaps(final Context context, final String emailId) {
        final OnMindmapsLoaded loader = (OnMindmapsLoaded) context;
        final im.delight.android.ddp.Meteor meteor = new im.delight.android.ddp.Meteor(context, MindIt.WEB_SOCKET);
        meteor.setCallback(new MeteorCallback() {
            @Override
            public void onConnect(boolean b) {
                meteor.call("myRootNodes", new String[]{emailId}, new ResultListener() {
                    @Override
                    public void onSuccess(String jsonNodes) {
                        ArrayList<Node> nodes = JsonParserService.parseToNodeList(jsonNodes);
                        loader.onMindmapsLoaded(nodes);
                        meteor.disconnect();
                    }

                    @Override
                    public void onError(String s, String s1, String s2) {
                        Toast.makeText(context, "Error Occured while getting all mindmaps", Toast.LENGTH_SHORT).show();
                        meteor.disconnect();
                        Error error = new Error("Loading Error: "+s+" "+s1+" "+s2);
                        loader.onLoadingError(error);
                    }
                });
            }

            @Override
            public void onDisconnect() {
            }

            @Override
            public void onDataAdded(String s, String s1, String s2) {

            }

            @Override
            public void onDataChanged(String s, String s1, String s2, String s3) {

            }

            @Override
            public void onDataRemoved(String s, String s1) {

            }

            @Override
            public void onException(Exception e) {

            }
        });

    }
}
