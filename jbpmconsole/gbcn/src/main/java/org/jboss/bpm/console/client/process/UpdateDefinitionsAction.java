package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.model.JSOParser;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.process.v2.Explorer;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.framework.Registry;

public class UpdateDefinitionsAction extends AbstractRESTAction
{
  public static final String ID = UpdateDefinitionsAction.class.getName();
  private ApplicationContext appContext;

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
    return (DefinitionListView)controller.getView(DefinitionListView.ID);
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
    boolean isjBPMInstance = this.appContext.getConfig().getProfileName().equals("jBPM Console");
    long start = System.currentTimeMillis();
    if (isjBPMInstance) {
      DefinitionListView view = (DefinitionListView)controller.getView(DefinitionListView.ID);
      if (view != null)
      {
        List<ProcessDefinitionRef> definitions = JSOParser.parseProcessDefinitions(response.getText());

        view.update(new Object[] { definitions });
        ConsoleLog.info("Loaded " + definitions.size() + " process definitions in " + (System.currentTimeMillis() - start) + " ms");
      }
    } else {
      Explorer view = (Explorer)controller.getView(Explorer.class.getName());
      if (view != null)
      {
        List<ProcessDefinitionRef> definitions = JSOParser.parseProcessDefinitions(response.getText());

        view.update(new Object[] { definitions });
        ConsoleLog.info("Loaded " + definitions.size() + " process definitions in " + (System.currentTimeMillis() - start) + " ms");
      }
    }
  }
}