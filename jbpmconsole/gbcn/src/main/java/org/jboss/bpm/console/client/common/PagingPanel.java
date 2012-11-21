package org.jboss.bpm.console.client.common;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gwt.mosaic.ui.client.Caption;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.util.ButtonHelper;

public class PagingPanel extends MosaicPanel
{
  @SuppressWarnings("unused")
private PagingCallback callback;
  private int limit = 12;
  private int page = 0;

  private boolean leftBounds = true;
  private boolean rightBounds;
  private Button revBtn;
  private Button ffwBtn;

  public PagingPanel(final PagingCallback callback)
  {
    super(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    setStyleName("bpm-paging-panel");

    this.callback = callback;

    ClickHandler clickHandler = new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
     
//        PagingPanel.access$010(PagingPanel.this);
        PagingPanel.this.rightBounds = false;
        callback.rev();
      }
    };
    this.revBtn = new Button(ButtonHelper.createButtonLabel(Caption.IMAGES.toolCollapseLeft(), "", ButtonHelper.ButtonLabelType.NO_TEXT), clickHandler);

    add(this.revBtn);

    ClickHandler clickHandler2 = new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      { 
//        PagingPanel.access$008(PagingPanel.this);
        PagingPanel.this.leftBounds = false;
        callback.ffw();
      }
    };
    this.ffwBtn = new Button(ButtonHelper.createButtonLabel(Caption.IMAGES.toolCollapseRight(), "", ButtonHelper.ButtonLabelType.NO_TEXT), clickHandler2);

    add(this.ffwBtn);
  }

  public void reset()
  {
    this.leftBounds = true;
    this.rightBounds = false;
    this.page = 0;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
public List trim(List tmp)
  {
    List trimmed = new ArrayList();

    int size = tmp.size();
    int begin;
    int end;
    int i;
    Iterator i$;
    if (this.limit >= size)
    {
      trimmed = tmp;
      this.rightBounds = true;
    }
    else
    {
      if (this.page <= 0)
      {
        this.page = 0;
        this.leftBounds = true;
      }

      begin = 0;
      end = 0;

      if (this.page * this.limit >= size)
      {
        begin = (this.page - 1) * this.limit;
        this.rightBounds = true;
      }
      else {
        begin = this.page * this.limit;
      }
      if (begin + this.limit >= size)
      {
        end = size;
        this.rightBounds = true;
      }
      else {
        end = begin + this.limit;
      }

      i = 0;
      for (i$ = tmp.iterator(); i$.hasNext(); ) { Object o = i$.next();

        if ((i >= begin) && (i < end)) trimmed.add(o);
        i++;
      }

    }

    this.revBtn.setEnabled(!this.leftBounds);
    this.ffwBtn.setEnabled(!this.rightBounds);

    return trimmed;
  }
}