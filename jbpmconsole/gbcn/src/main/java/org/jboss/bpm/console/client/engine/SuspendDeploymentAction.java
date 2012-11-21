package org.jboss.bpm.console.client.engine;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;

public class SuspendDeploymentAction extends AbstractRESTAction
{
  public static final String ID = SuspendDeploymentAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    String id = (String)event;
    return URLBuilder.getInstance().getSuspendDeploymentUrl(id);
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.POST;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    controller.handleEvent(new Event(UpdateDeploymentsAction.ID, null));
  }
}