package org.jboss.bpm.console.client.monitor;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.JSOParser;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class GetProcessDefinitionsAction extends AbstractRESTAction
{
  public static final String ID = GetProcessDefinitionsAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    return URLBuilder.getInstance().getProcessHistoryDefinitionsURL();
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    ExecutionHistoryView view = (ExecutionHistoryView)controller.getView(ExecutionHistoryView.ID);
    List<ProcessDefinitionRef> refs = JSOParser.parseProcessDefinitions(response.getText());
    view.selectDefinition(refs);
    ConsoleLog.debug("loaded " + refs.size() + " historic process definitions : " + response.getText());
  }
}