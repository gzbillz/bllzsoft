package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.ui.Label;

public class HeaderLabel extends Label
{
  public HeaderLabel(String string)
  {
    super(string);
    applyStyle(false);
  }

  public HeaderLabel(String string, boolean invert)
  {
    super(string);
    applyStyle(invert);
  }

  private void applyStyle(boolean invert)
  {
    if (invert)
      setStyleName("bpm-label-header-invert");
    else
      setStyleName("bpm-label-header");
  }
}