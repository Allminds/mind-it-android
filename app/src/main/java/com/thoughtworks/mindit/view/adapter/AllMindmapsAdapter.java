package com.thoughtworks.mindit.view.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.helper.OnMindmapOpenRequest;
import com.thoughtworks.mindit.model.Node;
import com.thoughtworks.mindit.view.HomeActivity;

import java.util.ArrayList;

public class AllMindmapsAdapter extends BaseAdapter {
    ArrayList<Node> rootNodes;
    private Context context;

    public AllMindmapsAdapter(HomeActivity context, ArrayList<Node> rootNodes) {
        this.rootNodes = rootNodes;
        this.context = context;
    }

    @Override
    public int getCount() {
        return rootNodes.size();
    }

    @Override
    public Object getItem(int position) {
        return rootNodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        holder = new ViewHolder();
        LayoutInflater inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.root_node, null);
        CardView cardView = (CardView) convertView.findViewById(R.id.card_view_root_node);
        holder.mindmapName = (TextView) cardView.findViewById(R.id.root_node_id);
        holder.mindmapName.setText(rootNodes.get(position).getName());
        holder.mindmapName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnMindmapOpenRequest request = (OnMindmapOpenRequest) context;
                request.OnMindmapOpenRequest(rootNodes.get(position).getId());
            }
        });
        return convertView;
    }

    private void openMindMap(String id) {
        OnMindmapOpenRequest request = (OnMindmapOpenRequest) context;
        request.OnMindmapOpenRequest(id);
    }

    public void setData(ArrayList<Node> data) {
        this.rootNodes = data;
    }

    public void addNodeToDashBoard(Node newNode) {
        for (Node node : rootNodes) {
            if (node.getId().equals(newNode.getId())) {
                node.setName(newNode.getName());
                return;
            }
        }
        this.rootNodes.add(0, newNode);
    }

    private boolean isExist(Node newNode) {
        for (Node node : rootNodes) {
            if (node.getId().equals(newNode.getId())) {
                node.setName(newNode.getName());
                return true;
            }
        }
        return false;
    }

    public static class ViewHolder {
        TextView mindmapName;
    }
}
