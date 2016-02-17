package com.thoughtworks.mindit.view.adapter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.constant.Colors;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.UpdateOption;
import com.thoughtworks.mindit.presenter.Presenter;
import com.thoughtworks.mindit.view.model.UINode;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class CustomAdapter extends BaseAdapter {
    private final CustomAdapterHelper customAdapterHelper;
    private Context context;
    private ArrayList<UINode> nodeList;
    private LayoutInflater layoutInflater;
    private int separatorPosition = -1;
    private int workingNodePosition = -1;
    private Presenter presenter;
    private int selectedNodePosition = 0;
    private UINode selectedNode=null;
    private ArrayList<String> descendents=new ArrayList<>();
    private String operation = UpdateOption.ADD;

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

    public int getWorkingNodePosition() {
        return workingNodePosition;
    }

    public void setWorkingNodePosition(int workingNodePosition) {
        this.workingNodePosition = workingNodePosition;
    }

    public int getSelectedNodePosition() {
        return selectedNodePosition;
    }

    public void setSelectedNodePosition(int selectedNodePosition) {
        if(this.selectedNodePosition!=selectedNodePosition)
            descendents.clear();
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

    public void resetWorkingNodePosition() {
        this.workingNodePosition = -1;
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
        nodeHolder.separator = (LinearLayout) rowView.findViewById(R.id.separator);
        nodeHolder.separator.setVisibility(View.GONE);

        nodeHolder.verticalLine=(LinearLayout)rowView.findViewById(R.id.verticle_line);

        customAdapterHelper.initializeTextView(nodeHolder, rowView, currentNode);
        customAdapterHelper.setImageForExpandCollapse(nodeHolder, rowView, currentNode);
        customAdapterHelper.setEventToExpandCollapse(position, nodeHolder, currentNode);

        rowView.setBackgroundColor(Color.parseColor(Colors.NODE_BACKGROUND));
        if (position == workingNodePosition) {

                customAdapterHelper.doOperation(nodeHolder, currentNode,operation);
        }
        if (position == 0) {
            nodeHolder.expandCollapseButton.setVisibility(View.INVISIBLE);
            Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), Constants.FONT_SERIF);
            nodeHolder.textViewForName.setTypeface(myTypeface);
            nodeHolder.textViewForName.setTextSize(18);


        }
        this.setSeparatorPosition();
        if (position == separatorPosition) {
            nodeHolder.separator.setVisibility(View.VISIBLE);
            nodeHolder.separator.setBackgroundColor(Color.parseColor(Colors.SEPARATOR_COLOR));
            resetSeparatorPosition();
        }
        if (selectedNodePosition == position) {
            selectedNode=currentNode;
            if(currentNode.getStatus().equals(Constants.STATUS.EXPAND.toString())&&position!=0)
                 descendents=currentNode.expandedChildrenCount(descendents);
            rowView.setBackgroundColor(Color.parseColor(Colors.NODE_BACKGROUND_ON_SELECTION));
            nodeHolder.verticalLine.setBackgroundColor(Color.parseColor(Colors.NODE_BACKGROUND_ON_SELECTION));
            nodeHolder.textViewForName.setTextColor(Color.parseColor(Colors.SELECTED_NODE_TEXT_COLOR));
            nodeHolder.editText.setTextColor(Color.parseColor(Colors.SELECTED_NODE_TEXT_COLOR));
            if (currentNode.getChildSubTree().size() == 0) {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.selected_leaf);
            } else if (currentNode.getStatus().equalsIgnoreCase(Constants.STATUS.EXPAND.toString())) {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.selected_expand);
            } else {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.selected_collapse);
            }

        }
        customAdapterHelper.addPadding(position, rowView,nodeHolder,selectedNode);

        if(descendents.contains(currentNode.getId())){
            nodeHolder.verticalLine.setBackgroundColor(Color.parseColor("#FCAA35"));
                nodeHolder.verticalLine.setVisibility(View.VISIBLE);
        }

        return rowView;
    }

    public void updateChildSubTree(UINode existingParent) {
        UINode uiNode = nodeList.get(this.getSelectedNodePosition());

        if (nodeList.indexOf(existingParent) != -1)
            existingParent.setStatus(Constants.STATUS.EXPAND.toString());
        UINode root = nodeList.get(0);
        nodeList.clear();
        nodeList.add(root);
        updateNodeList(root);
        if(nodeList.indexOf(uiNode)!=-1){

            this.setSelectedNodePosition(nodeList.indexOf(uiNode));
            this.setWorkingNodePosition(nodeList.indexOf(uiNode));
        }
        else
            this.setSelectedNodePosition(this.getSelectedNodePosition()-1);
    }

    public void updateNodeList(UINode root) {
        for (UINode child : root.getChildSubTree() ) {
            nodeList.add(child);
            if(child.getStatus().equals(Constants.STATUS.EXPAND.toString()))
            {
                updateNodeList(child);
            }
        }
    }

    private void setSeparatorPosition() {
        UINode leftFirstUINode = presenter.getLeftFirstNode();
        if (leftFirstUINode == null)
            separatorPosition = 0;
        else
            separatorPosition = nodeList.indexOf(leftFirstUINode) - 1;
    }

    private void resetSeparatorPosition() {
        separatorPosition = -1;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}


