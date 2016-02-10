package com.thoughtworks.mindit.presenter;

import com.thoughtworks.mindit.Tracker;
import com.thoughtworks.mindit.model.Node;
import com.thoughtworks.mindit.model.Tree;
import com.thoughtworks.mindit.presenter.Presenter;
import com.thoughtworks.mindit.view.model.UINode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class PresenterTest {
    Presenter presenter;
    Node root;
    private UINode uiNode;
    private Tracker tracker;
    private Tree tree;


    @Before
    public void setUp() throws Exception {
        root = new Node("1", "root", null, "1", 0);
        tracker = Mockito.mock(Tracker.class);
        HashMap<String, Node> nodes = new HashMap<String, Node>();
        nodes.put(root.getId(), root);
        tree = Tree.getInstance(nodes);
        presenter = new Presenter(tracker, tree);
    }


    @Test
    public void shouldConvertModelNodeUINode() throws Exception {

        uiNode = presenter.convertModelNodeToUINode(root);
        assertEquals(root.getId(), uiNode.getId());
    }

    @Test
    public void shouldBuildNodeListFromTree() throws Exception {
        ArrayList<UINode> nodeList = presenter.buildNodeListFromTree();
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
    public void shouldAddNode() {
        uiNode = new UINode("child2", 1, root.getId());
        ArgumentCaptor<Node> argumentCaptor = ArgumentCaptor.forClass(Node.class);
        presenter.addNode(uiNode);
        verify(tracker).addChild(argumentCaptor.capture());
    }

    @Test
    public void shouldUpdateNode() {
        uiNode = new UINode("child2", 1, root.getId());
        Node node = presenter.convertUINodeToModelNode(uiNode, root);
        tree.addNode(node);
        uiNode.setName("child2");
        ArgumentCaptor<Node> argumentCaptor = ArgumentCaptor.forClass(Node.class);
        presenter.updateNode(uiNode);
        verify(tracker).updateNode(argumentCaptor.capture());

    }

    @Test
    public void shouldDeleteNode() {
        uiNode = new UINode("child2", 1, root.getId());
        Node node = presenter.convertUINodeToModelNode(uiNode, root);
        tree.addNode(node);
        uiNode.setName("child2");
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        presenter.deleteNode(uiNode);
        verify(tracker).deleteNode(argumentCaptor.capture());

    }

    @Test
    public void shouldAddNodFromWebToParent() {

    }

}