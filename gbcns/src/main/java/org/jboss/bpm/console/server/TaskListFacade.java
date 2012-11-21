package org.jboss.bpm.console.server;

import com.google.gson.Gson;
import java.net.URL;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.jboss.bpm.console.client.model.TaskRef;
import org.jboss.bpm.console.client.model.TaskRefWrapper;
import org.jboss.bpm.console.server.gson.GsonFactory;
import org.jboss.bpm.console.server.integration.ManagementFactory;
import org.jboss.bpm.console.server.integration.TaskManagement;
import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
import org.jboss.bpm.console.server.plugin.PluginMgr;
import org.jboss.bpm.console.server.util.RsComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("tasks")
@RsComment(title="Task Lists", description="Access task lists", project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, example = "")
public class TaskListFacade
{
  private static final Logger log = LoggerFactory.getLogger(TaskMgmtFacade.class);
  private TaskManagement taskManagement;
  private FormDispatcherPlugin formPlugin;

  private TaskManagement getTaskManagement()
  {
    if (null == this.taskManagement)
    {
      ManagementFactory factory = ManagementFactory.newInstance();
      this.taskManagement = factory.createTaskManagement();
      log.debug("Using ManagementFactory impl:" + factory.getClass().getName());
    }

    return this.taskManagement;
  }

  private FormDispatcherPlugin getFormDispatcherPlugin()
  {
    if (null == this.formPlugin)
    {
      this.formPlugin = ((FormDispatcherPlugin)PluginMgr.load(FormDispatcherPlugin.class));
    }

    return this.formPlugin;
  }

  @GET
  @Path("{idRef}")
  @Produces({"application/json"})
  public Response getTasksForIdRef(@PathParam("idRef") String idRef)
  {
    List<TaskRef> assignedTasks = getTaskManagement().getAssignedTasks(idRef);
    return processTaskListResponse(assignedTasks);
  }

  @GET
  @Path("{idRef}/participation")
  @Produces({"application/json"})
  public Response getTasksForIdRefParticipation(@PathParam("idRef") String idRef)
  {
    List<TaskRef> taskParticipation = getTaskManagement().getUnassignedTasks(idRef, null);
    return processTaskListResponse(taskParticipation);
  }

  private Response processTaskListResponse(List<TaskRef> taskList)
  {
    FormDispatcherPlugin formPlugin = getFormDispatcherPlugin();
    if (formPlugin != null)
    {
      for (TaskRef task : taskList)
      {
        URL taskFormURL = formPlugin.getDispatchUrl(new FormAuthorityRef(String.valueOf(task.getId())));

        if (taskFormURL != null)
        {
          task.setUrl(taskFormURL.toExternalForm());
        }
      }
    }

    TaskRefWrapper wrapper = new TaskRefWrapper(taskList);
    return createJsonResponse(wrapper);
  }

  private Response createJsonResponse(Object wrapper)
  {
    Gson gson = GsonFactory.createInstance();
    String json = gson.toJson(wrapper);
    return Response.ok(json).type("application/json").build();
  }
}