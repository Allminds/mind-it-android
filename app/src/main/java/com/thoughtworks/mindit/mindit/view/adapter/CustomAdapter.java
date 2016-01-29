package com.thoughtworks.mindit.mindit.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.view.model.UINode;
import com.thoughtworks.mindit.mindit.presenter.Presenter;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private final int deviceHeight;
    private final CustomAdapterHelper customAdapterHelper;
    private Context context;
    private ArrayList<UINode> nodeList;
    private LayoutInflater layoutInflater;
    private int newNodePosition = -1;
    private Presenter presenter;


    public CustomAdapter(Context context, Presenter presenter) {
        this.context = context;
        this.presenter = presenter;
        this.nodeList = presenter.buildNodeListFromTree();
        customAdapterHelper = new CustomAdapterHelper(this);
        customAdapterHelper.expand(0, nodeList.get(0));
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        deviceHeight = size.y;
    }

    public int getDeviceHeight() {
        return deviceHeight;
    }

    public Context getContext() {
        return context;
    }

    public ArrayList<UINode> getNodeList() {
        return nodeList;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    public void expand(int position, UINode currentNode) {
        customAdapterHelper.expand(position, currentNode);
    }

    public void collapse(int position, UINode currentNode) {
        customAdapterHelper.collapse(position, currentNode);
    }

    public void setNewNodePosition(int newNodePosition) {
        this.newNodePosition = newNodePosition;
    }

    public void resetNewNodePosition() {
        this.newNodePosition = -1;
    }

    @Override
    public int getCount() {
        return nodeList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final NodeHolder nodeHolder = new NodeHolder();
        final UINode currentNode = nodeList.get(position);
        final View rowView = layoutInflater.inflate(R.layout.layout_node, null);

        nodeHolder.editText = (EditText) rowView.findViewById(R.id.editText);
        nodeHolder.editText.setVisibility(View.GONE);

        customAdapterHelper.initializeTextView(nodeHolder, rowView, currentNode);
        customAdapterHelper.addPadding(position, rowView);
        customAdapterHelper.setImageForExpandCollapse(nodeHolder, rowView, currentNode);
        customAdapterHelper.setEventToExpandCollapse(position, nodeHolder, currentNode);
        customAdapterHelper.setEventToAddNodeButton(position, nodeHolder, rowView, currentNode);

        if (position == newNodePosition)
            customAdapterHelper.addNode(nodeHolder, currentNode);

        return rowView;
    }
}
