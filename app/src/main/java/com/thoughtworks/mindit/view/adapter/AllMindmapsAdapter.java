package com.thoughtworks.mindit.view.adapter;

import android.content.Context;
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
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.root_node, null);
        }
        holder.mindmapName = (TextView) convertView.findViewById(R.id.root_node_id);
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

    public static class ViewHolder {
        TextView mindmapName;
    }
}
