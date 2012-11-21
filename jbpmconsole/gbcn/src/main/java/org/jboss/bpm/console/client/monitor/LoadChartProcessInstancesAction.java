package org.jboss.bpm.console.client.monitor;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.JSOParser;
import org.jboss.bpm.console.client.model.StringRef;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class LoadChartProcessInstancesAction extends AbstractRESTAction
{
  public static final String ID = LoadChartProcessInstancesAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    LoadChartProcessInstanceEvent theEvent = (LoadChartProcessInstanceEvent)event;

    if (0 == theEvent.getDatasetType()) {
      return URLBuilder.getInstance().getProcessHistoryCompletedInstancesURL(theEvent.getDefinitionId(), String.valueOf(theEvent.getDate().getTime()), theEvent.getTimespan().getCanonicalName());
    }
    if (2 == theEvent.getDatasetType()) {
      return URLBuilder.getInstance().getProcessHistoryTerminatedInstanceURL(theEvent.getDefinitionId(), String.valueOf(theEvent.getDate().getTime()), theEvent.getTimespan().getCanonicalName());
    }
    if (1 == theEvent.getDatasetType()) {
      return URLBuilder.getInstance().getProcessHistoryFailedInstanceURL(theEvent.getDefinitionId(), String.valueOf(theEvent.getDate().getTime()), theEvent.getTimespan().getCanonicalName());
    }

    throw new RuntimeException("couldn't find an appropriate URL for the type of " + theEvent.getDatasetType());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    ExecutionHistoryView view = (ExecutionHistoryView)controller.getView(ExecutionHistoryView.ID);

    List<StringRef> data = JSOParser.parseStringRef(response.getText());

    view.updateProcessInstances(data);

    ConsoleLog.debug("loaded chart data process instances : " + response.getText());
  }
}