package org.jboss.bpm.console.client.process;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.ArrayList;
import java.util.List;

import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.ToolBar;
import org.gwt.mosaic.ui.client.event.RowSelectionEvent;
import org.gwt.mosaic.ui.client.event.RowSelectionHandler;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.gwt.mosaic.ui.client.table.AbstractScrollTable;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.common.ModelParts;
import org.jboss.bpm.console.client.common.PagingCallback;
import org.jboss.bpm.console.client.common.PagingPanel;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

public class DefinitionListView
  implements WidgetProvider, ViewInterface, DataDriven
{
  public static final String ID = DefinitionListView.class.getName();
  private Controller controller;
  private MosaicPanel definitionList = null;
  private org.gwt.mosaic.ui.client.ListBox<ProcessDefinitionRef> listBox;
  private boolean isInitialized;
  private int FILTER_NONE = 10;
  private int FILTER_ACTIVE = 20;
  private int FILTER_SUSPENDED = 30;
  private int currentFilter = this.FILTER_NONE;

  private List<ProcessDefinitionRef> definitions = null;
  private PagingPanel pagingPanel;
  private MosaicPanel panel;

  public void provideWidget(ProvisioningCallback callback)
  {
    this.panel = new MosaicPanel();
    this.panel.setWidgetSpacing(0);
    this.panel.setPadding(0);

    this.listBox = createListBox();
    final Controller controller = (Controller)Registry.get(Controller.class);
    controller.addView(ID, this);

    controller.addAction(UpdateInstancesAction.ID, new UpdateInstancesAction());
    controller.addAction(StartNewInstanceAction.ID, new StartNewInstanceAction());
    controller.addAction(StateChangeAction.ID, new StateChangeAction());
    controller.addAction(DeleteDefinitionAction.ID, new DeleteDefinitionAction());
    controller.addAction(DeleteInstanceAction.ID, new DeleteInstanceAction());
    controller.addAction(UpdateDefinitionsAction.ID, new UpdateDefinitionsAction());

    initialize();

    Timer t = new Timer()
    {
      public void run()
      {
        controller.handleEvent(new Event(UpdateDefinitionsAction.ID, null));
      }
    };
    t.schedule(500);

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
      this.definitionList = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      this.definitionList.setPadding(0);
      this.definitionList.setWidgetSpacing(0);

      MosaicPanel toolBox = new MosaicPanel();
      toolBox.setPadding(0);
      toolBox.setWidgetSpacing(0);
      toolBox.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));

      ToolBar toolBar = new ToolBar();
      ClickHandler clickHandler = new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          DefinitionListView.this.reload();
        }
      };
      toolBar.add(new Button("Refresh", clickHandler));

      toolBox.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      MosaicPanel filterPanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      filterPanel.setStyleName("mosaic-ToolBar");
      final com.google.gwt.user.client.ui.ListBox dropBox = new com.google.gwt.user.client.ui.ListBox(false);
      dropBox.setStyleName("bpm-operation-ui");
      dropBox.addItem("All");
      dropBox.addItem("Active");
      dropBox.addItem("Retired");

      dropBox.addChangeHandler(new ChangeHandler()
      {
        public void onChange(ChangeEvent changeEvent)
        {
          switch (dropBox.getSelectedIndex())
          {
          case 0:
            DefinitionListView.this.currentFilter = DefinitionListView.this.FILTER_NONE;
            break;
          case 1:
            DefinitionListView.this.currentFilter = DefinitionListView.this.FILTER_ACTIVE;
            break;
          case 2:
            DefinitionListView.this.currentFilter = DefinitionListView.this.FILTER_SUSPENDED;
            break;
          default:
            throw new IllegalArgumentException("No such index");
          }

          DefinitionListView.this.renderFiltered();
        }
      });
      filterPanel.add(dropBox);

      toolBox.add(filterPanel, new BoxLayoutData(BoxLayoutData.FillStyle.VERTICAL));

      this.definitionList.add(toolBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
      this.definitionList.add(this.listBox, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
      this.pagingPanel = new PagingPanel(new PagingCallback()
      {
        public void rev()
        {
          DefinitionListView.this.renderFiltered();
        }

        public void ffw()
        {
          DefinitionListView.this.renderFiltered();
        }
      });
      this.definitionList.add(this.pagingPanel, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      this.panel.add(this.definitionList);

      ErraiBus.get().subscribe("appContext.model.listener", new MessageCallback()
      {
    
		@Override
		public void callback(Message message) {
			switch(org.jboss.bpm.console.client.common.ModelCommands.valueOf(message.getCommandType()).ordinal()){
			case 1:
              if (((String)message.get(String.class, ModelParts.CLASS)).equals("deploymentModel"))
                DefinitionListView.this.reload();
              break;
            }
			}
	    });
      this.isInitialized = true;
    }
  }

  private void reload()
  {
    DeferredCommand.addCommand(new Command()
    {
      public void execute()
      {
        DefaultListModel<ProcessDefinitionRef> model = (DefaultListModel<ProcessDefinitionRef>)DefinitionListView.this.listBox.getModel();

        model.clear();

        DefinitionListView.this.controller.handleEvent(new Event(UpdateDefinitionsAction.ID, null));
      }
    });
  }

  private ListBox<ProcessDefinitionRef> createListBox()
  {
    final ListBox<ProcessDefinitionRef> listBox = new org.gwt.mosaic.ui.client.ListBox<ProcessDefinitionRef>(new String[] { "<b>Process</b>", "v." });

    listBox.setFocus(true);

    listBox.setCellRenderer(new ListBox.CellRenderer<ProcessDefinitionRef>()
    {
      public void renderCell(org.gwt.mosaic.ui.client.ListBox<ProcessDefinitionRef> listBox, int row, int column, ProcessDefinitionRef item) {
        switch (column)
        {
        case 0:
          String name = item.getName();
          String s = name.indexOf("}") > 0 ? name.substring(name.lastIndexOf("}") + 1, name.length()) : name;

          String color = item.isSuspended() ? "#CCCCCC" : "#000000";
          String text = "<div style=\"color:" + color + "\">" + s + "</div>";

          listBox.setWidget(row, column, new HTML(text));
          break;
        case 1:
          listBox.setText(row, column, String.valueOf(item.getVersion()));
          break;
        case 2:
          listBox.setText(row, column, String.valueOf(item.isSuspended()));
          break;
        default:
          throw new RuntimeException("Unexpected column size");
        }
      }
    });
    listBox.setMinimumColumnWidth(0, 190);
    listBox.setColumnResizePolicy(AbstractScrollTable.ColumnResizePolicy.MULTI_CELL);

    listBox.addRowSelectionHandler(new RowSelectionHandler()
    {
      public void onRowSelection(RowSelectionEvent rowSelectionEvent)
      {
        int index = listBox.getSelectedIndex();
        if (index != -1)
        {
          ProcessDefinitionRef item = (ProcessDefinitionRef)listBox.getItem(index);

          DefinitionListView.this.controller.handleEvent(new Event(UpdateInstancesAction.ID, item));
        }
      }
    });
    return listBox;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void reset()
  {
    DefaultListModel<ProcessDefinitionRef> model = (DefaultListModel<ProcessDefinitionRef>)this.listBox.getModel();

    model.clear();

    this.controller.handleEvent(new Event(ClearInstancesAction.ID, null));
  }

  @SuppressWarnings("unchecked")
public void update(Object[] data)
  {
    this.definitions = ((List<ProcessDefinitionRef>)data[0]);
    this.pagingPanel.reset();
    renderFiltered();
  }

  public void setLoading(boolean isLoading)
  {
    LoadingOverlay.on(this.panel, isLoading);
  }

  private void renderFiltered()
  {
    if (this.definitions != null)
    {
      reset();

      DefaultListModel<ProcessDefinitionRef> model = (DefaultListModel<ProcessDefinitionRef>)this.listBox.getModel();

      List<ProcessDefinitionRef> tmp = new ArrayList<ProcessDefinitionRef>();
      for (ProcessDefinitionRef def : this.definitions)
      {
        if (this.FILTER_NONE == this.currentFilter)
        {
          tmp.add(def);
        }
        else
        {
          boolean showSuspended = this.FILTER_SUSPENDED == this.currentFilter;
          if (def.isSuspended() == showSuspended) {
            tmp.add(def);
          }
        }
      } 
      for (Object def : this.pagingPanel.trim(tmp)) {
        model.add((ProcessDefinitionRef)def);
      }
      if (this.listBox.getSelectedIndex() != -1)
        this.listBox.setItemSelected(this.listBox.getSelectedIndex(), false);
    }
  }

  public ProcessDefinitionRef getSelection()
  {
    ProcessDefinitionRef selection = null;
    if ((isInitialized()) && (this.listBox.getSelectedIndex() != -1))
      selection = (ProcessDefinitionRef)this.listBox.getItem(this.listBox.getSelectedIndex());
    return selection;
  }
}