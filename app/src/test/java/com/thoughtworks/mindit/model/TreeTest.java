package com.thoughtworks.mindit.model;

import com.thoughtworks.mindit.exception.NodeAlreadyDeletedException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TreeTest {
    private static Node root;
    private static Node child1;
    private static Node child2;
    private static HashMap nodes;
    private Tree tree;

    @BeforeClass
    public static void setUp() {
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
    public void reset() {
        tree.reset();
        tree = null;
    }

    @Test
    public void shouldNodeToBeAdded() {
        tree.addNode(child1);
        tree.addNode(child2);
        assertNotEquals(tree.getNode(child1.getId()).getName(),tree.getNode(child2.getId()).getName());
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
        Node child1Duplicate = new Node("child1Id", "child1Duplicate", root, root.getId(), 0);
        tree = tree.addNode(child1Duplicate);
        assertEquals(child1Duplicate.getName(), tree.getNode(child1.getId()).getName());
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

    @Test
    public void testFillRootChildSubtree() throws Exception {
        ArrayList<String> left = new ArrayList<>();
        left.add(child1.getId());
        ArrayList<String> right = new ArrayList<>();
        right.add(child2.getId());
        ArrayList<String> childSubTree = (ArrayList<String>) left.clone();
        childSubTree.addAll(right);
        tree.addNode(child1);
        tree.addNode(child2);
        tree.fillRootChildSubtree();
        assertEquals(childSubTree,tree.getRoot().getChildSubTree());
    }

    @Test
    public void testUpdateDepthOfAllNodes() throws Exception {
        tree.addNode(child1);
        tree.addNode(child2);
        child1.addThisChild(child2,0);
        tree.updateDepthOfAllNodes(child1,0);
        assertEquals(2,child2.getDepth());
    }

    @Test
    public void testAddNode() throws Exception {
        tree.addNode(child1);
        assertEquals(child1,tree.getNode(child1.getId()));
    }

    @Test
    public void testAddNodeFromWeb() throws Exception {
        tree.addNode(child1);
        assertEquals(child1,tree.getNode(child1.getId()));
    }

    @Test
    public void testUpdateNode() throws Exception {
        tree.addNode(child1);
        tree.addNode(child2);
        root.addThisChild(child1, 0);
        root.addThisChild(child2, 1);
        tree.updateNode(child1, "name", "gopinath");
        assertEquals("gopinath", tree.getNode(child1.getId()).getName());

        tree.updateNode(child2, "parentId", child1.getId());
        assertEquals(child1.getId(), child2.getParentId());


        child1.addThisChild(child2,0);
        ArrayList<String> childSubTree = new ArrayList<>();
        childSubTree.add(child2.getId());
        assertEquals(childSubTree,child1.getChildSubTree());
    }

    @Test
    public void testDeleteNode() throws Exception {
        tree.addNode(child1);
        tree.deleteNode(child1);
        assertEquals(null,tree.getNode(child1.getId()));
    }
}
