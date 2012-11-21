package org.jboss.bpm.console.client.process.v2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;

public class ProcessGroups
{
  Map<String, Set<ProcessDefinitionRef>> groups = new HashMap<String, Set<ProcessDefinitionRef>>();

  public ProcessGroups(List<ProcessDefinitionRef> processDefinitions)
  {
    for (ProcessDefinitionRef def : processDefinitions)
    {
      Set<ProcessDefinitionRef> subset = (Set<ProcessDefinitionRef>)this.groups.get(def.getName());
      if (null == subset)
      {
        subset = new HashSet<ProcessDefinitionRef>();
        this.groups.put(def.getName(), subset);
      }

      subset.add(def);
    }
  }

  public Set<String> getGroups()
  {
    return this.groups.keySet();
  }

  public Set<ProcessDefinitionRef> getProcessesForGroup(String name)
  {
    return (Set<ProcessDefinitionRef>)this.groups.get(name);
  }
}