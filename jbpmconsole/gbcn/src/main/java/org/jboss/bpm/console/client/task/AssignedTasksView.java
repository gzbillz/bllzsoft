package org.jboss.bpm.console.client.task;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
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
import org.jboss.bpm.console.client.ServerPlugins;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.IFrameWindowCallback;
import org.jboss.bpm.console.client.common.IFrameWindowPanel;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.common.ModelParts;
import org.jboss.bpm.console.client.common.PagingCallback;
import org.jboss.bpm.console.client.common.PagingPanel;
import org.jboss.bpm.console.client.model.TaskRef;
import org.jboss.bpm.console.client.task.events.DetailViewEvent;
import org.jboss.bpm.console.client.task.events.TaskIdentityEvent;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

public class AssignedTasksView extends AbstractTaskList
  implements WidgetProvider, DataDriven
{
  public static final String ID = AssignedTasksView.class.getName();
  private final ApplicationContext appContext;
  private IFrameWindowPanel iframeWindow = null;
  private TaskDetailView detailsView;
  private SimpleDateFormat dateFormat = new SimpleDateFormat();
  private boolean hasDispatcherPlugin;
  private PagingPanel pagingPanel;
  private MosaicPanel panel;
  private Button skipBtn;

  public AssignedTasksView()
  {
    this.controller = ((Controller)Registry.get(Controller.class));
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
  }

  @SuppressWarnings("deprecation")
public void provideWidget(ProvisioningCallback callback)
  {
    this.panel = new MosaicPanel(new BorderLayout());

    initialize();

    this.panel.add(this.taskList, new BorderLayoutData(BorderLayout.Region.CENTER));
    this.panel.add(this.detailsView, new BorderLayoutData(BorderLayout.Region.SOUTH, 10, 200));

    this.controller.addView(ID, this);

    callback.onSuccess(this.panel);
  }

  public void initialize()
  {
    if (!this.isInitialized)
    {
      OpenTasksView.registerCommonActions(this.controller);

      this.taskList = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      this.taskList.setPadding(0);
      this.taskList.setWidgetSpacing(0);

      this.listBox = new ListBox<TaskRef>(new String[] { "Priority", "Process", "Task Name", "Due Date" });

      this.listBox.setCellRenderer(new ListBox.CellRenderer<TaskRef>()
      {
        public void renderCell(ListBox<TaskRef> listBox, int row, int column, TaskRef item) {
          switch (column) {
          case 0:
            listBox.setText(row, column, String.valueOf(item.getPriority()));
            break;
          case 1:
            listBox.setText(row, column, item.getProcessId());
            break;
          case 2:
            listBox.setText(row, column, item.getName());
            break;
          case 3:
            String d = item.getDueDate() != null ? AssignedTasksView.this.dateFormat.format(item.getDueDate()) : "";
            listBox.setText(row, column, d);
            break;
          default:
            throw new RuntimeException("Unexpected column size");
          }
        }
      });
      this.listBox.addRowSelectionHandler(new RowSelectionHandler()
      {
        public void onRowSelection(RowSelectionEvent rowSelectionEvent)
        {
          TaskRef task = AssignedTasksView.this.getSelection();
          if (task != null)
          {
            if (!task.isBlocking())
              AssignedTasksView.this.skipBtn.setEnabled(true);
            else {
              AssignedTasksView.this.skipBtn.setEnabled(false);
            }
            AssignedTasksView.this.controller.handleEvent(new Event(UpdateDetailsAction.ID, new DetailViewEvent("AssignedDetailView", task)));
          }
        }
      });
      MosaicPanel toolBox = new MosaicPanel();
      toolBox.setPadding(0);
      toolBox.setWidgetSpacing(5);

      ToolBar toolBar = new ToolBar();
      toolBar.add(new Button("Refresh", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          AssignedTasksView.this.reload();
        }
      }));
      Button viewBtn = new Button("View", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          TaskRef selection = AssignedTasksView.this.getSelection();

          if (selection != null)
          {
            if ((selection.getUrl() != null) && (!selection.getUrl().equals("")))
            {
              AssignedTasksView.this.iframeWindow = new IFrameWindowPanel(selection.getUrl(), "Task Form: " + selection.getName());

              AssignedTasksView.this.iframeWindow.setCallback(new IFrameWindowCallback()
              {
                public void onWindowClosed()
                {
                  AssignedTasksView.this.reload();
                }
              });
              AssignedTasksView.this.iframeWindow.show();
            }
            else
            {
              MessageBox.alert("Invalid operation", "The task doesn't provide a UI");
            }
          }
          else
          {
            MessageBox.alert("Missing selection", "Please select a task");
          }
        }
      });
      toolBar.add(viewBtn);

      toolBar.add(new Button("Release", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          TaskRef selection = AssignedTasksView.this.getSelection();

          if (selection != null)
          {
            TaskIdentityEvent payload = new TaskIdentityEvent(null, selection);

            AssignedTasksView.this.controller.handleEvent(new Event(ReleaseTaskAction.ID, payload));
          }
          else
          {
            MessageBox.alert("Missing selection", "Please select a task");
          }
        }
      }));
      this.skipBtn = new Button("Skip", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent) {
          TaskRef selection = AssignedTasksView.this.getSelection();

          if ((selection != null) && (!selection.isBlocking()))
          {
            AssignedTasksView.this.controller.handleEvent(new Event(SkipTaskAction.ID, new TaskIdentityEvent(AssignedTasksView.this.appContext.getAuthentication().getUsername(), selection)));
          }
          else
          {
            MessageBox.alert("Missing selection", "Please select a task");
          }
        }
      });
      this.skipBtn.setEnabled(false);
      toolBar.add(this.skipBtn);

      toolBox.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      this.taskList.add(toolBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
      this.taskList.add(this.listBox, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      this.pagingPanel = new PagingPanel(new PagingCallback()
      {
        public void rev()
        {
          AssignedTasksView.this.renderUpdate();
        }

        public void ffw()
        {
          AssignedTasksView.this.renderUpdate();
        }
      });
      this.taskList.add(this.pagingPanel, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      this.detailsView = new TaskDetailView(false);
      this.controller.addView("AssignedDetailView", this.detailsView);
      this.detailsView.initialize();

      this.hasDispatcherPlugin = ServerPlugins.has("org.jboss.bpm.console.server.plugin.FormDispatcherPlugin");

      viewBtn.setEnabled(this.hasDispatcherPlugin);

      ErraiBus.get().subscribe("appContext.model.listener", new MessageCallback()
      {
        public void callback(Message message)
        {
          switch (org.jboss.bpm.console.client.common.ModelCommands.valueOf(message.getCommandType()).ordinal())
          {
          case 1:
            if (((String)message.get(String.class, ModelParts.CLASS)).equals("processModel"))
              AssignedTasksView.this.reload();
            break;
          }
        }
      });
      Timer t = new Timer()
      {
        public void run()
        {
          AssignedTasksView.this.reload();
        }
      };
      t.schedule(500);

      this.isInitialized = true;
    }
  }

  private void reload()
  {
    this.controller.handleEvent(new Event(LoadTasksAction.ID, this.appContext.getAuthentication().getUsername()));
  }

  public void reset()
  {
    DefaultListModel<TaskRef> model = (DefaultListModel<TaskRef>)this.listBox.getModel();

    model.clear();

    this.controller.handleEvent(new Event(UpdateDetailsAction.ID, new DetailViewEvent("AssignedDetailView", null)));
  }

  @SuppressWarnings("unchecked")
public void update(Object[] data)
  {
    this.identity = ((String)data[0]);
    this.cachedTasks = ((List<TaskRef>)data[1]);
    this.pagingPanel.reset();
    renderUpdate();
  }

  public void setLoading(boolean isLoading)
  {
    if (this.panel.isVisible())
      LoadingOverlay.on(this.taskList, isLoading);
  }

  private void renderUpdate()
  {
    initialize();

    reset();

    DefaultListModel<TaskRef> model = (DefaultListModel<TaskRef>)this.listBox.getModel();

    @SuppressWarnings("unchecked")
	List<TaskRef> trimmed = this.pagingPanel.trim(this.cachedTasks);
    for (TaskRef task : trimmed)
    {
      if (TaskRef.STATE.ASSIGNED == task.getCurrentState())
        model.add(task);
    }
  }
}