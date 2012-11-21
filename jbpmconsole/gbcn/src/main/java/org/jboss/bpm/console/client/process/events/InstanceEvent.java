package org.jboss.bpm.console.client.process.events;

import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;

public class InstanceEvent
{
  private ProcessDefinitionRef definition;
  private ProcessInstanceRef instance;

  public InstanceEvent(ProcessDefinitionRef definition, ProcessInstanceRef instance)
  {
    this.definition = definition;
    this.instance = instance;
  }

  public ProcessDefinitionRef getDefinition()
  {
    return this.definition;
  }

  public ProcessInstanceRef getInstance()
  {
    return this.instance;
  }
}