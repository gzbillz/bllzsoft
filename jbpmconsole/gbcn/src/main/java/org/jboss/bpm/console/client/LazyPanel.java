package org.jboss.bpm.console.client;

public abstract interface LazyPanel
{
  public abstract boolean isInitialized();

  public abstract void initialize();
}