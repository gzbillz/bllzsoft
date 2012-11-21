package org.jboss.bpm.console.client.history;

import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import java.util.StringTokenizer;
import org.gwt.mosaic.ui.client.DecoratedTabLayoutPanel;
import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.common.WidgetWindowPanel;
import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;
import org.jboss.bpm.console.client.model.StringRef;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

public class ProcessHistoryInstanceListView
  implements ViewInterface, WidgetProvider, DataDriven
{
  public static final String ID = ProcessHistoryInstanceListView.class.getName();
  private Controller controller;
  private MosaicPanel panel;
  private MosaicPanel instanceList;
  private ListBox<HistoryProcessInstanceRef> listbox;
  private SimpleDateFormat dateFormat = new SimpleDateFormat();
  @SuppressWarnings("unused")
private WidgetWindowPanel processEventsWindow;
  private ListBox<String> processEvents;
  private String selectedProcessInstanceId;

  public void provideWidget(ProvisioningCallback callback)
  {
    this.controller = ((Controller)Registry.get(Controller.class));
    this.controller.addView(ID, this);
    this.controller.addAction(LoadProcessInstanceEventsAction.ID, new LoadProcessInstanceEventsAction());

    this.panel = new MosaicPanel();
    this.panel.setPadding(0);
    this.panel.setWidgetSpacing(5);

    this.instanceList = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    this.instanceList.setPadding(0);
    this.instanceList.setWidgetSpacing(5);

    this.listbox = new ListBox<HistoryProcessInstanceRef>(new String[] { "Instance Id", "Correlation Key", "Status", "Start Time", "Finish Time" });
    this.listbox.setCellRenderer(new ListBox.CellRenderer<HistoryProcessInstanceRef>()
    {
      public void renderCell(ListBox<HistoryProcessInstanceRef> listBox, int row, int column, HistoryProcessInstanceRef item)
      {
        switch (column) {
        case 0:
          listBox.setText(row, column, item.getProcessInstanceId());
          break;
        case 1:
          listBox.setText(row, column, item.getKey());
          break;
        case 2:
          listBox.setText(row, column, item.getState());
          break;
        case 3:
          listBox.setText(row, column, ProcessHistoryInstanceListView.this.dateFormat.format(item.getStartTime()));
          break;
        case 4:
          listBox.setText(row, column, ProcessHistoryInstanceListView.this.dateFormat.format(item.getEndTime()));
          break;
        default:
          throw new RuntimeException("Should not happen!");
        }
      }
 
    });
    this.listbox.addDoubleClickHandler(new DoubleClickHandler()
    {
      public void onDoubleClick(DoubleClickEvent event)
      {
        int index = ProcessHistoryInstanceListView.this.listbox.getSelectedIndex();
        if (index != -1) {
          HistoryProcessInstanceRef historyInstance = (HistoryProcessInstanceRef)ProcessHistoryInstanceListView.this.listbox.getItem(index);
          ProcessHistoryInstanceListView.this.selectedProcessInstanceId = historyInstance.getProcessInstanceId();
          ProcessHistoryInstanceListView.this.createHistoryInstanceDetailWindow();
        }
      }
    });
    this.instanceList.add(this.listbox, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    this.panel.add(this.instanceList);
    callback.onSuccess(this.panel);
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void reset()
  {
  }

  public void update(Object[] data)
  {
    @SuppressWarnings("unchecked")
	List<HistoryProcessInstanceRef> result = (List<HistoryProcessInstanceRef>)data[0];
    DefaultListModel<HistoryProcessInstanceRef> model = (DefaultListModel<HistoryProcessInstanceRef>)this.listbox.getModel();
    model.clear();

    for (HistoryProcessInstanceRef ref : result) {
      model.add(ref);
    }
    this.panel.invalidate();
  }

  public void setLoading(boolean isLoading)
  {
    LoadingOverlay.on(this.instanceList, isLoading);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
public void createHistoryInstanceDetailWindow()
  {
    LayoutPanel layout = new ScrollLayoutPanel();
    layout.setStyleName("bpm-window-layout");
    layout.setPadding(5);

    Label header = new Label("Instance: " + this.selectedProcessInstanceId);
    header.setStyleName("bpm-label-header");
    layout.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    DecoratedTabLayoutPanel tabPanel = new DecoratedTabLayoutPanel(false);
    tabPanel.setPadding(5);

    this.processEvents = new ListBox(new String[] { "Process Events" });
    this.processEvents.setCellRenderer(new ListBox.CellRenderer()
    { 

	@Override
	public void renderCell(ListBox listBox, int row, int column, Object item) {
		switch (column) {
        case 0:
          listBox.setWidget(row, column, new HTML(item.toString()));
          break;
        default:
          throw new RuntimeException("Should not happen!");
        }
	}
 
    });
    MosaicPanel sourcePanel = new MosaicPanel();
    sourcePanel.add(this.processEvents, new BoxLayoutData(BoxLayoutData.FillStyle.VERTICAL));
    tabPanel.add(sourcePanel, "Activity Events");

    tabPanel.selectTab(0);

    layout.add(tabPanel, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    this.processEventsWindow = new WidgetWindowPanel("History Instance Activity", layout, true);

    this.controller.handleEvent(new Event(LoadProcessInstanceEventsAction.ID, this.selectedProcessInstanceId));
  }

  public void populateInstanceEvents(List<StringRef> refs)
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
}