package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowCloseListener;
import com.google.gwt.user.client.ui.Frame;
import java.util.Date;
import org.gwt.mosaic.core.client.Dimension;
import org.gwt.mosaic.ui.client.Caption.CaptionRegion;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.WindowPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.jboss.bpm.console.client.util.WindowUtil;

@SuppressWarnings("deprecation")
public class IFrameWindowPanel
{
  private WindowPanel windowPanel = null;
  private Frame frame = null;
  private String url;
  private String title;
  private IFrameWindowCallback callback = null;

  public IFrameWindowPanel(String url, String title)
  {
    this.url = url;
    this.title = title;
  }

  private void createWindow()
  {
    this.windowPanel = new WindowPanel();
    this.windowPanel.setAnimationEnabled(true);

    ScrollLayoutPanel layout = new ScrollLayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    layout.setStyleName("bpm-window-layout");
    layout.setPadding(5);

    HeaderLabel header = new HeaderLabel(this.title, true);

    layout.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    this.windowPanel.addWindowCloseListener(new WindowCloseListener() {
      public void onWindowClosed() {
        if (IFrameWindowPanel.this.getCallback() != null) {
          IFrameWindowPanel.this.getCallback().onWindowClosed();
        }
        IFrameWindowPanel.this.windowPanel = null;
        IFrameWindowPanel.this.frame = null;
      }

      public String onWindowClosing() {
        return null;
      }
    });
    this.frame = new Frame()
    {
    };
    DOM.setStyleAttribute(this.frame.getElement(), "border", "none");

    this.frame.getElement().setId(String.valueOf(new Date().getTime()));

    this.frame.setUrl(this.url);

    layout.add(this.frame, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
    this.windowPanel.setWidget(layout);

    WindowUtil.addMaximizeButton(this.windowPanel, CaptionRegion.RIGHT);
    WindowUtil.addMinimizeButton(this.windowPanel, CaptionRegion.RIGHT);

    int width = Window.getClientWidth() - 200;
    int height = Window.getClientHeight() - 100;
    this.windowPanel.setContentSize(new Dimension(width, height));
    this.windowPanel.center();
  }

  private void destroyWindow()
  {
    this.windowPanel.hide();
  }

  public Frame getFrame()
  {
    return this.frame;
  }

  public WindowPanel getWindowPanel()
  {
    return this.windowPanel;
  }

  public void setCallback(IFrameWindowCallback callback)
  {
    this.callback = callback;
  }

  private IFrameWindowCallback getCallback()
  {
    return this.callback;
  }

  public native String getContents(com.google.gwt.dom.client.Element paramElement);

  public void show()
  {
    createWindow();
  }
}