package com.thoughtworks.mindit.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.thoughtworks.mindit.Config;
import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.UpdateOption;
import com.thoughtworks.mindit.view.MindmapActivity;
import com.thoughtworks.mindit.view.model.UINode;

import java.util.ArrayList;

class CustomAdapterHelper {
    private InputMethodManager lManager;
    private final CustomAdapter customAdapter;
    private ArrayList<UINode> nodeList;

    public CustomAdapterHelper(CustomAdapter customAdapter) {
        this.customAdapter = customAdapter;
        this.nodeList = customAdapter.getNodeList();
        initializeInputMethodManager(customAdapter);
    }

    private void initializeInputMethodManager(CustomAdapter customAdapter) {
        if(customAdapter.getContext() != null) {
            lManager = (InputMethodManager) customAdapter.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }


    private void updateText(NodeHolder nodeHolder, UINode currentNode) {
        nodeHolder.textViewForName.setText(nodeHolder.editText.getText());
        currentNode.setName(Constants.EMPTY_STRING + nodeHolder.editText.getText());
    }

    void initializeTextView(final NodeHolder nodeHolder, View rowView, final UINode currentNode) {
        nodeHolder.switcher = (ViewSwitcher) rowView.findViewById(R.id.viewSwitcher);
        nodeHolder.textViewForName = (TextView) nodeHolder.switcher.findViewById(R.id.clickable_text_view);
        nodeHolder.textViewForName.setText(currentNode.getName());
        nodeHolder.editText = (EditText) nodeHolder.switcher.findViewById(R.id.hidden_edit_view);
        this.editText(nodeHolder, currentNode, rowView);
    }

    private void editText(final NodeHolder nodeHolder, final UINode currentNode, final View rowView) {
        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.layout_node);
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
        int mode = Constants.EDIT_MODE;
        if (mode == Constants.SELECTION_MODE || nodeList.indexOf(currentNode) != customAdapter.getSelectedNodePosition()) {
            int lastFocusedNode = customAdapter.getSelectedNodePosition();
            if (nodeList.get(lastFocusedNode).getName().equals("") && lastFocusedNode == customAdapter.getWorkingNodePosition()) {
                removeFromParentChildSubTree(lastFocusedNode);
                nodeList.remove(lastFocusedNode);
            }
            if (lManager.isActive())
                lManager.hideSoftInputFromWindow(nodeHolder.editText.getWindowToken(), 0);
            customAdapter.setSelectedNodePosition(nodeList.indexOf(currentNode));
            if (Config.FEATURE_EDIT) {
                mode = Constants.EDIT_MODE;
            }
            customAdapter.resetWorkingNodePosition();
            customAdapter.notifyDataSetChanged();
        } else {
            if (Config.FEATURE_EDIT) {
                editTextOfNode(nodeHolder, currentNode);
                mode = Constants.SELECTION_MODE;
            }
        }
    }

    private void removeFromParentChildSubTree(int lastFocusedNode) {
        UINode parent = null;
        UINode child = nodeList.get(lastFocusedNode);
        for (UINode node : nodeList) {
            if (node.getId().equals(child.getParentId())) {
                parent = node;
                break;
            }
        }
        if( parent != null) {
            parent.getChildSubTree().remove(child);
        }
    }

    private void editTextOfNode(final NodeHolder nodeHolder, final UINode currentNode) {
        nodeHolder.switcher.showNext();
        nodeHolder.editText.setText(nodeHolder.textViewForName.getText());
        nodeHolder.editText.setSelection(nodeHolder.editText.getText().length());
        customAdapter.setWorkingNodePosition(nodeList.indexOf(currentNode));
        customAdapter.setOperation(UpdateOption.UPDATE);
        customAdapter.notifyDataSetChanged();
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

    public void doOperation(final NodeHolder nodeHolder, final UINode currentNode, final UpdateOption operation) {
        nodeHolder.switcher.showNext();
        nodeHolder.editText.setText(nodeHolder.textViewForName.getText());
        showKeypad(nodeHolder, lManager);
        nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.v("KeyCode:", "" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.ACTION_DOWN || keyCode == KeyEvent.KEYCODE_BACK) {
                    if (operation == UpdateOption.ADD) {
                        updateTextOfNewNode(nodeHolder, currentNode, lManager);
                    } else if (operation == UpdateOption.UPDATE) {
                        updateTextOfCurrentNode(nodeHolder, currentNode, lManager);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void showKeypad(final NodeHolder nodeHolder, final InputMethodManager lManager) {
        nodeHolder.editText.requestFocus();
        nodeHolder.editText.post(new Runnable() {
            public void run() {
                lManager.showSoftInput(nodeHolder.editText, InputMethodManager.SHOW_FORCED);
                nodeHolder.editText.requestFocus();
            }
        });
    }

    private void updateTextOfNewNode(NodeHolder nodeHolder, UINode currentNode, InputMethodManager inputMethodManager) {
        updateText(nodeHolder, currentNode);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }


        /*listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);*/


        customAdapter.resetWorkingNodePosition();
        customAdapter.getPresenter().addNode(currentNode);
        nodeHolder.switcher.showPrevious();
    }
    private void updateTextOfCurrentNode(NodeHolder nodeHolder, UINode currentNode, InputMethodManager inputMethodManager) {
        updateText(nodeHolder, currentNode);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
        customAdapter.resetWorkingNodePosition();
        customAdapter.getPresenter().updateNode(currentNode);
        nodeHolder.switcher.showPrevious();
    }

    public UINode addChild(int position, UINode parent) {
        if (parent.getStatus().equals(Constants.STATUS.COLLAPSE.toString())) {
            this.expand(position, parent);
        }

        int newNodePosition = getNewNodePosition(position, parent);
        customAdapter.setWorkingNodePosition(newNodePosition);
        customAdapter.setOperation(UpdateOption.ADD);
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
        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.layout_text);
        linearLayout.setPadding(nodeList.get(position).getDepth(), 0, 0, 0);
    }

    void setImageForExpandCollapse(NodeHolder nodeHolder, View rowView, UINode currentNode) {
        nodeHolder.expandCollapseButton = (ImageView) rowView.findViewById(R.id.expandCollapse);
        if (currentNode.getChildSubTree().size() == 0) {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.simple_leaf);
        } else if (currentNode.getStatus().equalsIgnoreCase(Constants.STATUS.EXPAND.toString())) {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.simple_expand);
        } else {
            nodeHolder.expandCollapseButton.setImageResource(R.drawable.simple_collapse);

        }
    }

    public ArrayList<UINode> expand(int position, UINode currentNode) {
        int childPosition = position + 1;
        customAdapter.setSelectedNodePosition(position);
        ArrayList<UINode> childSubTree = currentNode.getChildSubTree();
        for (int nodeIndex = 0; nodeIndex < childSubTree.size(); nodeIndex++) {
            nodeList.add(childPosition++, childSubTree.get(nodeIndex));
        }
        currentNode.setStatus(Constants.STATUS.EXPAND.toString());

        return nodeList;
    }

    public ArrayList<UINode> collapse(int position, UINode currentNode) {
        int nodeIndex = position + 1;
        customAdapter.setSelectedNodePosition(position);
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

    public ArrayList<UINode> getNodeList() {
        return nodeList;
    }
}
