package org.jboss.bpm.console.client.process;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.ArrayList;
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
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.common.PagingCallback;
import org.jboss.bpm.console.client.common.PagingPanel;
import org.jboss.bpm.console.client.common.WidgetWindowPanel;
import org.jboss.bpm.console.client.model.HistoryActivityInstanceRef;
import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.process.events.HistoryActivityDiagramEvent;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

public class HistoryInstanceListView
  implements WidgetProvider, ViewInterface, DataDriven
{
  public static final String ID = HistoryInstanceListView.class.getName();
  private Controller controller;
  private MosaicPanel instanceList = null;
  private ListBox<HistoryProcessInstanceRef> listBoxHistory;
  private ListBox<HistoryActivityInstanceRef> listBoxInstanceActivity;
  private ProcessDefinitionRef currentDefinition;
  private boolean isInitialized;
  private List<HistoryProcessInstanceRef> cachedInstances = null;

  private List<HistoryActivityInstanceRef> cachedInstancesActivity = null;

  private List<String> executedActivities = null;

  private SimpleDateFormat dateFormat = new SimpleDateFormat();
  @SuppressWarnings("unused")
private ApplicationContext appContext;
  private PagingPanel pagingPanel;
  MosaicPanel panel;
  private Button diagramBtn;
  @SuppressWarnings("unused")
private WidgetWindowPanel diagramWindowPanel;
  private ActivityDiagramView diagramView;

  public void provideWidget(ProvisioningCallback callback)
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));

    this.panel = new MosaicPanel();
    this.panel.setPadding(0);

    ((Controller)Registry.get(Controller.class)).addView(ID, this);
    initialize();

    callback.onSuccess(this.panel);
  }

  public boolean isInitialized()
  {
    return this.isInitialized;
  }

  public void initialize()
  {
    if (!this.isInitialized)
    {
      this.instanceList = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      this.instanceList.setPadding(0);
      this.instanceList.setWidgetSpacing(0);

      this.listBoxHistory = createHistoryListBox();

      this.listBoxInstanceActivity = createHistoryActivitiesListBox();

      MosaicPanel toolBox = new MosaicPanel();

      toolBox.setPadding(0);
      toolBox.setWidgetSpacing(5);
      toolBox.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));

      ToolBar toolBar = new ToolBar();
      toolBar.add(new Button("Refresh", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          HistoryInstanceListView.this.controller.handleEvent(new Event(UpdateHistoryDefinitionAction.ID, HistoryInstanceListView.this.getCurrentDefinition()));
        }
      }));
      this.diagramBtn = new Button("Diagram", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          String diagramUrl = HistoryInstanceListView.this.currentDefinition.getDiagramUrl();
          if ((HistoryInstanceListView.this.currentDefinition != null) && (HistoryInstanceListView.this.executedActivities != null)) {
            HistoryActivityDiagramEvent eventData = new HistoryActivityDiagramEvent(HistoryInstanceListView.this.currentDefinition, HistoryInstanceListView.this.executedActivities);
            if ((diagramUrl != null) && (!diagramUrl.equals("")))
            {
              HistoryInstanceListView.this.createDiagramWindow();
              HistoryInstanceListView.this.controller.handleEvent(new Event(LoadHistoryDiagramAction.ID, eventData));
            }
            else
            {
              MessageBox.alert("Incomplete deployment", "No diagram associated with process");
            }
          }
        }
      });
      toolBar.add(this.diagramBtn);
      this.diagramBtn.setEnabled(false);

      toolBox.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      this.instanceList.add(toolBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
      this.instanceList.add(this.listBoxHistory, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      this.pagingPanel = new PagingPanel(new PagingCallback()
      {
        public void rev()
        {
          HistoryInstanceListView.this.renderUpdate();
        }

        public void ffw()
        {
          HistoryInstanceListView.this.renderUpdate();
        }
      });
      this.instanceList.add(this.pagingPanel, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
      this.instanceList.add(this.listBoxInstanceActivity, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      if (this.cachedInstances != null) {
        bindData(this.cachedInstances);
      }

      MosaicPanel layout = new MosaicPanel(new BorderLayout());
      layout.setPadding(0);
      layout.add(this.instanceList, new BorderLayoutData(BorderLayout.Region.CENTER));

      this.panel.add(layout);

      this.isInitialized = true;

      this.executedActivities = new ArrayList<String>();
    }
  }

  public HistoryProcessInstanceRef getSelection()
  {
    HistoryProcessInstanceRef selection = null;
    if (this.listBoxHistory.getSelectedIndex() != -1)
      selection = (HistoryProcessInstanceRef)this.listBoxHistory.getItem(this.listBoxHistory.getSelectedIndex());
    return selection;
  }

  public ProcessDefinitionRef getCurrentDefinition()
  {
    return this.currentDefinition;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;

    this.diagramView = new ActivityDiagramView();

    controller.addView(ActivityDiagramView.ID, this.diagramView);
  }

  public void reset()
  {
    this.currentDefinition = null;
    this.cachedInstances = new ArrayList<HistoryProcessInstanceRef>();
    renderUpdate();

    this.diagramBtn.setEnabled(false);
  }

 
@SuppressWarnings("unchecked")
public void update(Object[] data)
  {
    if ((data[0] instanceof ProcessDefinitionRef))
    {
      this.currentDefinition = ((ProcessDefinitionRef)data[0]);
      this.cachedInstances = ((List<HistoryProcessInstanceRef>)data[1]);

      renderUpdate();

      DefaultListModel<HistoryActivityInstanceRef>  model = (DefaultListModel<HistoryActivityInstanceRef>)this.listBoxInstanceActivity.getModel();

      model.clear();
      this.diagramBtn.setEnabled(false);
    }
    else
    { 
      this.cachedInstancesActivity = ((List<HistoryActivityInstanceRef>)data[0]);
      renderHistoryActivityList();
    }
  }

  public void setLoading(boolean isLoading)
  {
    LoadingOverlay.on(this.instanceList, isLoading);
  }

  private void renderUpdate()
  {
    if (isInitialized())
    {
      bindData(this.cachedInstances);
    }
  }

  private void bindData(List<HistoryProcessInstanceRef> instances)
  {
    DefaultListModel<HistoryProcessInstanceRef> model = (DefaultListModel<HistoryProcessInstanceRef>)this.listBoxHistory.getModel();

    model.clear();

    @SuppressWarnings("unchecked")
	List<HistoryProcessInstanceRef> list = this.pagingPanel.trim(instances);
    for (HistoryProcessInstanceRef inst : list)
    {
      model.add(inst);
    }

    this.panel.invalidate();
  }

  private void renderHistoryActivityList()
  {
    if (this.cachedInstancesActivity != null)
    {
      DefaultListModel<HistoryActivityInstanceRef> model = (DefaultListModel<HistoryActivityInstanceRef>)this.listBoxInstanceActivity.getModel();

      model.clear();
      this.executedActivities.clear();

      for (HistoryActivityInstanceRef def : this.cachedInstancesActivity)
      {
        model.add(def);
        this.executedActivities.add(def.getActivityName());
      }

      if (this.listBoxInstanceActivity.getSelectedIndex() != -1)
        this.listBoxInstanceActivity.setItemSelected(this.listBoxInstanceActivity.getSelectedIndex(), false);
    }
  }

  protected ListBox<HistoryProcessInstanceRef> createHistoryListBox()
  {
    this.listBoxHistory = new ListBox<HistoryProcessInstanceRef>(new String[] { "<b>Instance</b>", "State", "Start Date", "End Date", "Duration" });

    this.listBoxHistory.setCellRenderer(new ListBox.CellRenderer<HistoryProcessInstanceRef>()
    {
      public void renderCell(ListBox<HistoryProcessInstanceRef> listBox, int row, int column, HistoryProcessInstanceRef item) {
        switch (column) {
        case 0:
          listBox.setText(row, column, item.getProcessInstanceId());
          break;
        case 1:
          listBox.setText(row, column, item.getState().toString());
          break;
        case 2:
          String d = item.getStartTime() != null ? HistoryInstanceListView.this.dateFormat.format(item.getStartTime()) : "";
          listBox.setText(row, column, d);
          break;
        case 3:
          String de = item.getEndTime() != null ? HistoryInstanceListView.this.dateFormat.format(item.getEndTime()) : "";
          listBox.setText(row, column, de);
          break;
        case 4:
          listBox.setText(row, column, String.valueOf(item.getDuration()));
          break;
        default:
          throw new RuntimeException("Unexpected column size");
        }
      }
    });
    this.listBoxHistory.addRowSelectionHandler(new RowSelectionHandler()
    {
      public void onRowSelection(RowSelectionEvent rowSelectionEvent) {
        int index = HistoryInstanceListView.this.listBoxHistory.getSelectedIndex();
        if (index != -1) {
          HistoryProcessInstanceRef item = (HistoryProcessInstanceRef)HistoryInstanceListView.this.listBoxHistory.getItem(index);

          HistoryInstanceListView.this.controller.handleEvent(new Event(UpdateHistoryInstanceAction.ID, item.getProcessInstanceId()));

          HistoryInstanceListView.this.diagramBtn.setEnabled(true);
        }
      }
    });
    return this.listBoxHistory;
  }

  private ListBox<HistoryActivityInstanceRef> createHistoryActivitiesListBox()
  {
    ListBox<HistoryActivityInstanceRef> listBox = new ListBox<HistoryActivityInstanceRef>(new String[] { "ActivityName", "StartTime", "EndTime", "Duration" });

    listBox.setCellRenderer(new ListBox.CellRenderer<HistoryActivityInstanceRef>()
    {
      public void renderCell(ListBox<HistoryActivityInstanceRef> listBox, int row, int column, HistoryActivityInstanceRef item)
      {
        String dateString;
        switch (column) {
        case 0:
          listBox.setText(row, column, item.getActivityName());
          break;
        case 1:
          dateString = item.getStartTime() != null ? HistoryInstanceListView.this.dateFormat.format(item.getStartTime()) : "";
          listBox.setText(row, column, dateString);
          break;
        case 2:
          dateString = item.getEndTime() != null ? HistoryInstanceListView.this.dateFormat.format(item.getEndTime()) : "";
          listBox.setText(row, column, dateString);
          break;
        case 3:
          listBox.setText(row, column, String.valueOf(item.getDuration()));
          break;
        default:
          throw new RuntimeException("Unexpected column size");
        }
      }
    });
    return listBox;
  }

  private void createDiagramWindow()
  {
    MosaicPanel layout = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    layout.setStyleName("bpm-window-layout");
    layout.setPadding(5);

    Label header = new Label("Instance: ");
    header.setStyleName("bpm-label-header");
    layout.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    layout.add(this.diagramView, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    this.diagramWindowPanel = new WidgetWindowPanel("Process Instance Activity", layout, true);
  }
}