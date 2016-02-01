package com.thoughtworks.mindit.mindit.model;
import com.thoughtworks.mindit.mindit.Constants;
import com.thoughtworks.mindit.mindit.exception.NodeDoesNotExistException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
public class NodeTest {
    private Node root,child1,child2 ;

    @Before
    public void initialize(){
        root=new Node("123","node1",null,"123",0);
        child1 = new Node("child1Id", "child1", root, root.getId(), 0);
        child2 = new Node("child2Id", "child2", root, root.getId(), 1);
    }
    @Test
    public void shouldAddThisChild(){
        assertEquals(root, root.addThisChild(child1, 0));
    }
    @Test (expected= NodeDoesNotExistException.class)
    public void shouldNotRemoveNonExistingChild() throws Exception{
        root.removeThisChild(child1);
    }

    @Test
    public void shouldBeARoot(){
        assertEquals(true, root.isARoot());
    }

    @Test
    public void shouldNotBeARoot(){
        assertEquals(false,child1.isARoot());
    }

    @Test
    public void shouldAddThisChildToLeftSubtree(){
        child1.setPosition(Constants.POSITION.LEFT.toString());
        root.addThisChild(child1,0);
        assertEquals(true,root.getLeft().contains(child1.getId()));
    }
    @Test
    public void shouldNotAddThisChildToLeftSubtree(){
        child1.setPosition(Constants.POSITION.RIGHT.toString());
        root.addThisChild(child1, 0);
        assertEquals(false,root.getLeft().contains(child1.getId()));
    }
    @Test
    public void shouldAddThisChildToRightSubtree(){
        child1.setPosition(Constants.POSITION.RIGHT.toString());
        root.addThisChild(child1, 0);
        assertEquals(true,root.getRight().contains(child1.getId()));
    }
    @Test
    public void shouldNotAddThisChildToRightSubtree(){
        child1.setPosition(Constants.POSITION.LEFT.toString());
        root.addThisChild(child1, 0);
        assertEquals(true,root.getLeft().contains(child1.getId()));
    }

    @Test
    public void shouldRemoveThisChild() throws NodeDoesNotExistException{
        root.addThisChild(child1, 0);
        root.removeThisChild(child1);
        assertEquals(false, root.getChildSubTree().contains(child1.getId()));
    }

    @Test (expected= NodeDoesNotExistException.class)
    public void shouldNotRemoveThisChildTwice()throws NodeDoesNotExistException{
        root.addThisChild(child1, 0);
        root.removeThisChild(child1);
        root.removeThisChild(child1);
    }

}
