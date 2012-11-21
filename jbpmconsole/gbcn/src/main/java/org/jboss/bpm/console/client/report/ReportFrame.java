package org.jboss.bpm.console.client.report;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
import java.util.Date;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;

public class ReportFrame extends MosaicPanel
{
  private Frame frame;

  public ReportFrame()
  {
    setPadding(0);

    this.frame = new Frame();
    DOM.setStyleAttribute(this.frame.getElement(), "border", "1px solid #E8E8E8");
    DOM.setStyleAttribute(this.frame.getElement(), "backgroundColor", "#ffffff");
    add(this.frame);
  }

  public void setFrameUrl(String url)
  {
    this.frame.getElement().setId(String.valueOf(new Date().getTime()));

    this.frame.setUrl(url);
  }
}