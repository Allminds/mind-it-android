package com.thoughtworks.mindit.mindit.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.view.model.UINode;
import com.thoughtworks.mindit.mindit.presenter.Presenter;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private final int deviceWidth;
    private final int deviceHeight;
    private Context context;

    public ArrayList<UINode> getNodeArrayList() {
        return nodeArrayList;
    }

    private ArrayList<UINode> nodeArrayList;
    private LayoutInflater layoutInflater;
    private int newNodePosition=-1;
    private Presenter presenter;

    public CustomAdapter(Context context, Presenter presenter) {
        this.context = context;
        this.presenter = presenter;
        this.nodeArrayList = presenter.buildNodeListFromTree();
        this.expand(0,nodeArrayList.get(0));
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        deviceWidth = size.x;
        deviceHeight = size.y;
    }

    @Override
    public int getCount() {
        return nodeArrayList.size();
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
        final UINode currentNode = nodeArrayList.get(position);
        final View rowView = layoutInflater.inflate(R.layout.layout_node, null);

        nodeHolder.editText=(EditText)rowView.findViewById(R.id.editText);
        nodeHolder.editText.setVisibility(View.GONE);

        setText(nodeHolder, rowView, currentNode);
        addPadding(position, rowView);

        setImageForExpandCollapse(nodeHolder, rowView, currentNode);
        setEventToExpandCollpse(position, nodeHolder, currentNode);
        setEventToAddNode(position, nodeHolder, rowView, currentNode);

        if(position == newNodePosition)
        {
            nodeHolder.textViewForName.setVisibility(View.GONE);
            nodeHolder.editText.setVisibility(View.VISIBLE);
            nodeHolder.editText.requestFocus();
            String s=""+nodeHolder.textViewForName.getText();

            nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        nodeHolder.textViewForName.setText(nodeHolder.editText.getText());
                        currentNode.setName("" + nodeHolder.editText.getText());
                        nodeHolder.editText.setVisibility(View.GONE);
                        nodeHolder.textViewForName.setVisibility(View.VISIBLE);
                        newNodePosition=-1;
                        presenter.addChild(position);
                        return true;
                    }
                    return false;
                }
            });

        }
        return rowView;
    }

    private void setText(final NodeHolder nodeHolder, View rowView, final UINode currentNode) {
        nodeHolder.textViewForName = (TextView) rowView.findViewById(R.id.name);
        nodeHolder.textViewForName.setText(currentNode.getName());



        nodeHolder.textViewForName.setHeight(deviceHeight / Constants.HEIGHT_DIVIDER);

        editText(nodeHolder, currentNode);
    }

    private void editText(final NodeHolder nodeHolder, final UINode currentNode) {
        nodeHolder.textViewForName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodeHolder.textViewForName.setVisibility(View.GONE);
                nodeHolder.editText.setVisibility(View.VISIBLE);
                nodeHolder.editText.requestFocus();
                nodeHolder.editText.setText(nodeHolder.textViewForName.getText());
                nodeHolder.editText.setSelection(nodeHolder.editText.getText().length());
                final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(nodeHolder.editText, InputMethodManager.SHOW_FORCED);
                }
                nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            nodeHolder.textViewForName.setText(nodeHolder.editText.getText());
                            currentNode.setName("" + nodeHolder.editText.getText());
                            nodeHolder.editText.setVisibility(View.GONE);
                            nodeHolder.textViewForName.setVisibility(View.VISIBLE);
                            if (inputMethodManager != null) {
                                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                            }
                            int position=nodeArrayList.indexOf(currentNode);
                            presenter.updateChild(currentNode,position);
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
    }

    private void setEventToExpandCollpse(final int position, NodeHolder nodeHolder, final UINode currentNode) {
        nodeHolder.expandCollapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentNode.getChildSubTree().size() == 0)
                    return;
                if (currentNode.isExpanded()) {

                    collapse(position, currentNode);
                } else {
                    expand(position, currentNode);
                }
                currentNode.toggleStatus();

                notifyDataSetChanged();
            }
        });
    }

    private void setEventToAddNode(final int position, NodeHolder nodeHolder, View rowView, final UINode currentNode) {
        nodeHolder.addNodeButton = (ImageView) rowView.findViewById(R.id.options);
        nodeHolder.addNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChild(position, currentNode);
            }
        });
    }

    private void addChild(int position, UINode currentNode) {
        int i = position;
        while (++i < nodeArrayList.size() && nodeArrayList.get(i).getDepth() > currentNode.getDepth()) ;
        if(currentNode.getChildSubTree().size()==0)
        currentNode.toggleStatus();
        this.setNewNodePosition(i);
        UINode parentNode = nodeArrayList.get(position);
        UINode node = new UINode("Enter Text", parentNode.getDepth() + 20,parentNode.getId());
        nodeArrayList.add(i, node);
        parentNode.getChildSubTree().add(parentNode.getChildSubTree().size(), node);
        notifyDataSetChanged();
    }

    private void addPadding(int position, View rowView) {
        RelativeLayout relativeLayout = (RelativeLayout) rowView.findViewById(R.id.layout);
        setPaddingForeNode(position, relativeLayout);
    }

    private void setImageForExpandCollapse(NodeHolder nodeHolder, View rowView, UINode currentNode) {
        nodeHolder.expandCollapseButton = (ImageView) rowView.findViewById(R.id.expandCollapse);
        if (currentNode.getStatus().equalsIgnoreCase("expand")) {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.expand);
        } else {
            if(currentNode.getChildSubTree().size()==0){
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.leaf);
            }
            else {
                nodeHolder.expandCollapseButton.setImageResource(R.drawable.collapse);
            }
        }

    }

    public void expand(int position, UINode currentNode) {
        int j = position + 1;
        ArrayList<UINode> nodes = currentNode.getChildSubTree();
        for (int i = 0; i < nodes.size(); i++) {
            nodeArrayList.add(j++, nodes.get(i));
        }
    }

    private void collapse(int position, UINode currentNode) {
        for (int i = position + 1; i < nodeArrayList.size(); ) {
            if (nodeArrayList.get(i).getDepth() > currentNode.getDepth()) {
                if (nodeArrayList.get(i).getStatus().equals("expand"))
                    nodeArrayList.get(i).toggleStatus();
                nodeArrayList.remove(i);
            } else {

                break;
            }
        }
    }

    private void setPaddingForeNode(int position, RelativeLayout relativeLayout) {
        relativeLayout.setPadding(nodeArrayList.get(position).getDepth(), 0, 0, 0);
    }

    public void setNewNodePosition(int newNodePosition) {
        this.newNodePosition = newNodePosition;
    }

    private class NodeHolder{
        ImageView expandCollapseButton;
        TextView textViewForName;
        ImageView addNodeButton;
        EditText editText;
    }
}
