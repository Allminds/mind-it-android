package com.thoughtworks.mindit.mindit.presenter;

import com.thoughtworks.mindit.mindit.Tracker;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;

/**
 * Created by sjadhav on 01/02/16.
 */
public class PresenterTest {
    Presenter presenter;
    Node root;
    private UINode uiNode;
    private Tracker tracker;
    private Tree tree;

    @Before
    public void setUp() throws Exception {
        root = new Node("1", "node1", null, "1", 0);
        tracker = Mockito.mock(Tracker.class);
        HashMap<String, Node> nodes = new HashMap<String, Node>();
        nodes.put(root.getId(), root);
        tree = Tree.getInstance(nodes);
        //  tree = Mockito.mock(Tree.class);
        presenter = new Presenter(tracker, tree);
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
        tree.addNode(child);
        root.addThisChild(child, 0);
        uiNode = presenter.convertModelNodeToUINode(root);

        presenter.updateUIChildSubtree(root, uiNode);
        assertEquals(uiNode.getChildSubTree().size(), root.getChildSubTree().size());

    }

    @Test
    public void shouldAddNode() throws Exception {
        uiNode = new UINode("child1", 1, root.getId());
        presenter.addNode(uiNode);
        Node newNode = tree.getNode(uiNode.getId());
        assertEquals(newNode.getId(), uiNode.getId());
    }
}
