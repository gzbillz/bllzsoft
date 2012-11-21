package org.jboss.bpm.console.client.monitor;

import org.jboss.bpm.monitor.gui.client.TimespanValues;

public class LoadDatasetEvent
{
  private String definitionId;
  private TimespanValues timespan;
  private boolean includedFailed;

  public String getDefinitionId()
  {
    return this.definitionId;
  }

  public void setDefinitionId(String definitionId) {
    this.definitionId = definitionId;
  }

  public TimespanValues getTimespan() {
    return this.timespan;
  }

  public void setTimespan(TimespanValues timespan) {
    this.timespan = timespan;
  }

  public boolean isIncludedFailed() {
    return this.includedFailed;
  }

  public void setIncludedFailed(boolean includedFailed) {
    this.includedFailed = includedFailed;
  }
}