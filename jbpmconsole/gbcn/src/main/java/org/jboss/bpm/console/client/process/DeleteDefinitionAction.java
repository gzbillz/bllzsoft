package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;

public class DeleteDefinitionAction extends AbstractRESTAction
{
  public static final String ID = DeleteDefinitionAction.class.getName();

  public String getId()
  {
    return ID;
  }

  @SuppressWarnings("deprecation")
public String getUrl(Object event)
  {
    ProcessDefinitionRef procRef = (ProcessDefinitionRef)event;
    return URLBuilder.getInstance().getRemoveDefinitionURL(procRef.getId());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.POST;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    controller.handleEvent(new Event(UpdateDefinitionsAction.ID, null));
  }
}