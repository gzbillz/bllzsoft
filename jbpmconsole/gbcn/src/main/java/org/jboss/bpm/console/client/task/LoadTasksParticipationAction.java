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

public class LoadTasksParticipationAction extends AbstractRESTAction
{
  public static final String ID = LoadTasksParticipationAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    String identity = (String)event;
    return URLBuilder.getInstance().getParticipationTaskListURL(identity);
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  protected DataDriven getDataDriven(Controller controller)
  {
    return (OpenTasksView)controller.getView(OpenTasksView.ID);
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    String identity = (String)event;

    List<TaskRef> tasks = DTOParser.parseTaskReferenceList(response.getText());
    OpenTasksView view = (OpenTasksView)controller.getView(OpenTasksView.ID);

    ConsoleLog.info("Loaded " + tasks.size() + " tasks");
    view.update(new Object[] { identity, tasks });
  }
}