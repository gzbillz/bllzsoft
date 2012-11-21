package org.jboss.bpm.console.client.common;

public abstract interface DataDriven
{
  public abstract void reset();

  public abstract void update(Object[] paramArrayOfObject);

  public abstract void setLoading(boolean paramBoolean);
}