package org.jboss.bpm.console.client.process.v2;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import org.gwt.mosaic.ui.client.DecoratedTabLayoutPanel;
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
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.process.DeleteDefinitionAction;
import org.jboss.bpm.console.client.process.DeleteInstanceAction;
import org.jboss.bpm.console.client.process.InstanceListView;
import org.jboss.bpm.console.client.process.StartNewInstanceAction;
import org.jboss.bpm.console.client.process.StateChangeAction;
import org.jboss.bpm.console.client.process.UpdateDefinitionsAction;
import org.jboss.bpm.console.client.process.UpdateInstancesAction;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;
import org.jboss.errai.workspaces.client.framework.Registry;
import org.jboss.errai.workspaces.client.protocols.LayoutCommands;
import org.jboss.errai.workspaces.client.protocols.LayoutParts;

@LoadTool(name="Manage Instances", group="Processes", icon="processIcon", priority=1)
public class Explorer
  implements WidgetProvider, DataDriven, ViewInterface
{
  private LayoutPanel layout;
  private LayoutPanel definitionPanel;
  private ToolButton menuButton;
  private HTML title;
  private String selectedGroup;
  private Controller controller;
  private ProcessGroups processGroups = null;
  private ProcessDefinitionRef activeDefinition;

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  @SuppressWarnings("deprecation")
public void provideWidget(ProvisioningCallback callback)
  {
    initController();

    this.layout = new LayoutPanel(new BorderLayout());

    this.definitionPanel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    this.definitionPanel.setPadding(0);

    ToolBar toolBar = new ToolBar();
    this.definitionPanel.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    this.menuButton = new ToolButton("Open", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent) {
        Explorer.this.controller.handleEvent(new Event(UpdateDefinitionsAction.ID, null));
      }
    });
    toolBar.add(this.menuButton);

    this.title = new HTML();
    this.title.getElement().setAttribute("style", "font-size:24px; font-weight:BOLD");

    LayoutPanel headerPanel = new LayoutPanel(new ColumnLayout());
    headerPanel.add(this.title, new ColumnLayoutData("70%"));

    LayoutPanel actionPanel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    actionPanel.getElement().setAttribute("style", "margin-right:10px;");
    ToolButton actions = new ToolButton("More ...");
    actions.setStyle(ToolButton.ToolButtonStyle.MENU);
    @SuppressWarnings("unused")
	Command blank = new Command()
    {
      public void execute()
      {
      }
    };
    PopupMenu actionMenu = new PopupMenu();

    actionMenu.addItem("Execution History", new Command()
    {
      public void execute()
      {
        if (Explorer.this.getActiveDefinition() != null)
        {
          ((MessageBuildSendableWithReply)MessageBuilder.createMessage().toSubject("Workspace").command(LayoutCommands.ActivateTool).with(LayoutParts.TOOL, "Execution_History.2").with(LayoutParts.TOOLSET, "ToolSet_Processes").noErrorHandling()).sendNowWith(ErraiBus.get());

          ProcessDefinitionRef ref = Explorer.this.getActiveDefinition();
          ((MessageBuildSendableWithReply)MessageBuilder.createMessage().toSubject("process.execution.history").signalling().with("processName", ref.getName() + "-" + ref.getVersion()).with("processDefinitionId", ref.getId()).noErrorHandling()).sendNowWith(ErraiBus.get());
        }
      }
    });
    actionMenu.addItem("Change Version", new Command()
    {
      public void execute() {
        if (Explorer.this.getActiveDefinition() != null)
          Explorer.this.selectVersion();
      }
    });
    actions.setMenu(actionMenu);

    actions.getElement().setAttribute("style", "widht:30px; height:12px; padding-right:0px;background-image:none;");

    actionPanel.add(actions, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    headerPanel.add(actionPanel, new ColumnLayoutData("30%"));

    this.definitionPanel.add(headerPanel, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    InstanceListView instanceView = new InstanceListView();
    final DecoratedTabLayoutPanel tabPanel = new DecoratedTabLayoutPanel(false);
    instanceView.provideWidget(new ProvisioningCallback()
    {
      public void onSuccess(Widget instance) {
        tabPanel.add(instance, "Running");
      }

      public void onUnavailable()
      {
      }
    });
    this.layout.add(this.definitionPanel, new BorderLayoutData(BorderLayout.Region.NORTH, 130.0D));
    this.layout.add(tabPanel);

    callback.onSuccess(this.layout);
  }

  private void initController() {
    Controller controller = (Controller)Registry.get(Controller.class);
    controller.addView(Explorer.class.getName(), this);
    controller.addAction(UpdateInstancesAction.ID, new UpdateInstancesAction());
    controller.addAction(StartNewInstanceAction.ID, new StartNewInstanceAction());
    controller.addAction(StateChangeAction.ID, new StateChangeAction());
    controller.addAction(DeleteDefinitionAction.ID, new DeleteDefinitionAction());
    controller.addAction(DeleteInstanceAction.ID, new DeleteInstanceAction());
    controller.addAction(UpdateDefinitionsAction.ID, new UpdateDefinitionsAction());
  }

  public void reset()
  {
  }

  @SuppressWarnings("unchecked")
public void update(Object[] data) {
    this.processGroups = new ProcessGroups((List<ProcessDefinitionRef>)data[0]);
    selectDefinition();
  }

  public void setLoading(boolean isLoading) {
    LoadingOverlay.on(this.definitionPanel, isLoading);
  }

  private void selectDefinition()
  {
    final LayoutPopupPanel popup = new LayoutPopupPanel(true);
    popup.addStyleName("soa-PopupPanel");
    popup.setWidth("30%");

    final ListBox listBox = new ListBox();
    listBox.addItem("");

    assert (this.processGroups != null) : "process definitions not loaded";

    for (String group : this.processGroups.getGroups())
    {
      listBox.addItem(group);
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
          Explorer.this.selectedGroup = listBox.getItemText(listBox.getSelectedIndex());

          Explorer.this.identifyActiveVersion();

          if (null == Explorer.this.getActiveDefinition())
          {
            Explorer.this.identifyMostRecentVersion();
          }

          Explorer.this.updateTitle();

          Explorer.this.refresh();
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

    popup.setPopupPosition(this.menuButton.getAbsoluteLeft() - 2, this.menuButton.getAbsoluteTop() + 30);
    popup.setWidget(p);
    popup.pack();
    popup.show();
  }

  private void identifyActiveVersion()
  {
    for (ProcessDefinitionRef groupMemmber : this.processGroups.getProcessesForGroup(this.selectedGroup))
    {
      if (!groupMemmber.isSuspended())
      {
        setActiveDefinition(groupMemmber);
        break;
      }
    }
  }

  private void identifyMostRecentVersion() {
    ProcessDefinitionRef mostRecent = null;

    for (ProcessDefinitionRef groupMember : this.processGroups.getProcessesForGroup(this.selectedGroup))
    {
      if ((null == mostRecent) || (groupMember.getVersion() > mostRecent.getVersion())) {
        mostRecent = groupMember;
      }
    }
    setActiveDefinition(mostRecent);
  }

  private void refresh()
  {
    if (getActiveDefinition() != null)
    {
      DeferredCommand.addCommand(new Command()
      {
        public void execute() {
          Explorer.this.controller.handleEvent(new Event(UpdateInstancesAction.ID, Explorer.this.getActiveDefinition()));
        }
      });
    }
  }

  private void updateTitle()
  {
    ProcessDefinitionRef ref = getActiveDefinition();

    String name = this.selectedGroup;
    String subtitle = "";
    if (this.selectedGroup.indexOf("}") != -1)
    {
      String[] qname = this.selectedGroup.split("}");
      name = qname[1];
      subtitle = qname[0].substring(1, qname[0].length());
    }

    String nameAndSubtitle = name + "<br/><div style='color:#C8C8C8;font-size:12px;text-align:left;'>" + subtitle + "</div>";
    StringBuffer sb = new StringBuffer("<p/><div style='font-size:12px;text-align:left;'>Version: ");

    String state = ref.isSuspended() ? "suspended" : "active";
    sb.append(ref.getVersion()).append(" (").append(state).append(")");
    sb.append("</div>");

    this.title.setHTML(nameAndSubtitle + sb.toString());
  }

  private void selectVersion()
  {
    final LayoutPopupPanel popup = new LayoutPopupPanel(true);
    popup.addStyleName("soa-PopupPanel");

    final ListBox listBox = new ListBox();
    listBox.addItem("");
    listBox.setWidth("50%");

    assert (this.selectedGroup != null) : "no process selected";

    for (ProcessDefinitionRef def : this.processGroups.getProcessesForGroup(this.selectedGroup))
    {
      listBox.addItem(String.valueOf(def.getVersion()));
    }

    LayoutPanel p = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    p.add(new HTML("Please select a process version:"));
    p.add(listBox);

    LayoutPanel p2 = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    p2.add(new Button("Done", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent) {
        if (listBox.getSelectedIndex() > 0)
        {
          Explorer.this.setActiveDefinition(Explorer.this.getSelectedVersion(listBox));

          Explorer.this.updateTitle();

          Explorer.this.refresh();

          popup.hide();
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

    popup.setPopupPosition(this.menuButton.getAbsoluteLeft() - 2, this.menuButton.getAbsoluteTop() + 30);
    popup.setWidget(p);
    popup.pack();
    popup.show();
  }

  private ProcessDefinitionRef getSelectedVersion(ListBox listBox) {
    ProcessDefinitionRef selection = null;
    for (ProcessDefinitionRef ref : this.processGroups.getProcessesForGroup(this.selectedGroup))
    {
      if (ref.getVersion() == Long.valueOf(listBox.getItemText(listBox.getSelectedIndex())).longValue())
      {
        selection = ref;
        break;
      }
    }

    return selection;
  }

  public void setActiveDefinition(ProcessDefinitionRef ref) {
    this.activeDefinition = ref;
  }

  private ProcessDefinitionRef getActiveDefinition() {
    return this.activeDefinition;
  }
}