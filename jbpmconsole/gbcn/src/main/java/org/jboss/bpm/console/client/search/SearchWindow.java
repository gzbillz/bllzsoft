package org.jboss.bpm.console.client.search;

import com.google.gwt.user.client.WindowCloseListener;
import org.gwt.mosaic.ui.client.WindowPanel;

@SuppressWarnings("deprecation")
public class SearchWindow
{
  private WindowPanel window;

  public SearchWindow(String title, SearchDefinitionView view)
  {
    view.setParent(this);
    createLayoutWindowPanel(title, view);
  }

 private void createLayoutWindowPanel(String title, SearchDefinitionView view)
  {
    this.window = new WindowPanel(title);
    this.window.setAnimationEnabled(true);
    this.window.setSize("250px", "160px");

    this.window.setWidget(view);

    this.window.addWindowCloseListener(new WindowCloseListener() {
      public void onWindowClosed() {
        SearchWindow.this.window = null;
      }

      public String onWindowClosing() {
        return null;
      }
    });
  }

  public void center()
  {
    this.window.center();
  }

  public void close()
  {
    this.window.hide();
    this.window = null;
  }
}