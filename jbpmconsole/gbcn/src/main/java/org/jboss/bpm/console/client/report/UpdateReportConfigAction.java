package org.jboss.bpm.console.client.report;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.JSOParser;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.bpm.report.model.ReportReference;
import org.jboss.errai.workspaces.client.framework.Registry;

public class UpdateReportConfigAction extends AbstractRESTAction
{
  public static final String ID = UpdateReportConfigAction.class.getName();
  private ApplicationContext appContext;

  public UpdateReportConfigAction()
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
  }

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    return this.appContext.getConfig().getConsoleServerUrl() + "/rs/report/config";
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    String json = response.getText();
    List<ReportReference> reports = JSOParser.parseReportConfig(json);
    ReportView view = (ReportView)controller.getView(ReportView.ID);
    view.configure(reports);
  }

  public void handleError(String url, Throwable t)
  {
//    String out = "Reporting Engine does not seem to be running. Please make sure it is running before creating reports. Consult the jBPM Installer chapter in the documentation to learn how to set up the Reporting Engine.";

    ConsoleLog.warn("Reporting Engine does not seem to be running. Please make sure it is running before creating reports. Consult the jBPM Installer chapter in the documentation to learn how to set up the Reporting Engine.");
    this.appContext.displayMessage("Reporting Engine does not seem to be running. Please make sure it is running before creating reports. Consult the jBPM Installer chapter in the documentation to learn how to set up the Reporting Engine.", false);
  }
}