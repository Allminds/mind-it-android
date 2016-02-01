package com.thoughtworks.mindit.mindit;

import com.google.gson.JsonSyntaxException;
import com.thoughtworks.mindit.mindit.helper.JsonParserService;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class JsonParserServiceTest {
    JsonParserService jsonParserService;
    @Before
    public void initialize(){
        jsonParserService=new JsonParserService();
    }
    @Test(expected = JsonSyntaxException.class)
    public void shouldThrowExceptionForInvalidJsonInput(){
        String json = "{\"nodes\":{\"0\":{\"childSubTree\":[],\"depth\":,\"id\":\"0\",\"index\":0,\"name\":\"node0\"},\"1\":{\"childSubTree\":[],\"depth\":0,\"id\":\"1\",\"index\":1,\"name\":\"node1\"}}}";
        Tree tree = jsonParserService.parse(json);
    }
    @Test
    public void shouldCreateTreeForValidJsonInput(){
        String json = "{\"nodes\":{\"0\":{\"childSubTree\":[],\"depth\":0,\"id\":\"0\",\"index\":0,\"name\":\"node0\"},\"1\":{\"childSubTree\":[],\"depth\":0,\"id\":\"1\",\"index\":1,\"name\":\"node1\"}}}";
        Tree tree = jsonParserService.parse(json);
        assertNotNull(tree);
    }

    @Test
    public void shouldCreateNodeFromValidJason(){
        String jason="{\"childSubTree\":[],\"depth\":0,\"id\":\"0\",\"index\":0,\"name\":\"node0\"}";
        Node node=jsonParserService.parseNode(jason);
        assertNotNull(node);
    }

//    @Test
//    public void shouldCreateFieldFromRawParsing()throws JSONException{
//        String jason="{\"name\":\"bx\",\"position\":\"left\"}";
//        JSONObject jsonObject=jsonParserService.rawParse(jason);
//        assertEquals(true,jsonObject.has("name"));
//        assertEquals("node0",jsonObject.getString("name"));
//    }
}
