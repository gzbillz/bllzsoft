package org.jboss.bpm.console.client.process;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import java.util.StringTokenizer;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.DecoratedTabLayoutPanel;
import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.ServerPlugins;
import org.jboss.bpm.console.client.common.PropertyGrid;
import org.jboss.bpm.console.client.common.WidgetWindowPanel;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.model.StringRef;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.workspaces.client.framework.Registry;

public class InstanceDetailView extends CaptionLayoutPanel
  implements ViewInterface
{
  public static final String ID = InstanceDetailView.class.getName();
  private Controller controller;
  private PropertyGrid grid;
  private ProcessInstanceRef currentInstance;
  private Button diagramBtn;
  private Button instanceDataBtn;
  @SuppressWarnings("unused")
private WidgetWindowPanel diagramWindowPanel;
  @SuppressWarnings("unused")
private WidgetWindowPanel instanceDataWindowPanel;
  private ApplicationContext appContext;
  private ActivityDiagramView diagramView;
  private InstanceDataView instanceDataView;
  private boolean hasDiagramPlugin;
  private SimpleDateFormat dateFormat = new SimpleDateFormat();
  private ProcessDefinitionRef currentDefintion;
  private boolean isRiftsawInstance;
  private boolean isjBPMInstance;
  private ListBox<String> processEvents;

  public InstanceDetailView()
  {
    super("Execution details");

    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
    this.isRiftsawInstance = this.appContext.getConfig().getProfileName().equals("BPEL Console");
    this.isjBPMInstance = this.appContext.getConfig().getProfileName().equals("jBPM Console");

    if (this.isRiftsawInstance) {
      this.controller = ((Controller)Registry.get(Controller.class));
      this.controller.addView(ID, this);
      this.controller.addAction(GetProcessInstanceEventsAction.ID, new GetProcessInstanceEventsAction());
    }

    super.setStyleName("bpm-detail-panel");
    super.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));

    this.grid = new PropertyGrid(new String[] { "Process:", "Instance ID:", "State", "Start Date:", "Activity:" });

    add(this.grid, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    MosaicPanel buttonPanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    if (this.isRiftsawInstance) {
      this.diagramBtn = new Button("Execution Path", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          String diagramUrl = InstanceDetailView.this.getCurrentDefintion().getDiagramUrl();
          if ((diagramUrl != null) && (!diagramUrl.equals("")))
          {
            final ProcessInstanceRef selection = InstanceDetailView.this.getCurrentInstance();
            if (selection != null)
            {
              InstanceDetailView.this.createDiagramWindow(selection);

              DeferredCommand.addCommand(new Command()
              {
                public void execute() {
                  InstanceDetailView.this.controller.handleEvent(new Event(LoadInstanceActivityImage.class.getName(), selection));
                }

              });
            }

          }
          else
          {
            MessageBox.alert("Incomplete deployment", "No diagram associated with process");
          }
        }
      });
    }
    else if (this.isjBPMInstance) {
      this.diagramBtn = new Button("Diagram", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          String diagramUrl = InstanceDetailView.this.getCurrentDefintion().getDiagramUrl();
          if ((diagramUrl != null) && (!diagramUrl.equals("")))
          {
            ProcessInstanceRef selection = InstanceDetailView.this.getCurrentInstance();
            if (selection != null)
            {
              InstanceDetailView.this.createDiagramWindow(selection);
              InstanceDetailView.this.controller.handleEvent(new Event(LoadActivityDiagramAction.ID, selection));
            }

          }
          else
          {
            MessageBox.alert("Incomplete deployment", "No diagram associated with process");
          }
        }
      });
      this.diagramBtn.setVisible(!this.isRiftsawInstance);
    }
    this.diagramBtn.setEnabled(false);
    buttonPanel.add(this.diagramBtn, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    this.instanceDataBtn = new Button("Instance Data", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        if (InstanceDetailView.this.currentInstance != null)
        {
          InstanceDetailView.this.createDataWindow(InstanceDetailView.this.currentInstance);
          InstanceDetailView.this.controller.handleEvent(new Event(UpdateInstanceDataAction.ID, InstanceDetailView.this.currentInstance.getId()));
        }
      }
    });
    this.instanceDataBtn.setEnabled(false);
    buttonPanel.add(this.instanceDataBtn, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    add(buttonPanel);

    this.hasDiagramPlugin = ServerPlugins.has("org.jboss.bpm.console.server.plugin.GraphViewerPlugin");
  }

  private void createDiagramWindow(ProcessInstanceRef inst)
  {
    if (this.isRiftsawInstance) {
      LayoutPanel layout = new ScrollLayoutPanel();
      layout.setStyleName("bpm-window-layout");
      layout.setPadding(5);

      Label header = new Label("Instance: " + inst.getId());
      header.setStyleName("bpm-label-header");
      layout.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      DecoratedTabLayoutPanel tabPanel = new DecoratedTabLayoutPanel(false);
      tabPanel.setPadding(5);

      MosaicPanel diaViewLayout = new MosaicPanel();
      diaViewLayout.add(this.diagramView, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      tabPanel.add(this.diagramView, "View");

      this.processEvents = new ListBox<String>(new String[] { "Process Events" });
      this.processEvents.setCellRenderer(new ListBox.CellRenderer<String>()
      {
        public void renderCell(ListBox<String> listBox, int row, int column, String item)
        {
          switch (column) {
          case 0:
            listBox.setWidget(row, column, new HTML(item));
            break;
          default:
            throw new RuntimeException("Should not happen!");
          }
        }
      });
      MosaicPanel sourcePanel = new MosaicPanel();
      sourcePanel.add(this.processEvents, new BoxLayoutData(BoxLayoutData.FillStyle.VERTICAL));
      tabPanel.add(sourcePanel, "Source");

      tabPanel.selectTab(0);

      layout.add(tabPanel, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      this.diagramWindowPanel = new WidgetWindowPanel("Process Instance Activity", layout, true);

      this.controller.handleEvent(new Event(GetProcessInstanceEventsAction.ID, inst.getId()));
    } else if (this.isjBPMInstance) {
      MosaicPanel layout = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      layout.setStyleName("bpm-window-layout");
      layout.setPadding(5);

      Label header = new Label("Instance: " + inst.getId());
      header.setStyleName("bpm-label-header");
      layout.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      layout.add(this.diagramView, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      this.diagramWindowPanel = new WidgetWindowPanel("Process Instance Activity", layout, true);
    }
  }

  public void populateProcessInstanceEvents(List<StringRef> refs)
  {
    DefaultListModel<String> model = (DefaultListModel<String>)this.processEvents.getModel();
    model.clear();
    for (StringRef value : refs)
      model.add(formatResult(value.getValue()));
  }

  private String formatResult(String value)
  {
    StringBuffer sbuffer = new StringBuffer();
    StringTokenizer st = new StringTokenizer(value, "~");
    sbuffer.append(st.nextToken() + " : ");

    while (st.hasMoreTokens()) {
      sbuffer.append("<br/>");
      sbuffer.append(st.nextToken());
    }

    return sbuffer.toString();
  }

  private void createDataWindow(ProcessInstanceRef inst)
  {
    this.instanceDataView.clear();
    this.instanceDataWindowPanel = new WidgetWindowPanel("Process Instance Data: " + inst.getId(), this.instanceDataView, true);
  }

  public void setController(Controller controller)
  {
    this.controller = controller;

    this.diagramView = new ActivityDiagramView();
    this.instanceDataView = new InstanceDataView();

    controller.addView(ActivityDiagramView.ID, this.diagramView);
    controller.addView(InstanceDataView.ID, this.instanceDataView);
    controller.addAction(LoadActivityDiagramAction.ID, new LoadActivityDiagramAction());
    if (this.isRiftsawInstance) {
      controller.addAction(LoadInstanceActivityImage.class.getName(), new LoadInstanceActivityImage());
    }
    controller.addAction(UpdateInstanceDataAction.ID, new UpdateInstanceDataAction());
  }

  public void update(ProcessDefinitionRef def, ProcessInstanceRef instance)
  {
    this.currentDefintion = def;
    this.currentInstance = instance;

    String currentNodeName = instance.getRootToken() != null ? instance.getRootToken().getCurrentNodeName() : "n/a";

    String[] values = { def.getName(), instance.getId(), String.valueOf(instance.getState()), this.dateFormat.format(instance.getStartDate()), currentNodeName };

    this.grid.update(values);

    if (this.hasDiagramPlugin) {
      this.diagramBtn.setEnabled(true);
    }
    this.instanceDataBtn.setEnabled(true);
  }

  public void clearView()
  {
    this.grid.clear();
    this.currentDefintion = null;
    this.currentInstance = null;
    this.diagramBtn.setEnabled(false);
    this.instanceDataBtn.setEnabled(false);
  }

  private ProcessDefinitionRef getCurrentDefintion()
  {
    return this.currentDefintion;
  }

  private ProcessInstanceRef getCurrentInstance()
  {
    return this.currentInstance;
  }
}