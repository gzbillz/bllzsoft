package org.jboss.bpm.console.client.report;

import com.google.gwt.user.client.Timer;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import org.gwt.mosaic.ui.client.layout.FillLayout;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.common.WidgetWindowPanel;
import org.jboss.bpm.console.client.search.UpdateSearchDefinitionsAction;
import org.jboss.bpm.report.model.ReportReference;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

public class ReportView
  implements ViewInterface, WidgetProvider
{
  public static final String ID = ReportView.class.getName();
  private Controller controller;
  private boolean isInitialized;
  private ReportLaunchPadView coverpanel;
  private MosaicPanel panel;

  public void provideWidget(ProvisioningCallback callback)
  {
    this.panel = new MosaicPanel(new FillLayout());
    this.panel.setPadding(0);
    this.controller = ((Controller)Registry.get(Controller.class));

    initialize();

    this.controller.addView(ID, this);
    this.controller.addAction(UpdateReportConfigAction.ID, new UpdateReportConfigAction());

    Timer t = new Timer()
    {
      public void run()
      {
        ReportView.this.controller.handleEvent(new Event(UpdateReportConfigAction.ID, null));
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
      this.coverpanel = new ReportLaunchPadView();
      this.panel.add(this.coverpanel);

      this.controller.addView(ReportLaunchPadView.ID, this.coverpanel);

      this.controller.addAction(UpdateSearchDefinitionsAction.ID, new UpdateSearchDefinitionsAction());
      this.controller.addAction(RenderReportAction.ID, new RenderReportAction());

      this.isInitialized = true;
    }
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void configure(List<ReportReference> reports)
  {
    this.coverpanel.update(reports);
  }

  public void displayReport(String title, String dispatchUrl)
  {
    ReportFrame reportFrame = new ReportFrame();
    reportFrame.setFrameUrl(dispatchUrl);
    new WidgetWindowPanel(title, reportFrame, true);
  }
}