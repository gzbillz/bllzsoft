package org.jboss.bpm.console.client.history;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;
import org.jboss.bpm.console.client.model.JSOParser;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class LoadProcessHistoryAction extends AbstractRESTAction
{
  public static final String ID = LoadProcessHistoryAction.class.getName();

  public String getId()
  {
    return ID;
  }

  protected DataDriven getDataDriven(Controller controller)
  {
    return (ProcessHistoryInstanceListView)controller.getView(ProcessHistoryInstanceListView.ID);
  }

  public String getUrl(Object event)
  {
    ProcessSearchEvent searchEvent = (ProcessSearchEvent)event;
    StringBuffer sbuffer = new StringBuffer();
    sbuffer.append("status=");
    sbuffer.append(searchEvent.getStatus());
    sbuffer.append("&starttime=");
    sbuffer.append(searchEvent.getStartTime());
    sbuffer.append("&endtime=");
    sbuffer.append(searchEvent.getEndTime());
    if ((searchEvent.getKey() != null) && (!"".equals(searchEvent.getKey()))) {
      sbuffer.append("&correlationkey=");
      sbuffer.append(URL.encode(searchEvent.getKey().replace("=", "~")));
    }
    return URLBuilder.getInstance().getProcessHistoryURL(searchEvent.getDefinitionKey(), sbuffer.toString());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    ProcessHistoryInstanceListView view = (ProcessHistoryInstanceListView)controller.getView(ProcessHistoryInstanceListView.ID);
    List<HistoryProcessInstanceRef> ref = JSOParser.parseProcessDefinitionHistory(response.getText());
    view.update(new Object[] { ref });

    ConsoleLog.debug("Loaded " + ref.size() + " process instance(s) : " + response.getText());
  }
}