package org.jboss.bpm.console.client.task;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.model.DTOParser;
import org.jboss.bpm.console.client.model.TaskRef;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class LoadTasksAction extends AbstractRESTAction
{
  public static final String ID = LoadTasksAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    String identity = (String)event;
    return URLBuilder.getInstance().getTaskListURL(identity);
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  protected DataDriven getDataDriven(Controller controller)
  {
    return (AssignedTasksView)controller.getView(AssignedTasksView.ID);
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    String identity = (String)event;

    List<TaskRef> tasks = DTOParser.parseTaskReferenceList(response.getText());
    AssignedTasksView view = (AssignedTasksView)controller.getView(AssignedTasksView.ID);

    view.update(new Object[] { identity, tasks });

    ConsoleLog.info("Loaded " + tasks.size() + " tasks");
  }
}