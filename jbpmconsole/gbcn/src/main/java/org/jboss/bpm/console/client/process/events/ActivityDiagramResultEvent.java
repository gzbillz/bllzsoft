package org.jboss.bpm.console.client.process.events;

import java.util.List;
import org.jboss.bpm.console.client.model.ActiveNodeInfo;

public class ActivityDiagramResultEvent
{
  private String imageUrl;
  private List<ActiveNodeInfo> activeNodeInfo;

  public ActivityDiagramResultEvent()
  {
  }

  public ActivityDiagramResultEvent(String imageUrl, List<ActiveNodeInfo> activeNodeInfo)
  {
    this.imageUrl = imageUrl;
    this.activeNodeInfo = activeNodeInfo;
  }

  public String getImageUrl()
  {
    return this.imageUrl;
  }

  public void setImageUrl(String imageUrl)
  {
    this.imageUrl = imageUrl;
  }

  public List<ActiveNodeInfo> getActiveNodeInfo()
  {
    return this.activeNodeInfo;
  }

  public void setActiveNodeInfo(List<ActiveNodeInfo> activeNodeInfo)
  {
    this.activeNodeInfo = activeNodeInfo;
  }
}