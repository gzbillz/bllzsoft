package org.jboss.bpm.console.client.engine;

import com.google.gwt.user.client.ui.HTML;
import com.mvc4g.client.Controller;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.common.PropertyGrid;
import org.jboss.bpm.console.client.model.DeploymentRef;

public class ResourcePanel extends MosaicPanel
{
  @SuppressWarnings("unused")
private Controller controller;
  private PropertyGrid propGrid;
  private DeploymentRef currentDeployment = null;
  private boolean initialized;
  private MosaicPanel resources = new MosaicPanel();

  public ResourcePanel()
  {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));
  }

  private void initialize()
  {
    if (!this.initialized)
    {
      this.propGrid = new PropertyGrid(new String[] { "Deployment ID:" });

      add(this.propGrid);
      add(this.resources, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      this.initialized = true;
    }
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void update(DeploymentRef deployment)
  {
    initialize();
    this.currentDeployment = deployment;

    StringBuffer sb = new StringBuffer();
    sb.append("<ul>");
    for (String res : deployment.getResourceNames())
    {
      if (!res.endsWith("/"))
        sb.append("<li>").append(res);
    }
    sb.append("</ul>");

    HTML html = new HTML(sb.toString());
    this.resources.clear();
    this.resources.add(html);

    this.propGrid.update(new String[] { deployment.getId() });
  }

  public void clearView()
  {
    initialize();
    this.currentDeployment = null;
    this.propGrid.clear();
    this.resources.clear();
  }

  @SuppressWarnings("unused")
private DeploymentRef getSelection()
  {
    return this.currentDeployment;
  }
}