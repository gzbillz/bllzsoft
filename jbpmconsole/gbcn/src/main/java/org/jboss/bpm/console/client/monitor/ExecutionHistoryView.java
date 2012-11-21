package org.jboss.bpm.console.client.monitor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwt.mosaic.ui.client.LayoutPopupPanel;
import org.gwt.mosaic.ui.client.PopupMenu;
import org.gwt.mosaic.ui.client.ToolBar;
import org.gwt.mosaic.ui.client.ToolButton;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.gwt.mosaic.ui.client.layout.BorderLayoutData;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.ColumnLayout;
import org.gwt.mosaic.ui.client.layout.ColumnLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.layout.RowLayout;
import org.gwt.mosaic.ui.client.layout.RowLayoutData;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.gwt.mosaic.ui.client.util.ResizableWidget;
import org.gwt.mosaic.ui.client.util.ResizableWidgetCollection;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.StringRef;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.bpm.monitor.gui.client.ChronoscopeFactory;
import org.jboss.bpm.monitor.gui.client.JSOModel;
import org.jboss.bpm.monitor.gui.client.TimespanValues;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;
import org.jboss.errai.workspaces.client.framework.Registry;
import org.timepedia.chronoscope.client.Dataset;
import org.timepedia.chronoscope.client.Datasets;
import org.timepedia.chronoscope.client.Overlay;
import org.timepedia.chronoscope.client.XYPlot;
import org.timepedia.chronoscope.client.browser.ChartPanel;
import org.timepedia.chronoscope.client.browser.Chronoscope;
import org.timepedia.chronoscope.client.browser.json.GwtJsonDataset;
import org.timepedia.chronoscope.client.canvas.View;
import org.timepedia.chronoscope.client.canvas.ViewReadyCallback;
import org.timepedia.chronoscope.client.data.tuple.Tuple2D;
import org.timepedia.chronoscope.client.event.PlotFocusEvent;
import org.timepedia.chronoscope.client.event.PlotFocusHandler;
import org.timepedia.chronoscope.client.io.DatasetReader;
import org.timepedia.chronoscope.client.util.date.ChronoDate;

