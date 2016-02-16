package com.thoughtworks.mindit;


import com.google.gson.JsonSyntaxException;
import com.thoughtworks.mindit.helper.JsonParserService;
import com.thoughtworks.mindit.model.Node;
import com.thoughtworks.mindit.model.Tree;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class JsonParserServiceTest {
    private JsonParserService jsonParserService;

    @Before
    public void initialize() {
        jsonParserService = new JsonParserService();
    }

    @Test(expected = JsonSyntaxException.class)
    public void shouldThrowExceptionForInvalidJsonInput() {
        String json = "{\"nodes\":{\"0\":{\"childSubTree\":[],\"depth\":,\"id\":\"0\",\"index\":0,\"name\":\"node0\"},\"1\":{\"childSubTree\":[],\"depth\":0,\"id\":\"1\",\"index\":1,\"name\":\"node1\"}}}";
        JsonParserService.parse(json);
    }

    @Test
    public void shouldCreateTreeForValidJsonInput() {
        String json = "[{\"_id\":\"c59W4KuwGJHhPockv\",\"name\":\"New Mindmap\",\"left\":[],\"right\":[\"eyvBxMYz2d7sukFJL\"],\"childSubTree\":[],\"position\":null,\"parentId\":null,\"rootId\":null},{\"_id\":\"eyvBxMYz2d7sukFJL\",\"name\":\"a\",\"left\":[],\"right\":[],\"childSubTree\":[],\"position\":\"right\",\"parentId\":\"c59W4KuwGJHhPockv\",\"rootId\":\"c59W4KuwGJHhPockv\",\"index\":0}]";
        Tree tree = JsonParserService.parse(json);
        assertNotNull(tree);
    }

    @Test
    public void shouldCreateNodeFromValidJason() {
        String jason = "{\"childSubTree\":[],\"depth\":0,\"id\":\"0\",\"index\":0,\"name\":\"node0\"}";
        Node node = JsonParserService.parseNode(jason);
        assertNotNull(node);
    }

}
