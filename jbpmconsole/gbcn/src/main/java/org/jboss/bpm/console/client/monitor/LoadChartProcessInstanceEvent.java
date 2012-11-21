package org.jboss.bpm.console.client.monitor;

import java.util.Date;
import org.jboss.bpm.monitor.gui.client.TimespanValues;

public class LoadChartProcessInstanceEvent
{
  public static final int DATASET_COMPLETED = 0;
  public static final int DATASET_FAILED = 1;
  public static final int DATASET_TERMINATED = 2;
  private String definitionId;
  private int datasetType;
  private Date date;
  private TimespanValues timespan;

  public String getDefinitionId()
  {
    return this.definitionId;
  }

  public void setDefinitionId(String definitionId) {
    this.definitionId = definitionId;
  }

  public int getDatasetType() {
    return this.datasetType;
  }

  public void setDatasetType(int datasetType) {
    this.datasetType = datasetType;
  }

  public Date getDate() {
    return this.date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public TimespanValues getTimespan() {
    return this.timespan;
  }

  public void setTimespan(TimespanValues timespan) {
    this.timespan = timespan;
  }
}