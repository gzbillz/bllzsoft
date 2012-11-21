package org.jboss.bpm.console.client.report;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import java.io.IOException;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.LoadingStatusAction;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.framework.Registry;

public class RenderReportAction
  implements ActionInterface
{
  public static final String ID = RenderReportAction.class.getName();
  private ApplicationContext appContext;

  public RenderReportAction()
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
  }

  public void execute(final Controller controller, Object object)
  {
    final RenderDispatchEvent event = (RenderDispatchEvent)object;

    final String url = event.getDispatchUrl();
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);

    ConsoleLog.debug(RequestBuilder.POST + ": " + url);
    final ReportLaunchPadView view = (ReportLaunchPadView)controller.getView(ReportLaunchPadView.ID);

    view.reset();
    view.setLoading(true);
    try
    {
      controller.handleEvent(LoadingStatusAction.ON);

      String parameters = event.getParameters();
      final Request request = builder.sendRequest(parameters, new RequestCallback()
      {
        public void onError(Request request, Throwable exception)
        {
          RenderReportAction.this.handleError(controller, url, exception);
          controller.handleEvent(LoadingStatusAction.OFF);
        }

        public void onResponseReceived(Request request, Response response)
        {
          try {
            if (response.getText().indexOf("HTTP 401") != -1)
            {
              RenderReportAction.this.appContext.getAuthentication().handleSessionTimeout();
            }
            else if (200 == response.getStatusCode())
            {
              view.displayReport(event.getTitle(), event.getDispatchUrl());
            }
            else
            {
              String msg = response.getText().equals("") ? "Unknown error" : response.getText();
              RenderReportAction.this.handleError(controller, url, new RequestException("HTTP " + response.getStatusCode() + ": " + msg));
            }

          }
          finally
          {
            controller.handleEvent(LoadingStatusAction.OFF);
            view.setLoading(false);
          }
        }
      });
      Timer t = new Timer()
      {
        public void run()
        {
          if (request.isPending())
          {
            request.cancel();
            RenderReportAction.this.handleError(controller, url, new IOException("Request timeout"));
          }
        }
      };
      t.schedule(20000);
    }
    catch (Throwable e)
    {
      controller.handleEvent(LoadingStatusAction.OFF);
      handleError(controller, url, e);
      view.setLoading(false);
    }
  }

  protected void handleError(Controller controller, String url, Throwable t)
  {
    String out = "<ul><li>URL: '" + url + "'\n" + "<li>Action: '" + ID + "'\n" + "<li>Exception: '" + t.getClass() + "'" + "</ul>\n\n" + t.getMessage();

    ConsoleLog.error(out, t);
    this.appContext.displayMessage(out, true);

    controller.handleEvent(LoadingStatusAction.OFF);
  }
}