@LoadTool(name="Execution History", group="Processes")
public class ExecutionHistoryView
  implements WidgetProvider, ViewInterface
{
  public static final String ID = ExecutionHistoryView.class.getName();
  @SuppressWarnings("unused")
private static final String TIMEPEDIA_FONTBOOK_SERVICE = "http://api.timepedia.org/fr";
  @SuppressWarnings("unused")
private static volatile double GOLDEN__RATIO = 1.618D;
  private ChartPanel chartPanel;
  private ToolButton menuButton;
  private ToolButton timespanButton;
  private HTML title;
  private HTML timespan;
  private LayoutPanel chartArea;
  private LayoutPanel timespanPanel;
  private Map<Long, Overlay> overlayMapping = new HashMap<Long, Overlay>();

  @SuppressWarnings("unused")
private SimpleDateFormat dateFormat = new SimpleDateFormat();
  private String currentProcDef;
  private TimespanValues currentTimespan;
  private LayoutPanel instancePanel;
  private org.gwt.mosaic.ui.client.ListBox<String> listBox;
  private LayoutPanel buttonPanel;
  private CheckBox includeFailed;
  private Controller controller;
  @SuppressWarnings("unused")
private static final int DATASET_COMPLETED = 0;
  @SuppressWarnings("unused")
private static final int DATASET_FAILED = 1;
  @SuppressWarnings("unused")
private static final int DATASET_TERMINATED = 2;
  private List<ProcessDefinitionRef> processDefinitions;

  public void provideWidget(ProvisioningCallback callback)
  {
    this.controller = ((Controller)Registry.get(Controller.class));

    this.controller.addView(ID, this);
    this.controller.addAction(GetProcessDefinitionsAction.ID, new GetProcessDefinitionsAction());
    this.controller.addAction(LoadDatasetsAction.ID, new LoadDatasetsAction());
    this.controller.addAction(LoadChartProcessInstancesAction.ID, new LoadChartProcessInstancesAction());

    LayoutPanel panel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    ToolBar toolBar = new ToolBar();
    panel.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    this.menuButton = new ToolButton("Open", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent) {
        ExecutionHistoryView.this.controller.handleEvent(new Event(GetProcessDefinitionsAction.ID, null));
      }
    });
    toolBar.add(this.menuButton);

    this.title = new HTML();
    this.title.getElement().setAttribute("style", "font-size:24px; font-weight:BOLD");

    BoxLayout boxLayout = new BoxLayout(BoxLayout.Orientation.HORIZONTAL);
    this.timespanPanel = new LayoutPanel(boxLayout);
    this.timespanPanel.setPadding(0);

    this.timespan = new HTML();
    this.timespan.getElement().setAttribute("style", "padding-left:10px;padding-top:2px; color:#C8C8C8;font-size:16px;text-align:left;");
    this.timespanButton = new ToolButton();

    this.timespanButton.setStyle(ToolButton.ToolButtonStyle.MENU);
    this.timespanButton.getElement().setAttribute("style", "padding-right:0px;background-image:none;");
    this.timespanButton.setVisible(false);

    PopupMenu timeBtnMenu = new PopupMenu();

    for (final TimespanValues ts : TimespanValues.values())
    {
      timeBtnMenu.addItem(ts.getCanonicalName(), new Command()
      {
        public void execute()
        {
          LoadDatasetEvent theEvent = new LoadDatasetEvent();
          String theDefinitionId = ExecutionHistoryView.this.getDefinitionId(ExecutionHistoryView.this.currentProcDef);
          if (theDefinitionId == null) {
            return;
          }
          theEvent.setDefinitionId(theDefinitionId);
          theEvent.setTimespan(ts);
          ExecutionHistoryView.this.currentTimespan = ts;
          if (ExecutionHistoryView.this.includeFailed.getValue().booleanValue())
            theEvent.setIncludedFailed(true);
          else {
            theEvent.setIncludedFailed(false);
          }

          LoadingOverlay.on(ExecutionHistoryView.this.chartArea, true);
          ExecutionHistoryView.this.controller.handleEvent(new Event(LoadDatasetsAction.ID, theEvent));
        }
      });
    }

    this.timespanButton.setMenu(timeBtnMenu);

    this.timespanPanel.add(this.timespanButton, new BoxLayoutData("20px", "20px"));
    this.timespanPanel.add(this.timespan, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    LayoutPanel contents = new LayoutPanel(new RowLayout());

    LayoutPanel headerPanel = new LayoutPanel(new ColumnLayout());
    headerPanel.setPadding(0);
    headerPanel.add(this.title, new ColumnLayoutData("55%"));
    headerPanel.add(this.timespanPanel, new ColumnLayoutData("45%"));

    this.chartArea = new LayoutPanel();
    this.chartArea.setPadding(15);
    this.chartArea.setLayout(new BorderLayout());

    this.instancePanel = new LayoutPanel();
    this.listBox = new org.gwt.mosaic.ui.client.ListBox<String>(new String[] { "Process Instance" });
    this.instancePanel.add(this.listBox);
    contents.add(headerPanel, new RowLayoutData("100"));
    contents.add(this.chartArea, new RowLayoutData(true));

    this.includeFailed = new CheckBox("Incl. failed / terminated?");
    this.includeFailed.setValue(Boolean.valueOf(false));
    this.includeFailed.addValueChangeHandler(new ValueChangeHandler<Boolean>()
    {
      public void onValueChange(ValueChangeEvent<Boolean> isEnabled)
      {
        LoadDatasetEvent theEvent = new LoadDatasetEvent();
        String theDefinitionId = ExecutionHistoryView.this.getDefinitionId(ExecutionHistoryView.this.currentProcDef);
        if (theDefinitionId == null) {
          return;
        }
        theEvent.setDefinitionId(theDefinitionId);
        theEvent.setTimespan(ExecutionHistoryView.this.currentTimespan);
        if (ExecutionHistoryView.this.includeFailed.getValue().booleanValue())
          theEvent.setIncludedFailed(true);
        else {
          theEvent.setIncludedFailed(false);
        }
        LoadingOverlay.on(ExecutionHistoryView.this.chartArea, true);
        ExecutionHistoryView.this.controller.handleEvent(new Event(LoadDatasetsAction.ID, theEvent));
      }
    });
    this.buttonPanel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    this.buttonPanel.add(this.includeFailed);

    panel.add(contents, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    ErraiBus.get().subscribe("process.execution.history", new MessageCallback()
    {
      public void callback(Message message)
      {
        String processName = (String)message.get(String.class, "processName");
        String processDefinitionId = (String)message.get(String.class, "processDefinitionId");
        ExecutionHistoryView.this.update(processName, processDefinitionId);
      }
    });
    callback.onSuccess(panel);
  }

  private String getDefinitionId(String currentProcessDefinition)
  {
    String definitionId = null;

    if ((this.processDefinitions == null) || (this.processDefinitions.size() < 1)) {
      return null;
    }

    for (ProcessDefinitionRef ref : this.processDefinitions) {
      if (currentProcessDefinition.equals(ref.getName())) {
        definitionId = ref.getId();
      }
    }

    return definitionId;
  }

  public void selectDefinition(List<ProcessDefinitionRef> processDefinitions)
  {
    this.processDefinitions = processDefinitions;

    final LayoutPopupPanel popup = new LayoutPopupPanel(true);
    popup.addStyleName("soa-PopupPanel");

    final com.google.gwt.user.client.ui.ListBox listBox = new com.google.gwt.user.client.ui.ListBox();

    listBox.addItem("");

    for (ProcessDefinitionRef ref : processDefinitions)
    {
      listBox.addItem(ref.getName());
    }

    LayoutPanel p = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    p.add(new HTML("Please select a process:"));
    p.add(listBox);

    LayoutPanel p2 = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    p2.add(new Button("Done", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent) {
        if (listBox.getSelectedIndex() > 0)
        {
          popup.hide();
          String procDef = listBox.getItemText(listBox.getSelectedIndex()); 
          // 原始
//          ExecutionHistoryView.this.update(procDef, ExecutionHistoryView.access$200(ExecutionHistoryView.this, procDef));
          ExecutionHistoryView.this.update(procDef, ExecutionHistoryView.this.getDefinitionId(procDef));
        }
      }
    }));
    HTML html = new HTML("Cancel");
    html.addClickHandler(new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent) {
        popup.hide();
      }
    });
    p2.add(html, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    p.add(p2);

    popup.setPopupPosition(this.menuButton.getAbsoluteLeft() - 5, this.menuButton.getAbsoluteTop() + 30);
    popup.setWidget(p);
    popup.pack();
    popup.show();
  }

  private void update(String procDef, String processDefinitionId)
  {
    this.currentProcDef = procDef;

    String name = this.currentProcDef;
    String subtitle = "";
    if (this.currentProcDef.indexOf("}") != -1)
    {
      String[] qname = this.currentProcDef.split("}");
      name = qname[1];
      subtitle = qname[0].substring(1, qname[0].length());
    }

    this.title.setHTML(name + "<br/><div style='color:#C8C8C8;font-size:12px;text-align:left;'>" + subtitle + "</div>");
    TimespanValues ts = this.currentTimespan == null ? TimespanValues.LAST_7_DAYS : this.currentTimespan;

    LoadingOverlay.on(this.chartArea, true);

    LoadDatasetEvent theEvent = new LoadDatasetEvent();
    theEvent.setDefinitionId(processDefinitionId);
    theEvent.setTimespan(ts);
    if (this.includeFailed.getValue().booleanValue())
      theEvent.setIncludedFailed(true);
    else {
      theEvent.setIncludedFailed(false);
    }

    this.currentTimespan = ts;

    this.controller.handleEvent(new Event(LoadDatasetsAction.ID, theEvent));
  }

  @SuppressWarnings("unchecked")
