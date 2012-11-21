package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;

public class StateChangeAction extends AbstractRESTAction
{
  public static final String ID = StateChangeAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    ProcessInstanceRef inst = (ProcessInstanceRef)event;
    if (ProcessInstanceRef.STATE.ENDED == inst.getState()) {
      return URLBuilder.getInstance().getInstanceEndURL(inst.getId(), inst.getEndResult());
    }
    return URLBuilder.getInstance().getStateChangeURL(inst.getId(), inst.getState());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.POST;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    InstanceListView view = (InstanceListView)controller.getView(InstanceListView.ID);
    ProcessDefinitionRef def = view.getCurrentDefinition();

    controller.handleEvent(new Event(UpdateInstancesAction.ID, def));
  }
}