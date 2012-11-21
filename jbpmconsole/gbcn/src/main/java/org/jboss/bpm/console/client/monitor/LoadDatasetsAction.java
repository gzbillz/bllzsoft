package org.jboss.bpm.console.client.monitor;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class LoadDatasetsAction extends AbstractRESTAction
{
  public static final String ID = LoadDatasetsAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    LoadDatasetEvent theEvent = (LoadDatasetEvent)event;

    if (theEvent.isIncludedFailed()) {
      return URLBuilder.getInstance().getProcessHistoryFailedInstances4ChartURL(theEvent.getDefinitionId(), theEvent.getTimespan().getCanonicalName());
    }

    return URLBuilder.getInstance().getProcessHistoryCompletedInstances4ChartURL(theEvent.getDefinitionId(), theEvent.getTimespan().getCanonicalName());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    ExecutionHistoryView view = (ExecutionHistoryView)controller.getView(ExecutionHistoryView.ID);
    view.updateChart(response.getText());

    ConsoleLog.debug("Loaded chart data : " + response.getText());
  }
}