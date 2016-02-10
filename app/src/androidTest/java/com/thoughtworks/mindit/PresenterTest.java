package com.thoughtworks.mindit;


import com.thoughtworks.mindit.Tracker;
import com.thoughtworks.mindit.model.Node;
import com.thoughtworks.mindit.presenter.Presenter;
import com.thoughtworks.mindit.view.model.UINode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class PresenterTest {
    private Presenter presenter;
    private Node root;
    private UINode uiNode;
    private Tracker tracker;

    @Before
    public void setUp() throws Exception {
        root = new Node("1", "node1", null, null, 0);
        tracker = Mockito.mock(Tracker.class);
        presenter = new Presenter(tracker);
    }

    @Test
    public void shouldConvertModelNodeUINode() throws Exception {

        uiNode = presenter.convertModelNodeToUINode(root);
        assertEquals(root.getId(), uiNode.getId());
    }

    @Test
    public void shouldBuildNodeListFromTree() throws Exception {
        tracker.getTree().addNode(root);
        ArrayList<UINode> nodeList = presenter.buildNodeListFromTree();
        Node root = tracker.getTree().getRoot();
        UINode uiRoot = nodeList.get(0);
        assertEquals(uiRoot.getId(), root.getId());
    }

    @Test
    public void shouldUpdateUIChildSubtree() throws Exception {
        Node child = new Node("2", "node2", root, root.getId(), 0);
        root.addThisChild(child, 0);
        presenter.updateUIChildSubtree(root, uiNode);
        assertEquals(uiNode.getChildSubTree().size(), root.getChildSubTree().size());
        assertEquals(uiNode.getChildSubTree().get(0).getId(), root.getId());

    }
}
