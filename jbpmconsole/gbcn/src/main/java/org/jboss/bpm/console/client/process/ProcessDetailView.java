package org.jboss.bpm.console.client.process;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.Caption;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.DeckLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.jboss.bpm.console.client.ServerPlugins;
import org.jboss.bpm.console.client.common.PropertyGrid;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;

@SuppressWarnings("deprecation")
public class ProcessDetailView extends CaptionLayoutPanel
  implements ViewInterface
{
  public static final String ID = ProcessDetailView.class.getName();
  @SuppressWarnings("unused")
private Controller controller;
  private PropertyGrid grid;
  @SuppressWarnings("unused")
private ProcessDefinitionRef currentProcess;
  private DeploymentPanel deploymentPanel;

 public ProcessDetailView()
  {
    super("Process details");
    super.setStyleName("bpm-detail-panel");

    this.grid = new PropertyGrid(new String[] { "ID:", "Key:", "Name:", "Suspended:", "Package:", "Description:" });

    final DeckLayoutPanel deck = new DeckLayoutPanel();
    deck.add(this.grid);

    final ListBox dropBox = new ListBox(false);
    dropBox.setStyleName("bpm-operation-ui");
    dropBox.addItem("Properties");

    if (ServerPlugins.has("org.jboss.bpm.console.server.plugin.ProcessEnginePlugin"))
    {
      dropBox.addItem("Deployment");
      this.deploymentPanel = new DeploymentPanel();
      deck.add(this.deploymentPanel);
    }

    dropBox.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        deck.showWidget(dropBox.getSelectedIndex());
        deck.layout();
      }
    });
    getHeader().add(dropBox, Caption.CaptionRegion.RIGHT);
    add(deck, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    deck.showWidget(dropBox.getSelectedIndex());

    add(deck, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
    if (this.deploymentPanel != null)
      this.deploymentPanel.setController(controller);
  }

  public void update(ProcessDefinitionRef process)
  {
    this.currentProcess = process;

    String[] values = { process.getId(), process.getKey(), process.getName(), String.valueOf(process.isSuspended()), process.getPackageName(), process.getDescription() };

    this.grid.update(values);

    if (ServerPlugins.has("org.jboss.bpm.console.server.plugin.ProcessEnginePlugin"))
      this.deploymentPanel.update(process.getDeploymentId());
  }

  public void clearView()
  {
    this.grid.clear();
    if (ServerPlugins.has("org.jboss.bpm.console.server.plugin.ProcessEnginePlugin"))
      this.deploymentPanel.clearView();
    this.currentProcess = null;
  }
}