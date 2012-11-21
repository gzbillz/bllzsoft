package org.jboss.bpm.console.client.model;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.bpm.console.client.util.JSONWalk;

public class DTOParser
{
  public static List<TaskRef> parseTaskReferenceList(String json)
  {
    ConsoleLog.debug("parse " + json);

    List<TaskRef> results = new ArrayList<TaskRef>();

    JSONValue root = JSONParser.parse(json);
    JSONArray array = JSONWalk.on(root).next("tasks").asArray();

    for (int i = 0; i < array.size(); i++)
    {
      JSONObject item = array.get(i).isObject();
      TaskRef ref = parseTaskReference(item);
      results.add(ref);
    }

    return results;
  }

  public static TaskRef parseTaskReference(JSONObject item)
  {
    ConsoleLog.debug("parse " + item);

    long id = JSONWalk.on(item).next("id").asLong();

    JSONWalk.JSONWrapper instanceIdWrapper = JSONWalk.on(item).next("processInstanceId");
    String executionId = instanceIdWrapper != null ? instanceIdWrapper.asString() : "n/a";

    JSONWalk.JSONWrapper processIdWrapper = JSONWalk.on(item).next("processId");
    String processId = processIdWrapper != null ? processIdWrapper.asString() : "n/a";

    String name = JSONWalk.on(item).next("name").asString();
    String assignee = JSONWalk.on(item).next("assignee").asString();
    boolean isBlocking = JSONWalk.on(item).next("isBlocking").asBool();
    boolean isSignalling = JSONWalk.on(item).next("isSignalling").asBool();

    TaskRef ref = new TaskRef(id, executionId, processId, name, assignee, isSignalling, isBlocking);

    ref.setBlocking(isBlocking);
    ref.setSignalling(isSignalling);

    JSONWalk.JSONWrapper jsonWrapper = JSONWalk.on(item).next("url");
    if (jsonWrapper != null)
    {
      String url = jsonWrapper.asString();
      ref.setUrl(url);
    }
    else
    {
      ref.setUrl("");
    }

    JSONArray arrUsers = JSONWalk.on(item).next("participantUsers").asArray();
    for (int k = 0; k < arrUsers.size(); k++)
    {
      JSONValue jsonValue = arrUsers.get(k);
      ParticipantRef p = parseParticipant(jsonValue, k);
      ref.getParticipantUsers().add(p);
    }

    JSONArray arrGroups = JSONWalk.on(item).next("participantGroups").asArray();
    for (int k = 0; k < arrGroups.size(); k++)
    {
      JSONValue jsonValue = arrGroups.get(k);
      ParticipantRef p = parseParticipant(jsonValue, k);
      ref.getParticipantGroups().add(p);
    }

    if (isSignalling)
    {
      JSONArray arr = JSONWalk.on(item).next("outcomes").asArray();
      for (int k = 0; k < arr.size(); k++)
      {
        JSONValue jsonValue = arr.get(k);
        if (jsonValue.toString().equals("null"))
        {
          ConsoleLog.warn("FIXME JBPM-1828: Null value on outcomes:" + arr.toString());
        }
        else {
          JSONString t = jsonValue.isString();
          ref.getOutcomes().add(t.stringValue());
        }
      }
    }

    int prio = JSONWalk.on(item).next("priority").asInt();
    ref.setPriority(prio);

    JSONWalk.JSONWrapper dueDate = JSONWalk.on(item).next("dueDate");
    if (dueDate != null)
    {
      Date due = dueDate.asDate();
      ref.setDueDate(due);
    }

    JSONWalk.JSONWrapper createDate = JSONWalk.on(item).next("createDate");
    if (createDate != null)
    {
      Date due = createDate.asDate();
      ref.setDueDate(due);
    }

    return ref;
  }

  private static ParticipantRef parseParticipant(JSONValue jsonValue, int k)
  {
    String type = JSONWalk.on(jsonValue).next("type").asString();
    String idRef = JSONWalk.on(jsonValue).next("idRef").asString();
    ParticipantRef p = new ParticipantRef(type, idRef);

    boolean isGroup = JSONWalk.on(jsonValue).next("isGroup").asBool();
    p.setGroup(isGroup);

    return p;
  }

  public static List<ProcessInstanceRef> parseProcessInstances(JSONValue jso)
  {
    List<ProcessInstanceRef> results = new ArrayList<ProcessInstanceRef>();

    JSONArray arr = JSONWalk.on(jso).next("instances").asArray();
    for (int i = 0; i < arr.size(); i++)
    {
      results.add(parseProcessInstance(arr.get(i)));
    }
    return results;
  }

  public static ProcessInstanceRef parseProcessInstance(JSONValue root)
  {
    ConsoleLog.debug("parse " + root);

    String id = JSONWalk.on(root).next("id").asString();
    String definitionId = JSONWalk.on(root).next("definitionId").asString();
    Date start = JSONWalk.on(root).next("startDate").asDate();

    JSONWalk.JSONWrapper endDateJSON = JSONWalk.on(root).next("endDate");
    Date end = null;
    if (endDateJSON != null) {
      end = endDateJSON.asDate();
    }
    boolean suspended = JSONWalk.on(root).next("suspended").asBool();

    ProcessInstanceRef processInstance = new ProcessInstanceRef(id, definitionId, start, end, suspended);

    JSONWalk.JSONWrapper rootTokenJSON = JSONWalk.on(root).next("rootToken");
    if (rootTokenJSON != null) {
      JSONObject tokJso = rootTokenJSON.asObject();

      TokenReference rootToken = parseTokenReference(tokJso);
      processInstance.setRootToken(rootToken);
    }

    return processInstance;
  }

