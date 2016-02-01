package com.thoughtworks.mindit.mindit.presenter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.test.ActivityInstrumentationTestCase2;

import com.thoughtworks.mindit.mindit.Tracker;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;
import com.thoughtworks.mindit.mindit.view.MindmapActivity;
import com.thoughtworks.mindit.mindit.view.model.UINode;

import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void shouldAddNode() {
        uiNode = new UINode("child2", 1, root.getId());

        // Node node = new Node("", uiNode.getName(), parent, rootId, 0);
        //Mockito.verify(tracker).addChild();
        //  when(tracker.addChild(any(Node)).thenReturn(5));
        //  assertEquals(5,presenter.addNode(uiNode));
        ArgumentCaptor<Node> argumentCaptor = ArgumentCaptor.forClass(Node.class);
        presenter.addNode(uiNode);
        verify(tracker).addChild(argumentCaptor.capture());
        //verify(tracker).addChild(Matchers.eq(argumentCaptor.capture()));
        assertEquals(uiNode.getId(), argumentCaptor.getValue().getId());
        // System.out.println("UINode:" + uiNode);
        //System.out.println("Node:" + argumentCaptor.getValue());
    }

}