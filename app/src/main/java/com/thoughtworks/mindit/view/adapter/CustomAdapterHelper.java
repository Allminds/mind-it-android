package com.thoughtworks.mindit.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.view.menu.ActionMenuItemView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.thoughtworks.mindit.Config;
import com.thoughtworks.mindit.R;
import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.constant.MindIt;
import com.thoughtworks.mindit.constant.UpdateOption;
import com.thoughtworks.mindit.view.MindmapActivity;
import com.thoughtworks.mindit.view.model.UINode;

import java.util.ArrayList;

class CustomAdapterHelper {
    private final CustomAdapter customAdapter;
    private InputMethodManager lManager;
    private ArrayList<UINode> nodeList;

    private int mode = Constants.SELECTION_MODE;

    public CustomAdapterHelper(CustomAdapter customAdapter) {
        this.customAdapter = customAdapter;
        this.nodeList = customAdapter.getNodeList();
        initializeInputMethodManager(customAdapter);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private void initializeInputMethodManager(CustomAdapter customAdapter) {
        if (customAdapter.getContext() != null) {
            lManager = (InputMethodManager) customAdapter.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }


    private void updateText(NodeHolder nodeHolder, UINode currentNode) {
        nodeHolder.textViewForName.setText(Constants.EMPTY_STRING + nodeHolder.editText.getText());
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
        nodeHolder.clickableArea = (LinearLayout) rowView.findViewById(R.id.layout_node);
        nodeHolder.clickableArea.setOnClickListener(new View.OnClickListener() {
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

    public void handleSelectionMode(UINode currentNode, final NodeHolder nodeHolder) {
        int lastFocusedNode = customAdapter.getSelectedNodePosition();
        if (lastFocusedNode >= 0 && lastFocusedNode == customAdapter.getWorkingNodePosition() && nodeList.get(lastFocusedNode).getName().equals("") && nodeList.get(lastFocusedNode).getId().equals("")) {
            removeFromParentChildSubTree(lastFocusedNode);
            nodeList.remove(lastFocusedNode);
        }
        customAdapter.setSelectedNodePosition(nodeList.indexOf(currentNode));
        customAdapter.resetWorkingNodePosition();
        customAdapter.notifyDataSetChanged();
        mode = Constants.SELECTION_MODE;
        ActionMenuItemView delete = (ActionMenuItemView) ((MindmapActivity) customAdapter.getContext()).findViewById(R.id.delete);
        ActionMenuItemView add = (ActionMenuItemView) ((MindmapActivity) customAdapter.getContext()).findViewById(R.id.add);
        if (delete != null && MindIt.LinkType.equals("readWriteLink")) {
            delete.setVisibility(View.VISIBLE);
        }
        if (add != null && MindIt.LinkType.equals("readWriteLink")) {
            add.setVisibility(View.VISIBLE);
        }
        if (Config.FEATURE_EDIT ) {
            editTextOfNode(nodeHolder, currentNode);
        }
        lManager.hideSoftInputFromWindow(nodeHolder.editText.getWindowToken(), 0);
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
        if (parent != null) {
            parent.getChildSubTree().remove(child);
        }
    }

    private void editTextOfNode(final NodeHolder nodeHolder, final UINode currentNode) {
        customAdapter.setWorkingNodePosition(nodeList.indexOf(currentNode));
        customAdapter.setOperation(UpdateOption.UPDATE);
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

    public void doOperation(final NodeHolder nodeHolder, final UINode currentNode, final String operation) {
        nodeHolder.switcher.showNext();
        nodeHolder.editText.setText(nodeHolder.textViewForName.getText());
        if (operation == UpdateOption.ADD)
            nodeHolder.editText.requestFocus();
        if (operation == UpdateOption.UPDATE && mode == Constants.EDIT_MODE) {
            nodeHolder.editText.requestFocus();
            nodeHolder.editText.setCursorVisible(true);
        }

        nodeHolder.editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                customAdapter.resetWorkingNodePosition();
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.ACTION_DOWN || keyCode == KeyEvent.KEYCODE_BACK) {

                    return updateToDatabase(keyCode, operation, nodeHolder, currentNode);
                }
                return false;
            }
        });
        nodeHolder.editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                enableEditMode();
                return false;
            }
        });
        nodeHolder.editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                updateToDatabase(KeyEvent.KEYCODE_ENTER,operation,nodeHolder, currentNode);
                return true;
            }
        });
    }

    private boolean updateToDatabase(int keyCode, String operation, NodeHolder nodeHolder, UINode currentNode) {
        if (operation == UpdateOption.ADD) {
            updateTextOfNewNode(nodeHolder, currentNode, keyCode);
        } else if (operation == UpdateOption.UPDATE) {
            updateTextOfCurrentNode(nodeHolder, currentNode);

        }
        lManager.hideSoftInputFromWindow(nodeHolder.editText.getWindowToken(), 0);
        mode = Constants.SELECTION_MODE;
        ActionMenuItemView delete = (ActionMenuItemView) ((MindmapActivity) customAdapter.getContext()).findViewById(R.id.delete);
        ActionMenuItemView add = (ActionMenuItemView) ((MindmapActivity) customAdapter.getContext()).findViewById(R.id.add);
        if (delete != null) {
            delete.setVisibility(View.VISIBLE);
        }
        if (add != null) {
            add.setVisibility(View.VISIBLE);
        }
        return true;
    }

    public void enableEditMode() {
        mode = Constants.EDIT_MODE;
        ActionMenuItemView add = (ActionMenuItemView) ((MindmapActivity) customAdapter.getContext()).findViewById(R.id.add);
        ActionMenuItemView delete = (ActionMenuItemView) ((MindmapActivity) customAdapter.getContext()).findViewById(R.id.delete);
        if (delete != null) {
            delete.setVisibility(View.INVISIBLE);
        }
        if (add != null) {
            add.setVisibility(View.INVISIBLE);
        }
    }


    private void updateTextOfNewNode(NodeHolder nodeHolder, UINode currentNode, int keyCode) {
        updateText(nodeHolder, currentNode);
        customAdapter.resetWorkingNodePosition();
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            customAdapter.getPresenter().addNode(currentNode);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // int lastFocusedNode == customAdapter.getWorkingNodePosition()
            removeFromParentChildSubTree(nodeList.indexOf(currentNode));
            nodeList.remove(nodeList.indexOf(currentNode));

        }

        nodeHolder.switcher.showPrevious();
    }

    private void updateTextOfCurrentNode(NodeHolder nodeHolder, UINode currentNode) {
        updateText(nodeHolder, currentNode);
        customAdapter.resetWorkingNodePosition();
        customAdapter.getPresenter().updateNode(currentNode);
        nodeHolder.switcher.showPrevious();
    }

    public UINode addChild(int position, UINode parent) {
        if (parent.getStatus().equals(Constants.STATUS.COLLAPSE.toString())) {
            this.expand(position, parent);
        }
        int childPosition = getNewNodePosition(position, parent);
        customAdapter.setWorkingNodePosition(childPosition);
        customAdapter.setOperation(UpdateOption.ADD);
        UINode node = new UINode(Constants.EMPTY_STRING, parent.getDepth() + Constants.PADDING_FOR_DEPTH, parent.getId());
        nodeList.add(childPosition, node);
        boolean addedInParent = parent.addChild(node);
        Context actionMenuContext = customAdapter.getContext();
        if (actionMenuContext != null) {
            ActionMenuItemView add = (ActionMenuItemView) ((MindmapActivity) actionMenuContext).findViewById(R.id.add);
            if (add != null) {
                add.setVisibility(View.INVISIBLE);
            }
            ActionMenuItemView delete = (ActionMenuItemView) ((MindmapActivity) actionMenuContext).findViewById(R.id.delete);
            if (delete != null) {
                delete.setVisibility(View.INVISIBLE);
            }
        }
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

    void addPadding(int position, View rowView, NodeHolder nodeHolder, UINode selectedNode) {

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) nodeHolder.verticalLine.getLayoutParams();
        layoutParams.setMargins(selectedNode.getDepth() + 10, 0, 0, 0);
        nodeHolder.verticalLine.setBackgroundColor(Color.parseColor("#FFFFFF"));
        nodeHolder.verticalLine.setVisibility(View.INVISIBLE);

        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.layout_text);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        layoutParams1.setMargins(nodeList.get(position).getDepth() - selectedNode.getDepth() - 48, 0, 0, 0);
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
