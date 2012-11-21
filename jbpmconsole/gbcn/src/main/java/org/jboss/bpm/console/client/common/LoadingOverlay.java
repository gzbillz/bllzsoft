package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.ui.client.infopanel.InfoPanel;

public class LoadingOverlay
{
  static InfoPanel p = null;

  public static void on(Widget parent, boolean loading)
  {
    if ((parent != null) && (loading))
    {
      int left = parent.getAbsoluteLeft();
      int top = parent.getAbsoluteTop();

      int width = parent.getOffsetWidth();
      int height = parent.getOffsetHeight();

      if (((width > 100 ? 1 : 0) & (height > 100 ? 1 : 0)) != 0)
      {
        p = new InfoPanel();
        p.setStylePrimaryName("bpm-loading-overlay");
        p.setWidget(new Image("images/loading_lite.gif"));
        p.setPopupPosition(left + width / 2 - 15, top + height / 2 - 15);
        p.show();
      }

    }
    else if (p != null)
    {
      p.hide();
      p = null;
    }
  }
}