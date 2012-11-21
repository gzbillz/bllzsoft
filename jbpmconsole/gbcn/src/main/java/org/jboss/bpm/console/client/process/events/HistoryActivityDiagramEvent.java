package org.jboss.bpm.console.client.process.events;

import java.util.List;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;

public class HistoryActivityDiagramEvent
{
  private ProcessDefinitionRef definition;
  private List<String> executedActivities;

  public HistoryActivityDiagramEvent(ProcessDefinitionRef definition, List<String> executedActivities)
  {
    this.definition = definition;
    this.executedActivities = executedActivities;
  }

  public ProcessDefinitionRef getDefinition()
  {
    return this.definition;
  }

  public List<String> getExecutedActivities()
  {
    return this.executedActivities;
  }
}