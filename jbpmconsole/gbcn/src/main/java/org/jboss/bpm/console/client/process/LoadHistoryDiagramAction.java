package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.ActiveNodeInfo;
import org.jboss.bpm.console.client.model.DTOParser;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.process.events.ActivityDiagramResultEvent;
import org.jboss.bpm.console.client.process.events.HistoryActivityDiagramEvent;

public class LoadHistoryDiagramAction extends AbstractRESTAction
{
  public static final String ID = LoadHistoryDiagramAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    ProcessDefinitionRef inst = ((HistoryActivityDiagramEvent)event).getDefinition();
    return URLBuilder.getInstance().getDefinitionHistoryNodeInfoURL(inst.getId(), ((HistoryActivityDiagramEvent)event).getExecutedActivities());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    ProcessDefinitionRef inst = ((HistoryActivityDiagramEvent)event).getDefinition();

    List<ActiveNodeInfo> activeNodeInfos = DTOParser.parseActiveNodeInfo(response.getText());

    ActivityDiagramView view = (ActivityDiagramView)controller.getView(ActivityDiagramView.ID);
    view.update(new ActivityDiagramResultEvent(URLBuilder.getInstance().getProcessImageURL(inst.getId()), activeNodeInfos));
  }
}