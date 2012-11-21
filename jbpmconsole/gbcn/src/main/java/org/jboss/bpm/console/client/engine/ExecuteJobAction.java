package org.jboss.bpm.console.client.engine;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class ExecuteJobAction extends AbstractRESTAction
{
  public static final String ID = ExecuteJobAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    String id = (String)event;
    return URLBuilder.getInstance().getExecuteJobURL(id);
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.POST;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    String id = (String)event;

    ConsoleLog.debug("Executed jod with id " + id + " and response from server is " + response.getText());

    controller.handleEvent(new Event(UpdateJobsAction.ID, null));
  }
}