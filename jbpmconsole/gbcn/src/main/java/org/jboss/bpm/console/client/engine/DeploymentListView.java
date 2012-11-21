package org.jboss.bpm.console.client.engine;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.ToolBar;
import org.gwt.mosaic.ui.client.event.RowSelectionEvent;
import org.gwt.mosaic.ui.client.event.RowSelectionHandler;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.gwt.mosaic.ui.client.layout.BorderLayoutData;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.ConsoleConfig;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.model.DeploymentRef;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

@SuppressWarnings("deprecation")
public class DeploymentListView
  implements ViewInterface, WidgetProvider, DataDriven
{
  public static final String ID = DeploymentListView.class.getName();
  private Controller controller;
  private boolean initialized;
  private MosaicPanel deploymentList = null;
  private org.gwt.mosaic.ui.client.ListBox<DeploymentRef> listBox;
  @SuppressWarnings("unused")
private DeploymentRef selection = null;

  @SuppressWarnings("unused")
private SimpleDateFormat dateFormat = new SimpleDateFormat();

  private int FILTER_NONE = 10;
  private int FILTER_ACTIVE = 20;
  private int FILTER_SUSPENDED = 30;
  private int currentFilter = this.FILTER_NONE;

  private List<DeploymentRef> deployments = null;
  private DeploymentDetailView detailView;
  MosaicPanel panel;
  private boolean isRiftsawInstance = false;

  public DeploymentListView()
  {
    this.controller = ((Controller)Registry.get(Controller.class));

    ConsoleConfig config = ((ApplicationContext)Registry.get(ApplicationContext.class)).getConfig();
    this.isRiftsawInstance = config.getProfileName().equals("BPEL Console");
  }

  public void provideWidget(ProvisioningCallback callback)
  {
    this.panel = new MosaicPanel(new BorderLayout());
    this.listBox = createListBox();

    initialize();

    this.panel.add(this.deploymentList, new BorderLayoutData(BorderLayout.Region.CENTER));
    this.panel.add(this.detailView, new BorderLayoutData(BorderLayout.Region.SOUTH, 200.0D));

    this.controller.addAction(UpdateDeploymentsAction.ID, new UpdateDeploymentsAction());
    this.controller.addAction(UpdateDeploymentDetailAction.ID, new UpdateDeploymentDetailAction());
    this.controller.addAction(DeleteDeploymentAction.ID, new DeleteDeploymentAction());
    this.controller.addAction(SuspendDeploymentAction.ID, new SuspendDeploymentAction());
    this.controller.addAction(ResumeDeploymentAction.ID, new ResumeDeploymentAction());

    this.controller.addView(ID, this);

    callback.onSuccess(this.panel);
  }

  private ListBox<DeploymentRef> createListBox()
  {
    final ListBox<DeploymentRef> listBox = new ListBox<DeploymentRef>(new String[] { "Deployment", "Status" });

    listBox.setCellRenderer(new ListBox.CellRenderer<DeploymentRef>()
    {
      public void renderCell(ListBox<DeploymentRef> listBox, int row, int column, DeploymentRef item)
      {
        String color = item.isSuspended() ? "#CCCCCC" : "#000000";

        switch (column) {
        case 0:
          String text = "<div style=\"color:" + color + "\">" + item.getName() + "</div>";
          listBox.setWidget(row, column, new HTML(text));
          break;
        case 1:
          String status = item.isSuspended() ? "retired" : "active";
          @SuppressWarnings("unused")
		String s = "<div style=\"color:" + color + "\">" + status + "</div>";
          listBox.setWidget(row, column, new HTML(status));
          break;
        default:
          throw new RuntimeException("Unexpected column size");
        }
      }
 
    });
    listBox.addRowSelectionHandler(new RowSelectionHandler()
    {
      public void onRowSelection(RowSelectionEvent rowSelectionEvent)
      {
        int index = listBox.getSelectedIndex();
        if (index != -1)
        {
          DeploymentRef item = (DeploymentRef)listBox.getItem(index);

          DeploymentListView.this.controller.handleEvent(new Event(UpdateDeploymentDetailAction.ID, item));
        }
      }
    });
    return listBox;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public boolean isInitialized()
  {
    return this.initialized;
  }

  public void initialize()
  {
    if (!this.initialized)
    {
      this.deploymentList = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      this.deploymentList.setPadding(0);
      this.deploymentList.setWidgetSpacing(0);

      MosaicPanel toolBox = new MosaicPanel();
      toolBox.setPadding(0);
      toolBox.setWidgetSpacing(0);
      toolBox.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));

      ToolBar toolBar = new ToolBar();
      toolBar.add(new Button("Refresh", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          DeploymentListView.this.reset();

          DeploymentListView.this.controller.handleEvent(new Event(UpdateDeploymentsAction.ID, null));
        }
      }));
      Button deleteBtn = new Button("Delete", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          DeploymentRef deploymentRef = DeploymentListView.this.getSelection();
          if (deploymentRef != null)
          {
            MessageBox.confirm("Delete deployment", "Do you want to delete this deployment? Any related data will be removed.", new MessageBox.ConfirmationCallback()
            {
              public void onResult(boolean doIt)
              {
                if (doIt)
                {
                  DeploymentListView.this.controller.handleEvent(new Event(DeleteDeploymentAction.ID, DeploymentListView.this.getSelection().getId()));
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
      if (!this.isRiftsawInstance) {
        toolBar.add(deleteBtn);
      }
      toolBox.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      MosaicPanel filterPanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      filterPanel.setStyleName("mosaic-ToolBar");
      final com.google.gwt.user.client.ui.ListBox dropBox = new com.google.gwt.user.client.ui.ListBox(false);
      dropBox.setStyleName("bpm-operation-ui");
      dropBox.addItem("All");
      dropBox.addItem("Active");
      dropBox.addItem("Retired");

      dropBox.addChangeListener(new ChangeListener() {
        public void onChange(Widget sender) {
          switch (dropBox.getSelectedIndex())
          {
          case 0:
            DeploymentListView.this.currentFilter = DeploymentListView.this.FILTER_NONE;
            break;
          case 1:
            DeploymentListView.this.currentFilter = DeploymentListView.this.FILTER_ACTIVE;
            break;
          case 2:
            DeploymentListView.this.currentFilter = DeploymentListView.this.FILTER_SUSPENDED;
            break;
          default:
            throw new IllegalArgumentException("No such index");
          }

          DeploymentListView.this.renderFiltered();
        }
      });
      filterPanel.add(dropBox);

      toolBox.add(filterPanel, new BoxLayoutData(BoxLayoutData.FillStyle.VERTICAL));

      this.deploymentList.add(toolBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
      this.deploymentList.add(this.listBox, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      this.detailView = new DeploymentDetailView();
      this.controller.addView(DeploymentDetailView.ID, this.detailView);

      Timer t = new Timer()
      {
        public void run()
        {
          DeploymentListView.this.controller.handleEvent(new Event(UpdateDeploymentsAction.ID, null));
        }
      };
      t.schedule(500);

      this.initialized = true;
    }
  }

  public DeploymentRef getSelection()
  {
    DeploymentRef selection = null;
    if ((isInitialized()) && (this.listBox.getSelectedIndex() != -1))
      selection = (DeploymentRef)this.listBox.getItem(this.listBox.getSelectedIndex());
    return selection;
  }

  public void reset()
  {
    DefaultListModel<DeploymentRef> model = (DefaultListModel<DeploymentRef>)this.listBox.getModel();

    model.clear();

    this.controller.handleEvent(new Event(UpdateDeploymentDetailAction.ID, null));
  }

  @SuppressWarnings("unchecked")
public void update(Object[] data)
  {
    this.deployments = ((List<DeploymentRef>)data[0]);

    renderFiltered();
  }

  public void setLoading(boolean isLoading)
  {
    LoadingOverlay.on(this.deploymentList, isLoading);
  }

  private void renderFiltered()
  {
    if (this.deployments != null)
    {
      reset();

      DefaultListModel<DeploymentRef> model = (DefaultListModel<DeploymentRef>)this.listBox.getModel();

      for (DeploymentRef dpl : this.deployments)
      {
        if (this.FILTER_NONE == this.currentFilter)
        {
          model.add(dpl);
        }
        else
        {
          boolean showSuspended = this.FILTER_SUSPENDED == this.currentFilter;
          if (dpl.isSuspended() == showSuspended) {
            model.add(dpl);
          }
        }
      }
      if (this.listBox.getSelectedIndex() != -1)
        this.listBox.setItemSelected(this.listBox.getSelectedIndex(), false);
    }
  }

  public void select(String deploymentId)
  {
    DefaultListModel<DeploymentRef> model = (DefaultListModel<DeploymentRef>)this.listBox.getModel();

    for (int i = 0; i < model.getSize(); i++)
    {
      DeploymentRef ref = (DeploymentRef)model.getElementAt(i);
      if (ref.getId().equals(deploymentId))
      {
        this.listBox.setSelectedIndex(i);
        break;
      }
    }
  }
}