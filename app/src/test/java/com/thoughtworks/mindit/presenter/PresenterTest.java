package com.thoughtworks.mindit.presenter;

import android.os.NetworkOnMainThreadException;

import com.thoughtworks.mindit.Tracker;
import com.thoughtworks.mindit.constant.Fields;
import com.thoughtworks.mindit.model.Node;
import com.thoughtworks.mindit.model.Tree;

import com.thoughtworks.mindit.view.IMindmapView;
import com.thoughtworks.mindit.view.model.UINode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PresenterTest {
    Presenter presenter;
    Node root;
    private UINode uiNode;
    private Tracker tracker;
    private Tree tree;
    private IMindmapView iMindmapView;


    @Before
    public void setUp() throws Exception {
        root = new Node("root", "root", null, "root", 0);
        tracker = Mockito.mock(Tracker.class);
        HashMap<String, Node> nodes = new HashMap<String, Node>();
        nodes.put(root.getId(), root);
        tree = Tree.getInstance(nodes);
        iMindmapView=Mockito.mock(IMindmapView.class);
        presenter = new Presenter(tracker,iMindmapView);
    }


    @Test
    public void shouldConvertModelNodeUINode() throws Exception {
        uiNode = presenter.convertModelNodeToUINode(root);
        assertEquals(root.getId(), uiNode.getId());
    }

    @Test
    public void shouldBuildNodeListFromTree() throws Exception {
        when(tracker.getTree()).thenReturn(tree);
        ArrayList<UINode> nodeList = presenter.buildNodeListFromTree();
        UINode uiRoot = nodeList.get(0);
        assertEquals(uiRoot.getId(), root.getId());
    }

    @Test
    public void shouldUpdateUIChildSubtree() throws Exception {
        when(tracker.getTree()).thenReturn(tree);
        Node child = new Node("2.1", "node2", root, root.getId(), 0);
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
        when(tracker.getTree()).thenReturn(tree);
        presenter.addNode(uiNode);
        verify(tracker).addChild(argumentCaptor.capture());
    }



    @Test
    public void shouldDeleteNode() {
        uiNode = new UINode("child2", 1, root.getId());
        Node node = presenter.convertUINodeToModelNode(uiNode, root);
        tree.addNode(node);
        uiNode.setName("child2");
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(tracker.getTree()).thenReturn(tree);
        presenter.deleteNode(uiNode);
        verify(tracker).deleteNode(argumentCaptor.capture());

    }

    @Test
    public void shouldAddNodFromWebToParent() {

// Node testNode = new Node("1", "TestNode", root, root.getId(), 0);
// Node child1 = new Node("2", "child1", testNode, root.getId(), 0);
// testNode.addThisChild(child1, 0);
// tree.addNode(testNode);
// tree.addNode(child1);
//
// HashMap<String, UINode> myNodeTree = presenter.getNodeTree();
// when(tracker.getTree()).thenReturn(tree);
// // when(tracker.getTree().getLastUpdatedNode()).thenReturn(testNode);
// UINode testUiNode = presenter.convertModelNodeToUINode(testNode);
// // when(myNodeTree.get(testNode)).thenReturn(testUiNode);
//// UINode child1UiNode=presenter.convertModelNodeToUINode(child1);
// myNodeTree.put(testUiNode.getId(), testUiNode);
//// myNodeTree.put(child1UiNode.getId(),child1UiNode);
//// assertEquals(presenter.);
// ArgumentCaptor<ArrayList> argumentCaptor = ArgumentCaptor.forClass(ArrayList.class);
//
// presenter.update(2, Fields.CHILD_SUBTREE);
// ArrayList<UINode> result = argumentCaptor.capture();
// //verify(testUiNode).setChildSubTree(result);
// assertEquals(2, result.size());
    }


    @Test
    public void shouldReturnCorrectLeftFirstNode() throws Exception {
        when(tracker.getTree()).thenReturn(tree);
        Node child1 = new Node("1", "child1", root, root.getId(), 0);
        Node child2 = new Node("2", "child2", root, root.getId(), 0);
// Node child3 = new Node("3", "child3", root, root.getId(), 0);
// Node child4 = new Node("4", "child4", root, root.getId(), 0);
// Node child5 = new Node("5", "child5", root, root.getId(), 0);
// if(tree.getNode(root.getId())==null)
// tree.addNode(root);
        tree.addNode(child1);
        tree.addNode(child2);
// tree.addNode(child3);
// tree.addNode(child4);
// tree.addNode(child5);
        ArrayList<UINode > list=presenter.buildNodeListFromTree();
        UINode leftNode=presenter.getLeftFirstNode();
        assertEquals(true, leftNode.getName().equals("child2"));

    }

    @Test
    public void shouldUpdateNode() {
        uiNode = new UINode("child2", 1, root.getId());
        Node node = presenter.convertUINodeToModelNode(uiNode, root);
        tree.addNode(node);
        uiNode.setName("child2");
        ArgumentCaptor<Node> argumentCaptor = ArgumentCaptor.forClass(Node.class);
        when(tracker.getTree()).thenReturn(tree);
        presenter.updateNode(uiNode);
        verify(tracker).updateNode(argumentCaptor.capture());

    }

    @After
    public void end(){
        Node.setDirectionToggler(true);
        tree.removeAllNodes();
    }
}