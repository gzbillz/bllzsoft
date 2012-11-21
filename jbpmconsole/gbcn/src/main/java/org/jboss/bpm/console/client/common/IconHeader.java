package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;

public class IconHeader extends MosaicPanel
{
  public IconHeader(AbstractImagePrototype icon, String title)
  {
    super(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    setPadding(5);

    add(icon.createImage());
    add(new HTML("<b>" + title + "</b>"));
  }
}