package com.thoughtworks.mindit.mindit.helper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.mindit.mindit.model.Node;
import com.thoughtworks.mindit.mindit.model.Tree;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class JsonParserService {

    public static Tree parse (String json) throws JsonSyntaxException{
        Gson gson = new Gson();

        Type type = new TypeToken<List<Node>>() {}.getType();
        Tree tree = null;
        List<Node> nodes = gson.fromJson(json, type);

        if (nodes != null && nodes.size() != 0)
            tree = convertToTree(nodes);
        if (tree != null) {
            tree.fillRootChildSubtree();
            tree.updateDepthOfAllNodes(tree.getRoot(), -1);
            tree.updatePositionOfAllNodes(tree.getRoot(), null);
        }
        return tree;
    }

    public static Node parseNode (String json) throws JsonSyntaxException{
        Gson gson = new Gson();
        Node node = null;
        node = gson.fromJson(json, Node.class);
        return node;
    }

    private static Tree convertToTree(List<Node> nodes) {
        HashMap<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        return Tree.getInstance(nodeMap);
    }

    public static JSONObject rawParse(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
      //  System.out.println("jsonobject: " + jsonObject);
        return jsonObject;
    }
}