  public static TokenReference parseTokenReference(JSONObject jso)
  {
    ConsoleLog.debug("parse " + jso);

    String rootTokenId = JSONWalk.on(jso).next("id").asString();

    JSONWalk.JSONWrapper nodeNameWrapper = JSONWalk.on(jso).next("currentNodeName");
    String nodeName = nodeNameWrapper != null ? nodeNameWrapper.asString() : "";

    TokenReference rt = new TokenReference(rootTokenId, "", nodeName);

    boolean canBeSignaled = JSONWalk.on(jso).next("canBeSignaled").asBool();
    rt.setCanBeSignaled(canBeSignaled);

    JSONArray signals = JSONWalk.on(jso).next("availableSignals").asArray();
    for (int i = 0; i < signals.size(); i++)
    {
      JSONValue jsonValue = signals.get(i);
      if (jsonValue.toString().equals("null"))
      {
        ConsoleLog.warn("FIXME JBPM-1828: Null value on availableSignals:" + signals.toString());
      }
      else {
        JSONString item = jsonValue.isString();
        rt.getAvailableSignals().add(item.stringValue());
      }
    }
    JSONArray childArr = JSONWalk.on(jso).next("children").asArray();
    for (int i = 0; i < childArr.size(); i++)
    {
      JSONObject item = childArr.get(i).isObject();
      rt.getChildren().add(parseTokenReference(item));
    }

    return rt;
  }

  public static List<String> parseStringArray(JSONValue jso)
  {
    List<String> result = new ArrayList<String>();

    JSONArray jsonArray = jso.isArray();

    if (null == jsonArray) {
      throw new IllegalArgumentException("Not an array: " + jso);
    }
    for (int i = 0; i < jsonArray.size(); i++)
    {
      JSONValue jsonValue = jsonArray.get(i);
      if (jsonValue.toString().equals("null"))
      {
        ConsoleLog.warn("FIXME JBPM-1828: Null value on string array:" + jsonArray.toString());
      }
      else {
        JSONString item = jsonValue.isString();
        result.add(item.stringValue());
      }
    }
    return result;
  }

  public static List<DeploymentRef> parseDeploymentRefList(JSONValue json)
  {
    ConsoleLog.debug("parse " + json);

    List<DeploymentRef> result = new ArrayList<DeploymentRef>();

    JSONArray jsonArray = JSONWalk.on(json).next("deployments").asArray();
    for (int i = 0; i < jsonArray.size(); i++)
    {
      JSONValue item = jsonArray.get(i);
      String id = JSONWalk.on(item).next("id").asString();
      boolean suspended = JSONWalk.on(item).next("suspended").asBool();

      String name = JSONWalk.on(item).next("name").asString();
      JSONWalk.JSONWrapper tsWrapper = JSONWalk.on(item).next("timestamp");
      long ts = tsWrapper != null ? tsWrapper.asLong() : -1L;

      DeploymentRef ref = new DeploymentRef(id, suspended);
      ref.setName(name);
      ref.setTimestamp(ts);

      JSONArray defArr = JSONWalk.on(item).next("definitions").asArray();
      for (int c = 0; c < defArr.size(); c++)
      {
        String defId = defArr.get(c).isString().stringValue();
        ref.getDefinitions().add(defId);
      }

      JSONArray resArr = JSONWalk.on(item).next("resourceNames").asArray();
      for (int c = 0; c < resArr.size(); c++)
      {
        String resourceName = resArr.get(c).isString().stringValue();
        ref.getResourceNames().add(resourceName);
      }

      result.add(ref);
    }

    return result;
  }

  public static List<JobRef> parseJobRefList(JSONValue json)
  {
    ConsoleLog.debug("parse " + json);

    List<JobRef> result = new ArrayList<JobRef>();

    JSONArray jsonArray = JSONWalk.on(json).next("jobs").asArray();
    for (int i = 0; i < jsonArray.size(); i++)
    {
      JSONValue item = jsonArray.get(i);
      String id = JSONWalk.on(item).next("id").asString();
      String type = JSONWalk.on(item).next("type").asString();

      JobRef ref = new JobRef();
      ref.setId(id);
      ref.setType(type);

      JSONWalk.JSONWrapper tsEl = JSONWalk.on(item).next("timestamp");
      if (tsEl != null) {
        ref.setTimestamp(tsEl.asLong());
      }
      JSONWalk.JSONWrapper err = JSONWalk.on(item).next("errMsg");
      if (err != null) {
        ref.setErrMsg(err.asString());
      }
      result.add(ref);
    }

    return result;
  }

  public static List<ActiveNodeInfo> parseActiveNodeInfo(String json) {
    ConsoleLog.debug("Parse: " + json);

    List<ActiveNodeInfo> activeNodeInfos = new ArrayList<ActiveNodeInfo>();
    JSONValue root = JSONParser.parse(json);

    if ((root instanceof JSONArray)) {
      JSONArray array = (JSONArray)root;

      for (int i = 0; i < array.size(); i++) {
        JSONWalk walk = JSONWalk.on(array.get(i));
        JSONWalk.JSONWrapper wrapper = walk.next("activeNode");
        JSONObject activeNode = wrapper.asObject();

        int x = JSONWalk.on(activeNode).next("x").asInt();
        int y = JSONWalk.on(activeNode).next("y").asInt();

        int width = JSONWalk.on(activeNode).next("width").asInt();
        int height = JSONWalk.on(activeNode).next("height").asInt();
        String name = JSONWalk.on(activeNode).next("name").asString();

        activeNodeInfos.add(new ActiveNodeInfo(-1, -1, new DiagramNodeInfo(name, x, y, width, height)));

        wrapper = walk.next("activeNode");
      }
    }
    return activeNodeInfos;
  }
}