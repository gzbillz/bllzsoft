package org.jboss.bpm.console.client.search;

public abstract interface SearchDelegate
{
  public abstract void handleResult(String paramString);

  public abstract String getActionName();
}