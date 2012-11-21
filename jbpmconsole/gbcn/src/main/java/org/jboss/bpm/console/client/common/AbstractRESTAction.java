package org.jboss.bpm.console.client.common;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import java.io.IOException;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.LoadingStatusAction;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.framework.Registry;

public abstract class AbstractRESTAction
  implements ActionInterface
{
  protected ApplicationContext appContext;

  protected AbstractRESTAction()
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
  }
  public abstract String getId();

  public abstract String getUrl(Object paramObject);

  public abstract RequestBuilder.Method getRequestMethod();

  public abstract void handleSuccessfulResponse(Controller paramController, Object paramObject, Response paramResponse);

  public void execute(final Controller controller, final Object object) { final String url = getUrl(object);
    RequestBuilder builder = new RequestBuilder(getRequestMethod(), URL.encode(url));
    builder.setTimeoutMillis(0);

    ConsoleLog.debug(getRequestMethod() + ": " + url);
    try
    {
      if (getDataDriven(controller) != null)
      {
        getDataDriven(controller).setLoading(true);
      }

      final Request request = builder.sendRequest(null, new RequestCallback()
      {
        public void onError(Request request, Throwable exception)
        {
          AbstractRESTAction.this.handleError(url, exception);
          controller.handleEvent(LoadingStatusAction.OFF);
        }

        public void onResponseReceived(Request request, Response response)
        {
          try {
            if (response.getText().indexOf("HTTP 401") != -1)
            {
              AbstractRESTAction.this.appContext.getAuthentication().handleSessionTimeout();
            }
            else if (200 == response.getStatusCode())
            {
              AbstractRESTAction.this.handleSuccessfulResponse(controller, object, response);
            }
            else
            {
              String msg = response.getText().equals("") ? "Unknown error" : response.getText();
              AbstractRESTAction.this.handleError(url, new RequestException("HTTP " + response.getStatusCode() + ": " + msg));
            }

          }
          finally
          {
            if (AbstractRESTAction.this.getDataDriven(controller) != null)
            {
              AbstractRESTAction.this.getDataDriven(controller).setLoading(false);
            }
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
            AbstractRESTAction.this.handleError(url, new IOException("Request timeout"));
          }
        }
      };
      t.schedule(60000);
    }
    catch (RequestException e)
    {
      handleError(url, e);

      if (getDataDriven(controller) != null)
      {
        getDataDriven(controller).setLoading(false);
      }
    }
  }

  protected DataDriven getDataDriven(Controller controller)
  {
    return null;
  }

  protected void handleError(String url, Throwable t)
  {
    String out = "<ul><li>URL: '" + url + "'\n" + "<li>Action: '" + getId() + "'\n" + "<li>Exception: '" + t.getClass() + "'" + "</ul>\n\n" + t.getMessage();

    ConsoleLog.error(out, t);
    this.appContext.displayMessage(out, true);
  }
}