package org.jboss.bpm.console.client.search;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.JSOParser;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class UpdateSearchDefinitionsAction extends AbstractRESTAction
{
  public static final String ID = UpdateSearchDefinitionsAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    return URLBuilder.getInstance().getProcessDefinitionsURL();
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    String target = (String)event;

    List<ProcessDefinitionRef> definitions = JSOParser.parseProcessDefinitions(response.getText());
    SearchDefinitionView view = (SearchDefinitionView)controller.getView(target);
    view.update(definitions);

    ConsoleLog.info("Loaded " + definitions.size() + " process definitions");
  }
}