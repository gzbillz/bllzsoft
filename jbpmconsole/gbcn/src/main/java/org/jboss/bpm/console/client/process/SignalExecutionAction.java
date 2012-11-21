package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.process.events.SignalInstanceEvent;

class SignalExecutionAction extends AbstractRESTAction
{
  public static final String ID = SignalExecutionAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    SignalInstanceEvent def = (SignalInstanceEvent)event;
    String data = def.getToken().getName();

    String eventData = def.getSignalName();
    if ((eventData != null) && (!eventData.trim().equalsIgnoreCase(""))) {
      data = data + "^" + eventData;
    }

    return URLBuilder.getInstance().getExecutionSignalUrl(def.getToken(), data);
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.POST;
  }

  public void handleSuccessfulResponse(final Controller controller, Object event, Response response)
  {
    final SignalInstanceEvent def = (SignalInstanceEvent)event;
    InstanceListView view = (InstanceListView)controller.getView(InstanceListView.ID);
    if (view != null) view.renderSignalListBox(def.getIndex());

    Timer t = new Timer()
    {
      public void run()
      {
        controller.handleEvent(new Event(UpdateInstancesAction.ID, def.getDefinition()));
      }
    };
    t.schedule(500);
  }
}