package org.jboss.bpm.console.client.task;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.task.events.TaskIdentityEvent;

public class ReleaseTaskAction extends AbstractRESTAction
{
  public static final String ID = ReleaseTaskAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    TaskIdentityEvent tie = (TaskIdentityEvent)event;
    return URLBuilder.getInstance().getTaskReleaseURL(tie.getTask().getId());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.POST;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    String currentUser = this.appContext.getAuthentication().getUsername();
    controller.handleEvent(new Event(LoadTasksAction.ID, currentUser));
  }
}