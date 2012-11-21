package org.jboss.bpm.console.client;

public abstract interface ApplicationContext
{
  public abstract void displayMessage(String paramString, boolean paramBoolean);

  public abstract Authentication getAuthentication();

  public abstract ConsoleConfig getConfig();

  public abstract void refreshView();
}