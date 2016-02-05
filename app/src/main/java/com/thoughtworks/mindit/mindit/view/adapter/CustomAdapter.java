package com.thoughtworks.mindit.mindit.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.constant.Colors;
import com.thoughtworks.mindit.mindit.constant.Constants;
import com.thoughtworks.mindit.mindit.presenter.Presenter;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private final CustomAdapterHelper customAdapterHelper;
    private Context context;
    private ArrayList<UINode> nodeList;
    private LayoutInflater layoutInflater;
    private int newNodePosition = -1;
    private Presenter presenter;
    private int selectedNodePosition = 0;

    public CustomAdapter(Context context, Presenter presenter, ArrayList<UINode> uiNodes) {
        this.context = context;
        this.presenter = presenter;
        this.nodeList = uiNodes;
        customAdapterHelper = new CustomAdapterHelper(this);
        customAdapterHelper.expand(0, nodeList.get(0));
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
    }

    public int getSelectedNodePosition() {
        return selectedNodePosition;
    }

    public void setSelectedNodePosition(int selectedNodePosition) {
        this.selectedNodePosition = selectedNodePosition;
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

    public UINode addChild(int position, UINode parent) {
        return customAdapterHelper.addChild(position, parent);

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

        customAdapterHelper.initializeTextView(nodeHolder, rowView, currentNode);
        //if(nodeHolder.switcher.isShown())
        customAdapterHelper.addPadding(position, rowView);
        customAdapterHelper.setImageForExpandCollapse(nodeHolder, rowView, currentNode);
        customAdapterHelper.setEventToExpandCollapse(position, nodeHolder, currentNode);
        customAdapterHelper.setEventToAddNodeButton(position, nodeHolder, rowView, currentNode);
        rowView.setBackgroundColor(Color.parseColor(Colors.NODE_BACKGROUND));
        if (position == newNodePosition) {
            customAdapterHelper.addNode(nodeHolder, currentNode);
            //nodeHolder.switcher.showPrevious();
        }

        if (selectedNodePosition == position) {
            rowView.setBackgroundColor(Color.parseColor(Colors.NODE_BACKGROUND_ON_SELECTION));
            nodeHolder.textViewForName.setTextColor(Color.parseColor(Colors.NODE_NAME));
            nodeHolder.editText.setTextColor(Color.parseColor(Colors.EDIT_TEXT));
        }

        return rowView;
    }

    public void updateChildSubTree(UINode existingParent) {
        if (nodeList.indexOf(existingParent) != -1) {
            this.collapse(nodeList.indexOf(existingParent), existingParent);
            this.expand(nodeList.indexOf(existingParent), existingParent);
            existingParent.setStatus(Constants.STATUS.EXPAND.toString());
        }
    }
}


