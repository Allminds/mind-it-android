package com.thoughtworks.mindit.view.adapter;

import com.thoughtworks.mindit.constant.Constants;
import com.thoughtworks.mindit.view.adapter.CustomAdapter;
import com.thoughtworks.mindit.view.adapter.CustomAdapterHelper;
import com.thoughtworks.mindit.view.model.UINode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CustomAdapterHelperTest {
    UINode root, node;
    CustomAdapterHelper customAdapterHelper;
    CustomAdapter customAdapter;

    @Before
    public void setUp() throws Exception {
        customAdapter = Mockito.mock(CustomAdapter.class);
        customAdapterHelper = new CustomAdapterHelper(customAdapter);
        root = new UINode("root", 0, null);

        customAdapterHelper.getNodeList().add(root);
        customAdapterHelper.setNodeList(customAdapterHelper.getNodeList());
    }

    @Test
    public void shouldAddNode() throws Exception {
        assertEquals(true, customAdapterHelper.addChild(0, root) != null);
    }

    @Test
    public void shouldExpandNodeAndAddAllChildrenToNodelist() {
        UINode node = customAdapterHelper.addChild(0, root);
        ArrayList<UINode> nodeList = customAdapterHelper.expand(0, root);
        assertTrue(nodeList.contains(node));
        assertEquals(root.getStatus(), Constants.STATUS.EXPAND.toString());
    }

    @Test
    public void shouldCollapseNodeAndRemoveAllChildrenToNodelist() {
        UINode node = customAdapterHelper.addChild(0, root);
        ArrayList<UINode> nodeList = customAdapterHelper.collapse(0, root);
        assertFalse(nodeList.contains(node));
        assertEquals(root.getStatus(), Constants.STATUS.COLLAPSE.toString());
    }
}
