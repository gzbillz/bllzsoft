package org.jboss.bpm.console.client.report;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.report.model.ReportReference;
import org.jboss.errai.workspaces.client.framework.Registry;

class ReportLaunchPadView extends MosaicPanel
  implements ViewInterface, DataDriven
{
  public static final String ID = ReportLaunchPadView.class.getName();
  private Controller controller;
  private MosaicPanel inputPanel;
  private ListBox dropBox;
  List<ReportReference> reportTemplates;
  private HTML description;
  private ReportFrame reportFrame;
  private Map<String, ReportParameterForm> forms = new HashMap<String, ReportParameterForm>();

  public ReportLaunchPadView()
  {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    setPadding(5);

    CaptionLayoutPanel header = new CaptionLayoutPanel("Report configuration");
    header.setStyleName("bpm-detail-panel");

    header.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));

    MosaicPanel templatePanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    this.description = new HTML();

    this.dropBox = new ListBox(false);
    this.dropBox.addChangeHandler(new ChangeHandler()
    {
      public void onChange(ChangeEvent changeEvent)
      {
        String reportTitle = ReportLaunchPadView.this.dropBox.getItemText(ReportLaunchPadView.this.dropBox.getSelectedIndex());
        ReportLaunchPadView.this.selectForm(reportTitle);
      }
    });
    templatePanel.add(this.dropBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    templatePanel.add(this.description, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    this.inputPanel = new MosaicPanel();

    header.add(templatePanel, new BoxLayoutData("250 px", "100 px"));
    header.add(this.inputPanel, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    this.reportFrame = new ReportFrame();

    add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    add(this.reportFrame, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
  }

  private ReportParameterForm createInput(final ReportReference reportRef)
  {
    ReportParameterForm form = new ReportParameterForm(reportRef, new ReportParamCallback()
    {
      public void onSumbit(Map<String, String> paramValues)
      {
        boolean valid = true;
        for (String key : paramValues.keySet())
        {
          String s = (String)paramValues.get(key);
          if ((s == null) || (s.equals(""))) {
            valid = false;
          }
        }
        if (valid)
        {
          String url = URLBuilder.getInstance().getReportURL(reportRef.getReportFileName());
          RenderDispatchEvent eventPayload = new RenderDispatchEvent(reportRef.getTitle(), url);
          eventPayload.setParameters(ReportLaunchPadView.fieldValues2PostParams(paramValues));
          ReportLaunchPadView.this.controller.handleEvent(new Event(RenderReportAction.ID, eventPayload));
        }
        else
        {
          MessageBox.alert("Report Parameters", "Please provide the required input parameters");
        }
      }
    });
    return form;
  }

  private ReportReference getCurrentSelection()
  {
    String template = this.dropBox.getItemText(this.dropBox.getSelectedIndex());
    for (ReportReference r : this.reportTemplates)
    {
      if (r.getTitle().equals(template)) {
        return r;
      }
    }
    return null;
  }

  public void update(List<ReportReference> reports)
  {
    this.reportTemplates = reports;
    this.forms.clear();

    for (ReportReference report : reports)
    {
      this.dropBox.addItem(report.getTitle());

      this.forms.put(report.getTitle(), createInput(report));
    }

    this.dropBox.setSelectedIndex(0);

    selectForm(this.dropBox.getItemText(0));

    ((ApplicationContext)Registry.get(ApplicationContext.class)).refreshView();
  }

  private void selectForm(String reportTitle)
  {
    this.inputPanel.clear();
    ReportReference current = getCurrentSelection();
    this.description.setText(current.getDescription());
    this.inputPanel.add((Widget)this.forms.get(reportTitle));
    layout();
  }

  public void displayReport(String title, String dispatchUrl)
  {
    this.reportFrame.setFrameUrl(dispatchUrl);
  }

  public void reset()
  {
    String url = GWT.getModuleBaseURL() + "blank.html";
    System.out.println("** Blank URL: " + url);
    displayReport("", url);
  }

  public void update(Object[] data)
  {
    String title = (String)data[0];
    String url = (String)data[1];
    displayReport(title, url);
  }

  public void setLoading(boolean isLoading)
  {
    LoadingOverlay.on(this.reportFrame, isLoading);
  }

  private static String fieldValues2PostParams(Map<String, String> values)
  {
    StringBuffer sb = new StringBuffer();
    Iterator<String> keys = values.keySet().iterator();
    while (keys.hasNext())
    {
      String key = keys.next();
      sb.append(key).append("=").append((String)values.get(key));
      sb.append(";");
    }

    System.out.println(sb.toString());
    return sb.toString();
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }
}