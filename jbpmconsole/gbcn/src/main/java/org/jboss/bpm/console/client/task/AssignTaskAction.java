package org.jboss.bpm.console.client.task;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.task.events.AssignEvent;

public class AssignTaskAction extends AbstractRESTAction
{
  public static final String ID = AssignTaskAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    AssignEvent payload = (AssignEvent)event;
    return URLBuilder.getInstance().getTaskAssignURL(payload.getTask().getId(), payload.getIdRef());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.POST;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    controller.handleEvent(new Event(ReloadAllTaskListsAction.ID, null));
  }
}