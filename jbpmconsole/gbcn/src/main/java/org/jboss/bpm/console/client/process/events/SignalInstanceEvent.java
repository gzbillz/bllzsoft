package org.jboss.bpm.console.client.process.events;

import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.model.TokenReference;

public class SignalInstanceEvent extends InstanceEvent
{
  private String signalName;
  private TokenReference token;
  private int index;

  public int getIndex()
  {
    return this.index;
  }

  public SignalInstanceEvent(ProcessDefinitionRef definition, ProcessInstanceRef instance, TokenReference token, String signalName, int index) {
    super(definition, instance);

    this.signalName = signalName;
    this.token = token;
    this.index = index;
  }

  public String getSignalName() {
    return this.signalName;
  }

  public TokenReference getToken()
  {
    return this.token;
  }
}