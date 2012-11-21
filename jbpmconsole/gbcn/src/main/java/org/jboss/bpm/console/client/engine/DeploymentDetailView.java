package org.jboss.bpm.console.client.engine;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.Caption;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.DeckLayoutPanel;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.common.PropertyGrid;
import org.jboss.bpm.console.client.model.DeploymentRef;

@SuppressWarnings("deprecation")
public class DeploymentDetailView extends CaptionLayoutPanel
  implements ViewInterface
{
  public static final String ID = DeploymentDetailView.class.getName();
  private Controller controller;
  private PropertyGrid grid;
  private DeploymentRef currentDeployment;
  private ResourcePanel resourcePanel;
  Button suspendBtn;
  Button resumeBtn;

  public DeploymentDetailView()
  {
    super("Deployment details");
    super.setStyleName("bpm-detail-panel");

    this.grid = new PropertyGrid(new String[] { "ID:", "Name:", "Processes:" });
    MosaicPanel propLayout = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    propLayout.add(this.grid, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    this.suspendBtn = new Button("Retire", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        DeploymentRef deploymentRef = DeploymentDetailView.this.getSelection();
        if (deploymentRef != null)
        {
          MessageBox.confirm("Retire deployment", "Do you want to retire this deployment? Any associated process will be suspended.", new MessageBox.ConfirmationCallback()
          {
            public void onResult(boolean doIt)
            {
              if (doIt)
              {
                DeploymentDetailView.this.controller.handleEvent(new Event(SuspendDeploymentAction.ID, DeploymentDetailView.this.getSelection().getId()));
              }

            }

          });
        }
        else
        {
          MessageBox.alert("Missing selection", "Please select a deployment");
        }
      }
    });
    this.resumeBtn = new Button("Activate", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent) {
        DeploymentRef deploymentRef = DeploymentDetailView.this.getSelection();
        if (deploymentRef != null)
        {
          MessageBox.confirm("Activate deployment", "Do you want to resume this deployment?", new MessageBox.ConfirmationCallback()
          {
            public void onResult(boolean doIt)
            {
              if (doIt)
              {
                DeploymentDetailView.this.controller.handleEvent(new Event(ResumeDeploymentAction.ID, DeploymentDetailView.this.getSelection().getId()));
              }

            }

          });
        }
        else
        {
          MessageBox.alert("Missing selection", "Please select a deployment");
        }
      }
    });
    MosaicPanel btnLayout = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    btnLayout.add(this.suspendBtn, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    btnLayout.add(this.resumeBtn, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    propLayout.add(btnLayout);

    final DeckLayoutPanel deck = new DeckLayoutPanel();
    deck.add(propLayout);

    ScrollLayoutPanel scrollPanel = new ScrollLayoutPanel();
    this.resourcePanel = new ResourcePanel();
    scrollPanel.add(this.resourcePanel);
    deck.add(scrollPanel);

    final ListBox dropBox = new ListBox(false);
    dropBox.setStyleName("bpm-operation-ui");
    dropBox.addItem("Properties");
    dropBox.addItem("Resources");
    dropBox.addChangeListener(new ChangeListener() {
      public void onChange(final Widget sender) {
        deck.showWidget(dropBox.getSelectedIndex());
        deck.layout();
      }
    });
    getHeader().add(dropBox, Caption.CaptionRegion.RIGHT);
    add(deck, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    deck.showWidget(dropBox.getSelectedIndex());

    add(deck, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
  }

  private DeploymentRef getSelection()
  {
    return this.currentDeployment;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
    this.resourcePanel.setController(controller);
  }

  public void update(DeploymentRef deployment)
  {
    this.currentDeployment = deployment;

    String[] values = { deployment.getId(), deployment.getName(), deployment.getDefinitions().toString() };

    this.resumeBtn.setEnabled(deployment.isSuspended());
    this.suspendBtn.setEnabled(!this.resumeBtn.isEnabled());

    this.grid.update(values);
    this.resourcePanel.update(deployment);
  }

  public void clearView()
  {
    this.currentDeployment = null;
    this.grid.clear();
    this.resourcePanel.clearView();

    this.suspendBtn.setEnabled(false);
    this.resumeBtn.setEnabled(false);
  }
}