public void updateChart(String chartData)
  {
    ((DefaultListModel<String>)this.listBox.getModel()).clear();

    LoadingOverlay.on(this.chartArea, false);

    this.timespanButton.setVisible(true);

    Datasets<Tuple2D> datasets = new Datasets<Tuple2D>();
    DatasetReader datasetReader = ChronoscopeFactory.getInstance().getDatasetReader();
    JSOModel jsoModel = JSOModel.fromJson(chartData);

    if (chartData.startsWith("["))
    {
      for (int i = 0; i < jsoModel.length(); i++)
      {
        datasets.add(datasetReader.createDatasetFromJson(new GwtJsonDataset(jsoModel.get(i))));
      }

    }
    else
    {
      datasets.add(datasetReader.createDatasetFromJson(new GwtJsonDataset(jsoModel)));
    }

    renderChart(datasets);
    this.timespanPanel.layout();
  }

  private void renderChart(Datasets<Tuple2D> datasets)
  {
    try
    {
      @SuppressWarnings("rawtypes")
	Dataset[] dsArray = datasets.toArray();

      if (this.chartPanel != null)
      {
        this.chartPanel.replaceDatasets(dsArray);
        this.overlayMapping.clear();
      }
      else
      {
        initChartPanel(dsArray);
      }

      this.timespan.setText("Executions " + this.currentTimespan.getCanonicalName());
      this.chartArea.layout();
    }
    catch (Exception e)
    {
      ConsoleLog.error("Failed to create chart", e);
    }
  }

  @SuppressWarnings("rawtypes")
