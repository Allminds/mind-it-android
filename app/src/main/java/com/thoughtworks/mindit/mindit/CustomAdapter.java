package com.thoughtworks.mindit.mindit;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.thoughtworks.mindit.mindit.presenter.Presenter;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<UINode> nodeArrayList;
    private LayoutInflater layoutInflater;
    private int newNodePosition=-1;
    private Presenter presenter;

    public CustomAdapter(Context context, Presenter presenter) {
        this.context = context;
        this.presenter = presenter;
        this.nodeArrayList = presenter.getArrayList();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        if(position==newNodePosition)
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
        editText(nodeHolder, currentNode);
    }

    private void editText(final NodeHolder nodeHolder, final UINode currentNode) {
        nodeHolder.textViewForName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodeHolder.textViewForName.setVisibility(View.GONE);
                nodeHolder.editText.setVisibility(View.VISIBLE);
                nodeHolder.editText.requestFocus();
                nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            nodeHolder.textViewForName.setText(nodeHolder.editText.getText());
                            currentNode.setName("" + nodeHolder.editText.getText());
                            nodeHolder.editText.setVisibility(View.GONE);
                            nodeHolder.textViewForName.setVisibility(View.VISIBLE);
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

    private void addPadding(int position, View rowView) {
        RelativeLayout relativeLayout = (RelativeLayout) rowView.findViewById(R.id.layout);
        setPaddingForeNode(position, relativeLayout);
    }

    private void setImageForExpandCollapse(NodeHolder nodeHolder, View rowView, UINode currentNode) {
        nodeHolder.expandCollapseButton = (ImageView) rowView.findViewById(R.id.expandCollapse);
        if (currentNode.getStatus().equalsIgnoreCase("expand")) {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.expand);
        } else {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.collapse);
        }

    }

    private void addChild(int position, UINode currentNode) {
        int i = position;
        while (++i < nodeArrayList.size() && nodeArrayList.get(i).getDepth() > currentNode.getDepth()) ;
        currentNode.toggleStatus();
        newNodePosition=i;
        UINode parentNode = nodeArrayList.get(position);
        UINode node = new UINode("Enter Text", parentNode.getDepth() + 20);
        nodeArrayList.add(i, node);
        parentNode.getChildSubTree().add(parentNode.getChildSubTree().size(), node);
        notifyDataSetChanged();
    }

    private void expand(int position, UINode currentNode) {
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

}
