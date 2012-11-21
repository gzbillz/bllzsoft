package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.gwt.mosaic.ui.client.layout.ColumnLayout;
import org.gwt.mosaic.ui.client.layout.ColumnLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;

public class ListBoxToolbar extends MosaicPanel
{
  private HorizontalPanel toolPanel;

  public ListBoxToolbar(String title)
  {
    super(new ColumnLayout());

    setStyleName("bpm-toolbar-panel");

    this.toolPanel = new HorizontalPanel();

    add(new HTML("<b>" + title + "</b>"), new ColumnLayoutData("80%"));
    add(this.toolPanel, new ColumnLayoutData("20%"));
  }

  public HorizontalPanel getToolPanel()
  {
    return this.toolPanel;
  }
}