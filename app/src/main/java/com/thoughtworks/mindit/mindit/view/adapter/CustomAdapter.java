package com.thoughtworks.mindit.mindit.view.adapter;

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
    private  int seperatorPosition =0;

    public int getNewNodePosition() {
        return newNodePosition;
    }

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
        if(nodeList.size() <= position)
        {
            System.out.println("In CustomAdapter:"+nodeList);
        }
        final UINode currentNode = nodeList.get(position);
        final View rowView = layoutInflater.inflate(R.layout.layout_node, null);
        nodeHolder.seperator=(LinearLayout)rowView.findViewById(R.id.seperator);
        customAdapterHelper.initializeTextView(nodeHolder, rowView, currentNode);
        customAdapterHelper.addPadding(position, rowView);
        customAdapterHelper.setImageForExpandCollapse(nodeHolder, rowView, currentNode);
        customAdapterHelper.setEventToExpandCollapse(position, nodeHolder, currentNode);
       rowView.setBackgroundColor(Color.parseColor(Colors.NODE_BACKGROUND));
        if (position == newNodePosition) {
            customAdapterHelper.addNode(nodeHolder, currentNode);
        }
        if(position==0){
            nodeHolder.expandCollapseButton.setVisibility(View.INVISIBLE);
            Typeface myTypeface=Typeface.createFromAsset(context.getAssets(), "DroidSerif-Bold.ttf");
            nodeHolder.textViewForName.setTypeface(myTypeface);
            nodeHolder.textViewForName.setTextSize(18);


        }
        this.setSeperatosition();
        if(position== seperatorPosition) {
            nodeHolder.seperator.setBackgroundColor(Color.parseColor(Colors.EDIT_TEXT));
            resetSeperatorPosition();
        }
        if (selectedNodePosition == position) {
            rowView.setBackgroundColor(Color.parseColor(Colors.NODE_BACKGROUND_ON_SELECTION));
            nodeHolder.textViewForName.setTextColor(Color.parseColor("#000000"));
            nodeHolder.editText.setTextColor(Color.parseColor("#000000"));
            if (currentNode.getChildSubTree().size() == 0) {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.selected_leaf);
            } else if (currentNode.getStatus().equalsIgnoreCase(Constants.STATUS.EXPAND.toString())) {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.selected_expand);
            } else {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.selected_collapse);
            }

        }

        return rowView;
    }
    public void updateChildSubTree(UINode existingParent) {
        if (nodeList.indexOf(existingParent) != -1) {
//            this.collapse(nodeList.indexOf(existingParent), existingParent);
//            this.expand(nodeList.indexOf(existingParent), existingParent);
            ArrayList<UINode> expandedChildSubTree = new ArrayList<UINode>();
            existingParent.getAllExpandedChildren(expandedChildSubTree);
            int childPosition = nodeList.indexOf(existingParent) + 1;
            for(int i = nodeList.indexOf(existingParent) +1 ;i < nodeList.size() && existingParent.getDepth() < nodeList.get(i).getDepth();)
                nodeList.remove(i);
            for (UINode child : expandedChildSubTree) {
                nodeList.add(childPosition, child);
                childPosition++;

            }
            existingParent.setStatus(Constants.STATUS.EXPAND.toString());
        }
    }

    public void setSeperatosition(){
        UINode leftFirstUINode =presenter.getLeftfirstNode();
        for (int i = 0; i <nodeList.size() ; i++) {
            if(nodeList.get(i).equals(leftFirstUINode)){
                seperatorPosition =i-1;
                break;
            }
        }

    }

    public void resetSeperatorPosition(){
        seperatorPosition =0;
    }
}


