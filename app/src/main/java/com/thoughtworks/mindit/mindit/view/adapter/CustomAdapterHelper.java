package com.thoughtworks.mindit.mindit.view.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.thoughtworks.mindit.mindit.R;
import com.thoughtworks.mindit.mindit.constant.Constants;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import java.util.ArrayList;

public class CustomAdapterHelper {
    private final CustomAdapter customAdapter;
    private ArrayList<UINode> nodeList;
    private int mode = Constants.EDIT_MODE;

    public CustomAdapterHelper(CustomAdapter customAdapter) {
        this.customAdapter = customAdapter;
        this.nodeList = customAdapter.getNodeList();
    }

    public ArrayList<UINode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<UINode> nodeList) {
        this.nodeList = nodeList;
    }

    public void resetMode() {
        mode = Constants.SELECTION_MODE;
    }

    void updateText(NodeHolder nodeHolder, UINode currentNode) {
        nodeHolder.textViewForName.setText(nodeHolder.editText.getText());
        currentNode.setName("" + nodeHolder.editText.getText());
    }

    void initializeTextView(final NodeHolder nodeHolder, View rowView, final UINode currentNode) {
        nodeHolder.switcher = (ViewSwitcher) rowView.findViewById(R.id.viewSwitcher);
        nodeHolder.textViewForName = (TextView) nodeHolder.switcher.findViewById(R.id.clickable_text_view);
        nodeHolder.textViewForName.setText(currentNode.getName());
        nodeHolder.editText = (EditText) nodeHolder.switcher.findViewById(R.id.hidden_edit_view);
        this.editText(nodeHolder, currentNode, rowView);
    }

    void editText(final NodeHolder nodeHolder, final UINode currentNode, final View rowView) {
        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.layout_text);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSelectionMode(currentNode, nodeHolder);
            }
        });
        nodeHolder.textViewForName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSelectionMode(currentNode, nodeHolder);
            }
        });
    }

    private void handleSelectionMode(UINode currentNode, NodeHolder nodeHolder) {
        if (mode == Constants.SELECTION_MODE || nodeList.indexOf(currentNode) != customAdapter.getSelectedNodePosition()) {
            int lastFocusedNode = customAdapter.getSelectedNodePosition();
           if(nodeList.get(lastFocusedNode).getName().equals("") && lastFocusedNode ==customAdapter.getNewNodePosition()) {
               nodeList.remove(lastFocusedNode);
               customAdapter.resetNewNodePosition();
           }
            final InputMethodManager lManager = (InputMethodManager) customAdapter.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(lManager.isActive())
                lManager.hideSoftInputFromWindow(nodeHolder.editText.getWindowToken(), 0);
            customAdapter.setSelectedNodePosition(nodeList.indexOf(currentNode));
            mode = Constants.EDIT_MODE;
            customAdapter.notifyDataSetChanged();
        } else {
            editTextOfNode(nodeHolder, currentNode);
            mode = Constants.SELECTION_MODE;
        }
    }

    private void editTextOfNode(final NodeHolder nodeHolder, final UINode currentNode) {
        nodeHolder.switcher.showNext();
        nodeHolder.editText.setText(nodeHolder.textViewForName.getText());
        nodeHolder.editText.setSelection(nodeHolder.editText.getText().length());
        final InputMethodManager lManager = (InputMethodManager) customAdapter.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        showKeypad(nodeHolder, lManager);
        nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                System.out.println("KeyCode add:" + KeyEvent.keyCodeToString(keyCode) +"***"+event.toString());
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BACK ) {
                    updateText(nodeHolder, currentNode);
                    if (lManager != null) {
                        lManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                    customAdapter.getPresenter().updateNode(currentNode);
                    nodeHolder.switcher.showPrevious();
                    return true;
                }
                return false;
            }
        });
    }
    void setEventToExpandCollapse(final int position, NodeHolder nodeHolder, final UINode currentNode) {
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
                customAdapter.notifyDataSetChanged();
            }
        });
    }

    void setEventToAddNodeButton(final int position, NodeHolder nodeHolder, View rowView, final UINode currentNode) {
/*        nodeHolder.addNodeButton = (ImageView) rowView.findViewById(R.id.options);
        nodeHolder.addNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChild(position, currentNode);
            }
        });*/
    }

    public void addNode(final NodeHolder nodeHolder, final UINode currentNode) {
        nodeHolder.switcher.showNext();
    //    nodeHolder.switcher.addOnAttachStateChangeListener();
        nodeHolder.editText.requestFocus();
        nodeHolder.editText.setText(nodeHolder.textViewForName.getText());
       final InputMethodManager lManager = (InputMethodManager) customAdapter.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        showKeypad(nodeHolder, lManager);

        nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                System.out.println("KeyCode edit:" + KeyEvent.keyCodeToString(keyCode));
                if (keyCode == KeyEvent.KEYCODE_ENTER  ) {
                    updateTextOfNewNode(nodeHolder, currentNode, lManager);
                    return true;
                }
                return false;
            }
        });
    }

    private void showKeypad(final NodeHolder nodeHolder, final InputMethodManager lManager) {
        nodeHolder.editText.post(new Runnable() {
            public void run() {
                nodeHolder.editText.requestFocus();
                lManager.showSoftInput(nodeHolder.editText, 0);
            }
        });
    }

    private void updateTextOfNewNode(NodeHolder nodeHolder, UINode currentNode, InputMethodManager inputMethodManager) {
        updateText(nodeHolder, currentNode);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        customAdapter.resetNewNodePosition();
        customAdapter.getPresenter().addNode(currentNode);
        nodeHolder.switcher.showPrevious();
    }

    public UINode addChild(int position, UINode parent) {
        if (parent.getStatus().equals(Constants.STATUS.COLLAPSE.toString())) {
            this.expand(position, parent);
        }

        int newNodePosition = getNewNodePosition(position, parent);
        customAdapter.setNewNodePosition(newNodePosition);

        UINode node = new UINode(Constants.EMPTY_STRING, parent.getDepth() + Constants.PADDING_FOR_DEPTH, parent.getId());
        nodeList.add(newNodePosition, node);

        boolean addedInParent = parent.addChild(node);

        customAdapter.notifyDataSetChanged();

        if (nodeList.contains(node) && addedInParent)
            return node;
        else
            return null;
    }

    private int getNewNodePosition(int position, UINode parent) {
        int childCount = 1;
        for (int index = position + 1; index < nodeList.size() && nodeList.get(index).getDepth() > parent.getDepth(); index++) {
            childCount++;
        }
        return position + childCount;
    }

    void addPadding(int position, View rowView) {
        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.layout);
        linearLayout.setPadding(nodeList.get(position).getDepth(), 0, 0, 0);
    }

    void setImageForExpandCollapse(NodeHolder nodeHolder, View rowView, UINode currentNode) {
        nodeHolder.expandCollapseButton = (ImageView) rowView.findViewById(R.id.expandCollapse);
        if (currentNode.getChildSubTree().size() == 0) {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.leaf);
        } else if (currentNode.getStatus().equalsIgnoreCase(Constants.STATUS.EXPAND.toString())) {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.expand);
        } else {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.collapse);
        }
    }

    public ArrayList<UINode> expand(int position, UINode currentNode) {
        int childPosition = position + 1;

        ArrayList<UINode> childSubTree = currentNode.getChildSubTree();
        for (int nodeIndex = 0; nodeIndex < childSubTree.size(); nodeIndex++) {
            nodeList.add(childPosition++, childSubTree.get(nodeIndex));
        }
        currentNode.setStatus(Constants.STATUS.EXPAND.toString());

        return nodeList;
    }

    public ArrayList<UINode> collapse(int position, UINode currentNode) {
        int nodeIndex = position + 1;
        while (nodeIndex < nodeList.size()) {
            if (nodeList.get(nodeIndex).getDepth() > currentNode.getDepth()) {
                if (nodeList.get(nodeIndex).getStatus().equals(Constants.STATUS.EXPAND.toString()))
                    nodeList.get(nodeIndex).toggleStatus();
                nodeList.remove(nodeIndex);
            } else {
                break;
            }
        }
        currentNode.setStatus(Constants.STATUS.COLLAPSE.toString());
        return nodeList;
    }
}
