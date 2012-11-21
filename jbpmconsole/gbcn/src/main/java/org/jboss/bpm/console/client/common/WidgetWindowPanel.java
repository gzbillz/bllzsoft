package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.core.client.Dimension;
import org.gwt.mosaic.ui.client.Caption.CaptionRegion;
import org.gwt.mosaic.ui.client.WindowPanel;
import org.jboss.bpm.console.client.util.WindowUtil;

public class WidgetWindowPanel
{
  private WindowPanel window;

  public WidgetWindowPanel(String title, Widget widget)
  {
    this(title, widget, false);
  }

  public WidgetWindowPanel(String title, Widget widget, boolean overlay)
  {
    this.window = new WindowPanel(title);
    this.window.setAnimationEnabled(true);
    this.window.setWidget(widget);

    WindowUtil.addMaximizeButton(this.window, CaptionRegion.RIGHT);
    WindowUtil.addMinimizeButton(this.window, CaptionRegion.RIGHT);

    this.window.pack();

    if (overlay)
    {
      int width = Window.getClientWidth() - 120;
      int height = Window.getClientHeight() - 80;

      this.window.setContentSize(new Dimension(width, height));
      this.window.setPopupPosition(60, 40);

      this.window.show();
    }
    else
    {
      this.window.center();
    }
  }

  public void close()
  {
    this.window.hide();
  }

  public void setSize(Dimension dimension)
  {
    this.window.setContentSize(dimension);
  }
}