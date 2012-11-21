package org.jboss.bpm.console.client.report;

public final class RenderDispatchEvent
{
  String title;
  String dispatchUrl;
  String parameters = "None";

  public RenderDispatchEvent(String targetView, String dispatchUrl)
  {
    this.title = targetView;
    this.dispatchUrl = dispatchUrl;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getDispatchUrl()
  {
    return this.dispatchUrl;
  }

  public void setParameters(String parameters)
  {
    this.parameters = parameters;
  }

  public String getParameters()
  {
    return this.parameters;
  }
}