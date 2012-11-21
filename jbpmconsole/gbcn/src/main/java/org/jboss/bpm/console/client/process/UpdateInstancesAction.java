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
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class UpdateInstancesAction extends AbstractRESTAction
{
  public static final String ID = UpdateInstancesAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    ProcessDefinitionRef def = (ProcessDefinitionRef)event;
    return URLBuilder.getInstance().getProcessInstancesURL(def.getId());
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  protected DataDriven getDataDriven(Controller controller)
  {
    return (InstanceListView)controller.getView(InstanceListView.ID);
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    long start = System.currentTimeMillis();

    ProcessDefinitionRef def = (ProcessDefinitionRef)event;
    List<ProcessInstanceRef> instances = JSOParser.parseProcessInstances(response.getText());
    InstanceListView view = (InstanceListView)controller.getView(InstanceListView.ID);
    if (view != null) view.update(new Object[] { def, instances });

    ConsoleLog.info("Loaded " + instances.size() + " process instance(s) in " + (System.currentTimeMillis() - start) + " ms");
  }
}