package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.model.JSOParser;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class UpdateHistoryDefinitionsAction extends AbstractRESTAction
{
  public static final String ID = UpdateHistoryDefinitionsAction.class.getName();

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

  protected DataDriven getDataDriven(Controller controller)
  {
    return (DefinitionHistoryListView)controller.getView(DefinitionHistoryListView.ID);
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    long start = System.currentTimeMillis();

    DefinitionHistoryListView view = (DefinitionHistoryListView)controller.getView(DefinitionHistoryListView.ID);
    if (view != null)
    {
      List<ProcessDefinitionRef> definitions = JSOParser.parseProcessDefinitions(response.getText());

      view.update(new Object[] { definitions });
      ConsoleLog.info("Loaded " + definitions.size() + " process definitions in " + (System.currentTimeMillis() - start) + " ms");
    }
  }
}