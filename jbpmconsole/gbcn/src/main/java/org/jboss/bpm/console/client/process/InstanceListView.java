package org.jboss.bpm.console.client.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.ArrayList;
import java.util.List;
import org.gwt.mosaic.core.client.Dimension;
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
import org.jboss.bpm.console.client.common.IFrameWindowCallback;
import org.jboss.bpm.console.client.common.IFrameWindowPanel;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.common.WidgetWindowPanel;
import org.jboss.bpm.console.client.icons.ConsoleIconBundle;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.model.TokenReference;
import org.jboss.bpm.console.client.process.events.InstanceEvent;
import org.jboss.bpm.console.client.process.events.SignalInstanceEvent;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

public class InstanceListView
  implements WidgetProvider, ViewInterface, DataDriven
{
  public static final String ID = InstanceListView.class.getName();
  private Controller controller;
  private MosaicPanel instanceList = null;
  private ListBox<ProcessInstanceRef> listBox;
  private ProcessDefinitionRef currentDefinition;
  private boolean isInitialized;
  private List<ProcessInstanceRef> cachedInstances = null;

  private SimpleDateFormat dateFormat = new SimpleDateFormat();
  private ApplicationContext appContext;
  private IFrameWindowPanel iframeWindow = null;
  private boolean isRiftsawInstance;
  MosaicPanel panel;
  private Button startBtn;
  private Button terminateBtn;
  private Button deleteBtn;
  private Button signalBtn;
  private Button refreshBtn;
  private List<TokenReference> tokensToSignal = null;
  private WidgetWindowPanel signalWindowPanel;
  private ListBox<TokenReference> listBoxTokens = null;

  @SuppressWarnings("unused")
private List<TextBox> signalTextBoxes = null;

  private TextBox eventData = null;
  @SuppressWarnings("unused")
private ImageResource greenIcon;
  private TextBox signalRef = null;

  public void provideWidget(ProvisioningCallback callback)
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));

    this.isRiftsawInstance = this.appContext.getConfig().getProfileName().equals("BPEL Console");

    this.panel = new MosaicPanel();
    this.panel.setPadding(0);

    ((Controller)Registry.get(Controller.class)).addView(ID, this);
    initialize();

    ConsoleIconBundle imageBundle = (ConsoleIconBundle)GWT.create(ConsoleIconBundle.class);
    this.greenIcon = imageBundle.greenIcon();

    callback.onSuccess(this.panel);
  }

  public boolean isInitialized()
  {
    return this.isInitialized;
  }

  @SuppressWarnings("deprecation")
