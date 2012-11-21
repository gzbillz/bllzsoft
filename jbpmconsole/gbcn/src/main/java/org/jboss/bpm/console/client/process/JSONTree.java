package org.jboss.bpm.console.client.process;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class JSONTree extends ScrollPanel
{
  String json = null;

  public JSONTree(String json)
  {
    this.json = json;

    Tree tree = new Tree();
    TreeItem root = tree.addItem("message");

    parseObject(root, "root", JSONParser.parse(json));

    add(tree);
  }

  private void parseValue(TreeItem root, String key, JSONValue jsonValue)
  {
    if (jsonValue.isBoolean() != null)
    {
      TreeItem treeItem = root.addItem(key);
      treeItem.addItem(jsonValue.isBoolean().toString());
    }
    else if (jsonValue.isNumber() != null)
    {
      TreeItem fastTreeItem = root.addItem(key);
      fastTreeItem.addItem(jsonValue.isNumber().toString());
    }
    else if (jsonValue.isString() != null)
    {
      TreeItem treeItem = root.addItem(key);
      treeItem.addItem(jsonValue.isString().toString());
    }
    else
    {
      ConsoleLog.warn("Unexpected JSON value: " + jsonValue);
    }
  }

  private void parseArray(TreeItem root, String key, JSONValue jsonValue)
  {
  }

  private void parseObject(TreeItem root, String key, JSONValue topLevel)
  {
    JSONObject rootJSO = topLevel.isObject();
    if (null == rootJSO) {
      throw new IllegalArgumentException("Not a JSON object: " + topLevel);
    }
    for (String innerKey : rootJSO.keySet())
    {
      JSONValue jsonValue = rootJSO.get(innerKey);
      if (jsonValue.isObject() != null)
      {
        parseObject(root, innerKey, jsonValue);
      }
      else if (jsonValue.isArray() != null)
      {
        parseArray(root, innerKey, jsonValue);
      }
      else
      {
        parseValue(root, innerKey, jsonValue);
      }
    }
  }
}