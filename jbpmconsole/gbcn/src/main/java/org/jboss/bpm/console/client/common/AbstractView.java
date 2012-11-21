package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.LazyPanel;

public abstract class AbstractView extends MosaicPanel
  implements ViewInterface, LazyPanel
{
  private AbstractImagePrototype icon;
  protected boolean initialized;

  protected AbstractImagePrototype getIcon()
  {
    return this.icon;
  }

  protected void setIcon(AbstractImagePrototype icon)
  {
    this.icon = icon;
  }

  public String getIconTitle()
  {
    HTML html = new HTML((this.icon != null ? this.icon.getHTML() + "&nbsp;" : "") + getTitle());

    return html.toString();
  }
}