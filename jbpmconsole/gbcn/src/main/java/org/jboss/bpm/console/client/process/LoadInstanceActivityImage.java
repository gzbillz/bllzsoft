package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;

public class LoadInstanceActivityImage extends AbstractRESTAction
{
  public static final String ID = LoadActivityDiagramAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    ProcessInstanceRef inst = (ProcessInstanceRef)event;
    return URLBuilder.getInstance().getActivityImage(inst.getDefinitionId(), inst.getId());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    ProcessInstanceRef inst = (ProcessInstanceRef)event;

    ActivityDiagramView view = (ActivityDiagramView)controller.getView(ActivityDiagramView.ID);
    String url = URLBuilder.getInstance().getActivityImage(inst.getDefinitionId(), inst.getId());
    view.update(url);
  }
}