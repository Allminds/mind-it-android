package com.thoughtworks.mindit.mindit;

import com.thoughtworks.mindit.mindit.exception.NodeAlreadyDeletedException;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TreeTest {
    private Node root;
    private Node child1;
    private Node child2;
    private Tree tree;

    @Before
    public void initialize() {
        root = new Node("rootId", "root", null, null, 0);
        child1 = new Node("child1Id", "child1", root, root.getId(), 0);
        child2 = new Node("child2Id", "child2", root, root.getId(), 1);
        HashMap nodes = new HashMap<>();
        nodes.put("rootId", root);
        tree = Tree.getInstance(nodes);
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
