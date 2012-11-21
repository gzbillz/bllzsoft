package org.jboss.bpm.console.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.jboss.bpm.console.server.integration.ManagementFactory;
import org.jboss.bpm.console.server.integration.TaskManagement;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
import org.jboss.bpm.console.server.plugin.PluginMgr;
import org.jboss.bpm.console.server.util.RsComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("task")
@RsComment(title="Task Management", description="Manage task instances", project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, example = "")
public class TaskMgmtFacade
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

  @SuppressWarnings("unused")
private FormDispatcherPlugin getFormDispatcherPlugin()
  {
    if (null == this.formPlugin)
    {
      this.formPlugin = ((FormDispatcherPlugin)PluginMgr.load(FormDispatcherPlugin.class));
      log.debug("Using FormDispatcherPlugin impl:" + this.formPlugin);
    }

    return this.formPlugin;
  }

  @POST
  @Path("{taskId}/assign/{ifRef}")
  @Produces({"application/json"})
  public Response assignTask(@Context HttpServletRequest request, @PathParam("taskId") long taskId, @PathParam("ifRef") String idRef)
  {
    log.debug("Assign task " + taskId + " to '" + idRef + "'");
    getTaskManagement().assignTask(taskId, idRef, request.getUserPrincipal().getName());
    return Response.ok().build();
  }

  @POST
  @Path("{taskId}/release")
  @Produces({"application/json"})
  public Response releaseTask(@Context HttpServletRequest request, @PathParam("taskId") long taskId)
  {
    log.debug("Release task " + taskId);
    getTaskManagement().assignTask(taskId, null, request.getUserPrincipal().getName());
    return Response.ok().build();
  }

  @POST
  @Path("{taskId}/close")
  @Produces({"application/json"})
  public Response closeTask(@Context HttpServletRequest request, @PathParam("taskId") long taskId)
  {
    log.debug("Close task " + taskId);
    getTaskManagement().completeTask(taskId, null, request.getUserPrincipal().getName());
    return Response.ok().build();
  }

  @POST
  @Path("{taskId}/close/{outcome}")
  @Produces({"application/json"})
  public Response closeTaskWithSignal(@Context HttpServletRequest request, @PathParam("taskId") long taskId, @PathParam("outcome") String outcome)
  {
    log.debug("Close task " + taskId + " outcome " + outcome);
    getTaskManagement().completeTask(taskId, outcome, null, request.getUserPrincipal().getName());
    return Response.ok().build();
  }
}