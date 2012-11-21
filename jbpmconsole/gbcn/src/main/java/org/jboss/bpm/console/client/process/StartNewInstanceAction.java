package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;

public class StartNewInstanceAction extends AbstractRESTAction
{
  public static final String ID = StartNewInstanceAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    ProcessDefinitionRef def = (ProcessDefinitionRef)event;
    return URLBuilder.getInstance().getStartNewInstanceURL(def.getId());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.POST;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    ProcessDefinitionRef def = (ProcessDefinitionRef)event;

    controller.handleEvent(new Event(UpdateInstancesAction.ID, def));
  }
}