public void initialize()
  {
    if (!this.isInitialized)
    {
      this.instanceList = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
      this.instanceList.setPadding(0);
      this.instanceList.setWidgetSpacing(0);

      this.listBox = new ListBox<ProcessInstanceRef>(new String[] { "<b>Instance</b>", "State", "Start Date" });

      this.listBox.setCellRenderer(new ListBox.CellRenderer<ProcessInstanceRef>()
      {
        public void renderCell(ListBox<ProcessInstanceRef> listBox, int row, int column, ProcessInstanceRef item) {
          switch (column) {
          case 0:
            listBox.setText(row, column, item.getId());
            break;
          case 1:
            listBox.setText(row, column, item.getState().toString());
            break;
          case 2:
            String d = item.getStartDate() != null ? InstanceListView.this.dateFormat.format(item.getStartDate()) : "";
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
          int index = InstanceListView.this.listBox.getSelectedIndex();
          if (index != -1)
          {
            ProcessInstanceRef item = (ProcessInstanceRef)InstanceListView.this.listBox.getItem(index);

            if (InstanceListView.this.isSignalable(item))
              InstanceListView.this.signalBtn.setEnabled(true);
            else {
              InstanceListView.this.signalBtn.setEnabled(false);
            }

            InstanceListView.this.terminateBtn.setEnabled(true);

            InstanceListView.this.controller.handleEvent(new Event(UpdateInstanceDetailAction.ID, new InstanceEvent(InstanceListView.this.currentDefinition, item)));
          }
        }
      });
      MosaicPanel toolBox = new MosaicPanel();

      toolBox.setPadding(0);
      toolBox.setWidgetSpacing(5);

      ToolBar toolBar = new ToolBar();
      this.refreshBtn = new Button("Refresh", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent) {
          InstanceListView.this.controller.handleEvent(new Event(UpdateInstancesAction.ID, InstanceListView.this.getCurrentDefinition()));
        }
      });
      toolBar.add(this.refreshBtn);
      this.refreshBtn.setEnabled(false);
      toolBar.addSeparator();

      this.startBtn = new Button("Start", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          MessageBox.confirm("Start new execution", "Do you want to start a new execution of this process?", new MessageBox.ConfirmationCallback()
          {
            public void onResult(boolean doIt)
            {
              if (doIt)
              {
                String url = InstanceListView.this.getCurrentDefinition().getFormUrl();
                boolean hasForm = (url != null) && (!url.equals(""));
                if (hasForm)
                {
                  ProcessDefinitionRef definition = InstanceListView.this.getCurrentDefinition();
                  InstanceListView.this.iframeWindow = new IFrameWindowPanel(definition.getFormUrl(), "New Process Instance: " + definition.getId());

                  InstanceListView.this.iframeWindow.setCallback(new IFrameWindowCallback()
                  {
                    public void onWindowClosed()
                    {
                      InstanceListView.this.controller.handleEvent(new Event(UpdateInstancesAction.ID, InstanceListView.this.getCurrentDefinition()));
                    }
                  });
                  InstanceListView.this.iframeWindow.show();
                }
                else
                {
                  InstanceListView.this.controller.handleEvent(new Event(StartNewInstanceAction.ID, InstanceListView.this.getCurrentDefinition()));
                }
              }
            }
          });
        }
      });
      this.terminateBtn = new Button("Terminate", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          if (InstanceListView.this.getSelection() != null)
          {
            MessageBox.confirm("Terminate instance", "Terminating this instance will stop further execution.", new MessageBox.ConfirmationCallback()
            {
              public void onResult(boolean doIt)
              {
                if (doIt)
                {
                  ProcessInstanceRef selection = InstanceListView.this.getSelection();
                  selection.setState(ProcessInstanceRef.STATE.ENDED);
                  selection.setEndResult(ProcessInstanceRef.RESULT.OBSOLETE);
                  InstanceListView.this.controller.handleEvent(new Event(StateChangeAction.ID, selection));
                }

              }

            });
          }
          else
          {
            MessageBox.alert("Missing selection", "Please select an instance");
          }
        }
      });
      this.deleteBtn = new Button("Delete", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          if (InstanceListView.this.getSelection() != null)
          {
            MessageBox.confirm("Delete instance", "Deleting this instance will remove any history information and associated tasks as well.", new MessageBox.ConfirmationCallback()
            {
              public void onResult(boolean doIt)
              {
                if (doIt)
                {
                  ProcessInstanceRef selection = InstanceListView.this.getSelection();
                  selection.setState(ProcessInstanceRef.STATE.ENDED);

                  InstanceListView.this.controller.handleEvent(new Event(DeleteInstanceAction.ID, selection));
                }

              }

            });
          }
          else
          {
            MessageBox.alert("Missing selection", "Please select an instance");
          }
        }
      });
      this.signalBtn = new Button("Signal", new ClickHandler()
      {
        public void onClick(ClickEvent clickEvent)
        {
          if (InstanceListView.this.getSelection() != null)
          {
            InstanceListView.this.createSignalWindow();
          }
          else
          {
            MessageBox.alert("Missing selection", "Please select an instance");
          }
        }
      });
      if (!this.isRiftsawInstance)
      {
        toolBar.add(this.startBtn);
        toolBar.add(this.signalBtn);
        toolBar.add(this.deleteBtn);

        this.startBtn.setEnabled(false);
        this.deleteBtn.setEnabled(false);
        this.signalBtn.setEnabled(false);
      }

      toolBar.add(this.terminateBtn);
      this.terminateBtn.setEnabled(false);

      toolBox.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

      this.instanceList.add(toolBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
      this.instanceList.add(this.listBox, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

      if (this.cachedInstances != null) {
        bindData(this.cachedInstances);
      }

      MosaicPanel layout = new MosaicPanel(new BorderLayout());
      layout.setPadding(0);
      layout.add(this.instanceList, new BorderLayoutData(BorderLayout.Region.CENTER));

      InstanceDetailView detailsView = new InstanceDetailView();
      this.controller.addView(InstanceDetailView.ID, detailsView);
      this.controller.addAction(UpdateInstanceDetailAction.ID, new UpdateInstanceDetailAction());
      this.controller.addAction(ClearInstancesAction.ID, new ClearInstancesAction());
      this.controller.addAction(SignalExecutionAction.ID, new SignalExecutionAction());
      layout.add(detailsView, new BorderLayoutData(BorderLayout.Region.SOUTH, 10, 200));

      this.panel.add(layout);

      this.isInitialized = true;
    }
  }

  public ProcessInstanceRef getSelection()
  {
    ProcessInstanceRef selection = null;
    if (this.listBox.getSelectedIndex() != -1)
      selection = (ProcessInstanceRef)this.listBox.getItem(this.listBox.getSelectedIndex());
    return selection;
  }

  public ProcessDefinitionRef getCurrentDefinition()
  {
    return this.currentDefinition;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void reset()
  {
    this.currentDefinition = null;
    this.cachedInstances = new ArrayList<ProcessInstanceRef>();
    renderUpdate();

    this.startBtn.setEnabled(false);
    this.terminateBtn.setEnabled(false);
    this.deleteBtn.setEnabled(false);
    this.signalBtn.setEnabled(false);
    this.refreshBtn.setEnabled(false);
  }

  @SuppressWarnings("unchecked")
public void update(Object[] data)
  {
    this.currentDefinition = ((ProcessDefinitionRef)data[0]);
    this.cachedInstances = ((List<ProcessInstanceRef>)data[1]);

    renderUpdate();
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

      this.controller.handleEvent(new Event(UpdateInstanceDetailAction.ID, new InstanceEvent(this.currentDefinition, null)));

      this.startBtn.setEnabled(true);
      this.deleteBtn.setEnabled(true);
      this.refreshBtn.setEnabled(true);
      this.signalBtn.setEnabled(false);
    }
  }

  private void bindData(List<ProcessInstanceRef> instances)
  {
    DefaultListModel<ProcessInstanceRef> model = (DefaultListModel<ProcessInstanceRef>)this.listBox.getModel();

    model.clear();

    List<ProcessInstanceRef> list = instances;
    for (ProcessInstanceRef inst : list)
    {
      model.add(inst);
    }

    this.panel.invalidate();
  }

  private boolean isSignalable(ProcessInstanceRef processInstance)
  {
    this.tokensToSignal = new ArrayList<TokenReference>();

    if ((processInstance.getRootToken() != null) && (processInstance.getRootToken().canBeSignaled())) {
      this.tokensToSignal.add(processInstance.getRootToken());
    }
    else if ((processInstance.getRootToken() != null) && (processInstance.getRootToken().getChildren() != null))
    {
      collectSignalableTokens(processInstance.getRootToken(), this.tokensToSignal);
    }

    if (this.tokensToSignal.size() > 0) {
      return true;
    }
    return false;
  }

  private void collectSignalableTokens(TokenReference tokenParent, List<TokenReference> tokensToSignal)
  {
    if (tokenParent.getChildren() != null)
      for (TokenReference token : tokenParent.getChildren()) {
        if (token.canBeSignaled()) {
          tokensToSignal.add(token);
        }

        collectSignalableTokens(token, tokensToSignal);
      }
  }

  private void createSignalWindow()
  {
    this.signalTextBoxes = new ArrayList<TextBox>();

    MosaicPanel layout = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    layout.setStyleName("bpm-window-layout");
    layout.setPadding(5);

    MosaicPanel toolBox = new MosaicPanel();

    toolBox.setPadding(0);
    toolBox.setWidgetSpacing(5);
    toolBox.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));

    ToolBar toolBar = new ToolBar();
    toolBar.add(new Button("Signal", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        int selectedToken = InstanceListView.this.listBoxTokens.getSelectedIndex();

        if ((selectedToken != -1) && (InstanceListView.this.signalRef.getText().length() > 0))
        {
          MessageBox.alert("Multi selection", "Known active nodes and signal ref (text box) is given, please choose only one of them");
        }
        else if (selectedToken != -1)
        {
          InstanceListView.this.controller.handleEvent(new Event(SignalExecutionAction.ID, new SignalInstanceEvent(InstanceListView.this.getCurrentDefinition(), InstanceListView.this.getSelection(), (TokenReference)InstanceListView.this.listBoxTokens.getItem(selectedToken), InstanceListView.this.eventData.getText(), selectedToken)));
        }
        else if (InstanceListView.this.signalRef.getText().length() > 0)
        {
          TokenReference token = new TokenReference();
          token.setId(InstanceListView.this.getSelection().getId());
          token.setName(InstanceListView.this.signalRef.getText());
          int foundMatch = -1;
          int index = 0;

          for (TokenReference ref : InstanceListView.this.tokensToSignal)
          {
            if (ref.getName().equals(token.getName())) {
              foundMatch = index;
              break;
            }
            index++;
          }

          InstanceListView.this.controller.handleEvent(new Event(SignalExecutionAction.ID, new SignalInstanceEvent(InstanceListView.this.getCurrentDefinition(), InstanceListView.this.getSelection(), token, InstanceListView.this.eventData.getText(), foundMatch)));
        }
        else
        {
          MessageBox.alert("Incomplete selection", "Please select element you want to signal");
        }
      }
    }));
    toolBar.add(new Button("Close", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        InstanceListView.this.signalWindowPanel.close();
        InstanceListView.this.controller.handleEvent(new Event(UpdateInstancesAction.ID, InstanceListView.this.getCurrentDefinition()));
      }
    }));
    toolBox.add(toolBar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    layout.add(toolBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    Label header = new Label("Known active nodes to signal: ");
    header.setStyleName("bpm-label-header");
    layout.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    this.listBoxTokens = new ListBox<TokenReference>(new String[] { "Node name", "Signal ref" });

    this.listBoxTokens.setCellRenderer(new ListBox.CellRenderer<TokenReference>()
    {
      public void renderCell(ListBox<TokenReference> listBox, int row, int column, TokenReference item) {
        switch (column) {
        case 0:
          listBox.setText(row, column, item.getCurrentNodeName());
          break;
        case 1:
          listBox.setText(row, column, item.getName());
          break;
        default:
          throw new RuntimeException("Unexpected column size");
        }
      }
    });
    renderSignalListBox(-1);
    layout.add(this.listBoxTokens, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    Label headerSignalRef = new Label("Signal ref");
    headerSignalRef.setStyleName("bpm-label-header");
    layout.add(headerSignalRef, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    this.signalRef = new TextBox();
    this.signalRef.addFocusHandler(new FocusHandler()
    {
      public void onFocus(FocusEvent event)
      {
        if (InstanceListView.this.listBox.getSelectedIndex() != -1)
          InstanceListView.this.listBoxTokens.setItemSelected(InstanceListView.this.listBox.getSelectedIndex(), false);
      }
    });
    layout.add(this.signalRef, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    Label headerSignals = new Label("Event data");
    headerSignals.setStyleName("bpm-label-header");
    layout.add(headerSignals, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    this.eventData = new TextBox();

    layout.add(this.eventData, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    this.signalWindowPanel = new WidgetWindowPanel("Signal process", layout, false);

    this.signalWindowPanel.setSize(new Dimension(500, 400));
  }

  public void renderSignalListBox(int i)
  {
    if (i > -1) {
      this.tokensToSignal.remove(i);
    }

    ((DefaultListModel<TokenReference>)this.listBoxTokens.getModel()).clear();
    if (!this.tokensToSignal.isEmpty())
    {
      for (TokenReference token : this.tokensToSignal) {
        ((DefaultListModel<TokenReference>)this.listBoxTokens.getModel()).add(token);
      }
    }

    if (this.signalRef != null)
    {
      this.signalRef.setText("");
    }
    if (this.eventData != null)
    {
      this.eventData.setText("");
    }
  }
}