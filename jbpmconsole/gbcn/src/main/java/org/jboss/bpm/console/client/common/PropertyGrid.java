package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.layout.FillLayout;

public class PropertyGrid extends ScrollLayoutPanel
{
  private String[] fieldNames;
  private Grid grid;

  public PropertyGrid(String[] fieldDesc)
  {
    super(new FillLayout());
    this.grid = new Grid(fieldDesc.length, 2);
    this.grid.setStyleName("bpm-prop-grid");
    this.fieldNames = fieldDesc;

    add(this.grid);

    initReset();
  }

  private void initReset()
  {
    for (int i = 0; i < this.fieldNames.length; i++)
    {
      Label label = new Label(this.fieldNames[i]);
      label.setStyleName("bpm-prop-grid-label");
      this.grid.setWidget(i, 0, label);
      this.grid.setWidget(i, 1, new HTML(""));

      String style = i % 2 == 0 ? "bpm-prop-grid-even" : "bpm-prop-grid-odd";
      this.grid.getRowFormatter().setStyleName(i, style);
      this.grid.getColumnFormatter().setWidth(0, "20%");
      this.grid.getColumnFormatter().setWidth(1, "80%");
    }
  }

  public void clear()
  {
    initReset();
  }

  public void update(String[] fieldValues)
  {
    if (fieldValues.length != this.fieldNames.length) {
      throw new IllegalArgumentException("fieldValues.length doesn't match fieldName.length: " + this.fieldNames);
    }
    for (int i = 0; i < this.fieldNames.length; i++)
    {
      Label label = new Label(this.fieldNames[i]);
      label.setStyleName("bpm-prop-grid-label");
      this.grid.setWidget(i, 0, label);
      this.grid.setWidget(i, 1, new HTML(fieldValues[i]));
    }
  }
}