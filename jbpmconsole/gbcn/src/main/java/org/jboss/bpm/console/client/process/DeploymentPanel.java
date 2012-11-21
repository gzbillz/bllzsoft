package org.jboss.bpm.console.client.process;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.common.PropertyGrid;
import org.jboss.bpm.console.client.engine.ViewDeploymentAction;
import org.jboss.bpm.console.client.util.ConsoleLog;

@SuppressWarnings("deprecation")
public class DeploymentPanel extends MosaicPanel
{
  private Controller controller;
  private PropertyGrid propGrid;
  String deploymentId = null;
  private boolean initialized;

  public DeploymentPanel()
  {
    super(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
  }

  private void initialize()
  {
    if (!this.initialized)
    {
      this.propGrid = new PropertyGrid(new String[] { "Deployment ID:" });

      add(this.propGrid, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
      Button button = new Button("View Deployment", new ClickListener()
      {
        public void onClick(final Widget widget)
        {
          DeploymentPanel.this.controller.handleEvent(new Event(ViewDeploymentAction.ID, DeploymentPanel.this.getSelection()));
        }
      });
      add(button);

      this.initialized = true;
    }
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void update(String id)
  {
    initialize();

    if (id != null)
    {
      this.deploymentId = id;
      this.propGrid.update(new String[] { id });
    }
    else
    {
      ConsoleLog.warn("deploymentId is null");
    }
  }

  public void clearView()
  {
    initialize();
    this.deploymentId = null;
    this.propGrid.clear();
  }

  private String getSelection()
  {
    return this.deploymentId;
  }
}