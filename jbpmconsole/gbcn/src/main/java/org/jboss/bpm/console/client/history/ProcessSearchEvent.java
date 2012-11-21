package org.jboss.bpm.console.client.history;

public class ProcessSearchEvent
{
  private String definitionKey;
  private String key;
  private String status;
  private long startTime;
  private long endTime;

  public String getDefinitionKey()
  {
    return this.definitionKey;
  }

  public void setDefinitionKey(String definitionKey) {
    this.definitionKey = definitionKey;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getStartTime() {
    return this.startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return this.endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }
}