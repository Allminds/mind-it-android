package com.thoughtworks.mindit.mindit.view.model;

import com.thoughtworks.mindit.mindit.Constants;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UINodeTest {
    UINode uiNode;
    UINode root;

    @Before
    public void initilaize() {
        root = new UINode("root", 0, null);
        uiNode = new UINode("node", 1, root.getId());

        ArrayList<UINode> childSubTree = new ArrayList<UINode>();
        childSubTree.add(0, uiNode);
        root.setChildSubTree(childSubTree);
    }

    @Test
    public void shouldToggleStatusFromCollapseToExpandForNonRootNode() {
        assertEquals(Constants.STATUS.COLLAPSE.toString(), uiNode.getStatus());
        assertEquals(Constants.STATUS.EXPAND.toString(), uiNode.toggleStatus());
    }

    @Test
    public void shouldToggleStatusFromExpandToCollapseForRootNode() {
        root.setStatus(Constants.STATUS.EXPAND.toString());
        assertEquals(Constants.STATUS.COLLAPSE.toString(), root.toggleStatus());
    }

    @Test
    public void shouldRemoveChildFromChildSubTree() {
        assertEquals(true, root.removeChild(uiNode));
    }

    @Test
    public void shouldReturnFalseOnTryingToRemoveNonExistantChild() {
        root.removeChild(uiNode);
        assertFalse(root.removeChild(uiNode));
    }

    @Test
    public void shouldAddChildInChildSubTree() {
        assertEquals(true, root.addChild(uiNode));
    }

    @Test
    public void shouldReturnFalseForCollapsedNode() {
        root.setStatus(Constants.STATUS.COLLAPSE.toString());
        assertFalse(root.isExpanded());
    }

    @Test
    public void shouldReturnTrueForExpandedNode() {
        root.setStatus(Constants.STATUS.EXPAND.toString());
        assertTrue(root.isExpanded());
    }
}