private void initChartPanel(Dataset[] datasets)
  {
    int[] dim = calcChartDimension();

    this.chartPanel = Chronoscope.createTimeseriesChart(datasets, dim[0], dim[1]);

    XYPlot plot = this.chartPanel.getChart().getPlot();

    plot.addPlotFocusHandler(new PlotFocusHandler()
    {
      public void onFocus(PlotFocusEvent event) {
        if (event.getFocusDataset() >= 0)
        {
          ChronoDate chronoDate = ChronoDate.get(event.getDomain());
          Date date = new Date();
          date.setTime((long) chronoDate.getTime());
          

          LoadChartProcessInstanceEvent theEvent = new LoadChartProcessInstanceEvent();
          String theDefinitionId = ExecutionHistoryView.this.getDefinitionId(ExecutionHistoryView.this.currentProcDef);
          if (theDefinitionId == null) {
            return;
          }
          theEvent.setDefinitionId(theDefinitionId);
          theEvent.setDate(date);
          theEvent.setDatasetType(event.getFocusDataset());
          theEvent.setTimespan(ExecutionHistoryView.this.currentTimespan);
          ExecutionHistoryView.this.controller.handleEvent(new Event(LoadChartProcessInstancesAction.ID, theEvent));
        }
      }
    });
    ViewReadyCallback callback = new ViewReadyCallback() {
      public void onViewReady(View view) {
        ExecutionHistoryView.this.resizeChartArea(view);
      }
    };
    this.chartPanel.setViewReadyCallback(callback);

    LayoutPanel wrapper = new LayoutPanel();
    wrapper.setLayout(new ColumnLayout());
    wrapper.add(this.chartPanel, new ColumnLayoutData("70%"));
    wrapper.add(new Label("More..."), new ColumnLayoutData("30%"));

    this.chartArea.add(this.chartPanel);
    this.chartArea.add(this.buttonPanel, new BorderLayoutData(BorderLayout.Region.EAST, "150px"));

    this.instancePanel.getElement().setAttribute("style", "margin-top:15px");
    this.chartArea.add(this.instancePanel, new BorderLayoutData(BorderLayout.Region.SOUTH, "150px"));

    ResizableWidgetCollection.get().add(new ResizableWidget() {
      public Element getElement() {
        return ExecutionHistoryView.this.chartPanel.getElement();
      }

      public boolean isAttached() {
        return ExecutionHistoryView.this.chartPanel.isAttached();
      }

      public void onResize(int width, int height)
      {
        @SuppressWarnings("unused")
		View view = ExecutionHistoryView.this.resizeChartView();
      }
    });
  }

  public void updateProcessInstances(List<StringRef> instances)
  {
    DefaultListModel<String> listModel = (DefaultListModel<String>)this.listBox.getModel();
    listModel.clear();
    for (StringRef instance : instances)
      listModel.add(instance.getValue());
  }

  private int[] calcChartDimension()
  {
    return new int[] { 460, 200 };
  }

  private View resizeChartView()
  {
    int[] dim = calcChartDimension();

    View view = this.chartPanel.getChart().getView();
    if (view != null) {
      view.resize(dim[0], dim[1]);
    }
    resizeChartArea(view);

    return view;
  }

  private void resizeChartArea(View view)
  {
    this.chartArea.layout();
  }

  public void setController(Controller controller) {
    this.controller = controller;
  }
}