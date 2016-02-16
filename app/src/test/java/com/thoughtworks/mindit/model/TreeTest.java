package com.thoughtworks.mindit.model;

import com.thoughtworks.mindit.exception.NodeAlreadyDeletedException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TreeTest {
    private static Node root;
    private static Node child1;
    private static Node child2;
    private Tree tree;
    private static HashMap nodes;

    @BeforeClass
    public static void setUp(){
        root = new Node("rootId", "root", null, null, 0);
        child1 = new Node("child1Id", "child1", root, root.getId(), 0);
        child2 = new Node("child2Id", "child2", root, root.getId(), 1);
        nodes = new HashMap<>();
        nodes.put("rootId", root);
    }
    @Before
    public void initialize() {
        Tree.reset();
        tree = Tree.getInstance(nodes);
    }
    @After
    public void reset(){
        tree.reset();
        tree = null;
    }
    @Test
    public void shouldNodeToBeAdded() {
        tree.addNode(child1);
        tree.addNode(child2);
        assertEquals(true, tree.isAlreadyExists(child1));
    }

    @Test
    public void shouldAddRootNodeInTheTree() throws Exception {
        assertEquals(root, tree.getNode(root.getId()));
    }

    @Test
    public void shouldAddNodeOtherThanRootInTheTree() throws Exception {
        tree = tree.addNode(child1);
        assertEquals(child1, tree.getNode(child1.getId()));
        ArrayList<String> rootChildSubTree = tree.getNode(root.getId()).getChildSubTree();
        assertEquals(true, rootChildSubTree.contains(child1.getId()));
    }

    @Test
    public void shouldUpdateExistingNodeInsteadOfAddingNewNode() throws Exception {
        tree = tree.addNode(child1);
        child1.setName("child1New");
        tree = tree.addNode(child1);
        assertEquals(child1.getName(), tree.getNode(child1.getId()).getName());
    }

    @Test
    public void shouldNotDeleteRootNodeInTheTree() throws Exception {
        tree = tree.deleteNode(root);
        assertEquals(root, tree.getNode(root.getId()));
    }

    @Test
    public void shouldDeleteChildNodeInTheTree() throws Exception {
        tree = tree.addNode(child1);
        tree = tree.deleteNode(child1);
        assertEquals(null, tree.getNode(child1.getId()));
    }

    @Test(expected = NodeAlreadyDeletedException.class)
    public void shouldThrowExceptionForAlreadyDeletedNode() throws Exception {
        tree = tree.addNode(child1);
        tree = tree.deleteNode(child1);
        tree = tree.deleteNode(child1);
    }
}
