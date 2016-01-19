package com.thoughtworks.mindit.mindit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class JsonParserService {

    public static Tree parse (String json) throws JsonSyntaxException{
        Gson gson = new Gson();
        Tree tree;
        tree = gson.fromJson(json,Tree.class);
        return tree;

        /*Type type = new TypeToken<List<Node>>() {}.getType();
        Tree tree = null;
        List<Node> nodes = gson.fromJson(json, type);
        tree = convertToTree(nodes);
        return tree;*/


        /*Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray jArray = parser.parse(json).getAsJsonArray();

        ArrayList<Node> lcs = new ArrayList<channelSearchEnum>();

        for(JsonElement obj : jArray )
        {
            channelSearchEnum cse = gson.fromJson( obj , channelSearchEnum.class);
            lcs.add(cse);
        }*/

    }

    public static Node parseNode (String json) throws JsonSyntaxException{
        Gson gson = new Gson();
        Node node = null;
        node = gson.fromJson(json, Node.class);
        System.out.println("parsing: " + node.getRight());
        return node;
    }

    private static Tree convertToTree(List<Node> nodes) {
        System.out.println("nodes " + nodes);
        HashMap<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        return new Tree(nodeMap);
    }
}
