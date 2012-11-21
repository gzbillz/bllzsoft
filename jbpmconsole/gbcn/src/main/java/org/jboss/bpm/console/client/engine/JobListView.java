package org.jboss.bpm.console.client.engine;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.Date;
import java.util.List;
import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.ToolBar;
import org.gwt.mosaic.ui.client.event.RowSelectionEvent;
import org.gwt.mosaic.ui.client.event.RowSelectionHandler;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.model.JobRef;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

@SuppressWarnings("deprecation")
public class JobListView
  implements ViewInterface, WidgetProvider, DataDriven
{
  public static final String ID = JobListView.class.getName();
  private Controller controller;
  private MosaicPanel jobList = null;
  private org.gwt.mosaic.ui.client.ListBox<JobRef> listBox;
  @SuppressWarnings("unused")
private JobRef selection = null;

  private SimpleDateFormat dateFormat = new SimpleDateFormat();

  private int FILTER_NONE = 10;
  private int FILTER_TIMER = 20;
  private int FILTER_MESSAGE = 30;
  private int currentFilter = this.FILTER_NONE;

  private List<JobRef> jobs = null;
  MosaicPanel panel;
  private boolean initialized;

  public JobListView()
  {
    this.controller = ((Controller)Registry.get(Controller.class));
  }

  public void provideWidget(ProvisioningCallback callback)
  {
    this.panel = new MosaicPanel();

    this.listBox = createListBox();

    initialize();

    this.panel.add(this.jobList);

    this.controller.addView(ID, this);
    this.controller.addAction(ExecuteJobAction.ID, new ExecuteJobAction());
    callback.onSuccess(this.panel);
  }

  private ListBox<JobRef> createListBox()
  {
    final ListBox<JobRef> listBox = new ListBox<JobRef>(new String[] { "ID", "Due Date", "Type" });

    listBox.setCellRenderer(new ListBox.CellRenderer<JobRef>()
    {
      public void renderCell(ListBox<JobRef> listBox, int row, int column, JobRef item) {
        switch (column) {
        case 0:
          listBox.setText(row, column, item.getId());
          break;
        case 1:
          long ts = item.getTimestamp();
          String ds = ts > 0L ? JobListView.this.dateFormat.format(new Date(ts)) : "";
          listBox.setText(row, column, ds);
          break;
        case 2:
          listBox.setText(row, column, item.getType());
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
        @SuppressWarnings("unused")
		JobRef item;
        if (index != -1)
        {
          item = (JobRef)listBox.getItem(index);
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
      this.jobList = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      this.jobList.setPadding(0);
      this.jobList.setWidgetSpacing(0);

      MosaicPanel toolBox = new MosaicPanel();
      toolBox.setPadding(0);
      toolBox.setWidgetSpacing(0);
      toolBox.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));

      ToolBar toolBar = new ToolBar();
      toolBar.add(new Button("Refresh", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          JobListView.this.controller.handleEvent(new Event(UpdateJobsAction.ID, null));
        }
      }));
      toolBar.add(new Button("Execute", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          JobRef selection = JobListView.this.getSelection();
          if (null == selection)
          {
            MessageBox.alert("Missing selection", "Please select a job!");
          }
          else
          {
            JobListView.this.controller.handleEvent(new Event(ExecuteJobAction.ID, selection.getId()));
          }
        }
      }));
      toolBox.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      MosaicPanel filterPanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      filterPanel.setStyleName("mosaic-ToolBar");
      final com.google.gwt.user.client.ui.ListBox dropBox = new com.google.gwt.user.client.ui.ListBox(false);
      dropBox.setStyleName("bpm-operation-ui");
      dropBox.addItem("All");
      dropBox.addItem("Timers");
      dropBox.addItem("Messages");

      dropBox.addChangeListener(new ChangeListener() {
        public void onChange(Widget sender) {
          switch (dropBox.getSelectedIndex())
          {
          case 0:
            JobListView.this.currentFilter = JobListView.this.FILTER_NONE;
            break;
          case 1:
            JobListView.this.currentFilter = JobListView.this.FILTER_TIMER;
            break;
          case 2:
            JobListView.this.currentFilter = JobListView.this.FILTER_MESSAGE;
            break;
          default:
            throw new IllegalArgumentException("No such index");
          }

          JobListView.this.renderFiltered();
        }
      });
      filterPanel.add(dropBox);

      toolBox.add(filterPanel, new BoxLayoutData(BoxLayoutData.FillStyle.VERTICAL));

      this.jobList.add(toolBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
      this.jobList.add(this.listBox, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      Timer t = new Timer()
      {
        public void run()
        {
          JobListView.this.controller.handleEvent(new Event(UpdateJobsAction.ID, null));
        }
      };
      t.schedule(500);

      this.controller.addAction(UpdateJobsAction.ID, new UpdateJobsAction());

      this.initialized = true;
    }
  }

  public void reset()
  {
    DefaultListModel<JobRef> model = (DefaultListModel<JobRef>)this.listBox.getModel();

    model.clear();
  }

  @SuppressWarnings("unchecked")
public void update(Object[] data)
  {
    this.jobs = (List<JobRef>)data[0];
    renderFiltered();
  }

  public void setLoading(boolean isLoading)
  {
    LoadingOverlay.on(this.jobList, isLoading);
  }

  private void renderFiltered()
  {
    if (this.jobs != null)
    {
      reset();

      DefaultListModel<JobRef> model = (DefaultListModel<JobRef>)this.listBox.getModel();

      for (JobRef def : this.jobs)
      {
        if (this.FILTER_NONE == this.currentFilter)
        {
          model.add(def);
        }
        else if ((this.FILTER_TIMER == this.currentFilter) && (def.getType().equals("timer")))
        {
          model.add(def);
        }
        else if ((this.FILTER_MESSAGE == this.currentFilter) && (def.getType().equals("message")))
        {
          model.add(def);
        }

      }

      if (this.listBox.getSelectedIndex() != -1)
        this.listBox.setItemSelected(this.listBox.getSelectedIndex(), false);
    }
  }

  public JobRef getSelection()
  {
    JobRef selection = null;
    if ((isInitialized()) && (this.listBox.getSelectedIndex() != -1))
      selection = (JobRef)this.listBox.getItem(this.listBox.getSelectedIndex());
    return selection;
  }
 
}