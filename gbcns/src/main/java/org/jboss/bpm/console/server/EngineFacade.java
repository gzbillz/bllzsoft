package org.jboss.bpm.console.server;

import com.google.gson.Gson;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.bpm.console.client.model.DeploymentRef;
import org.jboss.bpm.console.client.model.DeploymentRefWrapper;
import org.jboss.bpm.console.client.model.JobRef;
import org.jboss.bpm.console.client.model.JobRefWrapper;
import org.jboss.bpm.console.server.gson.GsonFactory;
import org.jboss.bpm.console.server.plugin.PluginMgr;
import org.jboss.bpm.console.server.plugin.ProcessEnginePlugin;
import org.jboss.bpm.console.server.util.RsComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("engine")
@RsComment(title="Process Engine", description="Process runtime state", example = "", project = { })
public class EngineFacade
{
  private static final Logger log = LoggerFactory.getLogger(EngineFacade.class);
  private ProcessEnginePlugin processEnginePlugin;

  private ProcessEnginePlugin getDeploymentPlugin()
  {
    if (null == this.processEnginePlugin)
    {
      this.processEnginePlugin = ((ProcessEnginePlugin)PluginMgr.load(ProcessEnginePlugin.class));
    }

    return this.processEnginePlugin;
  }

  @GET
  @Path("deployments")
  @Produces({"application/json"})
  public Response getDeployments() {
    ProcessEnginePlugin dplPlugin = getDeploymentPlugin();
    if (this.processEnginePlugin != null)
    {
      List<DeploymentRef>  dpls = dplPlugin.getDeployments();
      return createJsonResponse(new DeploymentRefWrapper(dpls));
    }

    log.error("ProcessEnginePlugin not available");
    return Response.serverError().build();
  }

  @POST
  @Path("deployment/{id}/suspend")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response suspendDeployment(@PathParam("id") String id)
  {
    return doSuspend(id, true);
  }

  @POST
  @Path("deployment/{id}/resume")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response resumeDeployment(@PathParam("id") String id)
  {
    return doSuspend(id, false);
  }

  private Response doSuspend(String id, boolean suspended)
  {
    @SuppressWarnings("unused")
	ProcessEnginePlugin dplPlugin = getDeploymentPlugin();
    if (this.processEnginePlugin != null)
    {
      this.processEnginePlugin.suspendDeployment(id, suspended);
      return Response.ok().build();
    }

    log.error("ProcessEnginePlugin not available");
    return Response.serverError().build();
  }

  @POST
  @Path("deployment/{id}/delete")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response deleteDeployment(@PathParam("id") String id)
  {
    @SuppressWarnings("unused")
	ProcessEnginePlugin dplPlugin = getDeploymentPlugin();
    if (this.processEnginePlugin != null)
    {
      this.processEnginePlugin.deleteDeployment(id);
      return Response.ok().build();
    }

    log.error("ProcessEnginePlugin not available");
    return Response.serverError().build();
  }

  @GET
  @Path("jobs")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response getJobs()
  {
    ProcessEnginePlugin dplPlugin = getDeploymentPlugin();
    if (this.processEnginePlugin != null)
    {
      List<JobRef> jobs = dplPlugin.getJobs();
      return createJsonResponse(new JobRefWrapper(jobs));
    }

    log.error("ProcessEnginePlugin not available");
    return Response.serverError().build();
  }

  @POST
  @Path("job/{id}/execute")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response executeJob(@PathParam("id") String id)
  {
    ProcessEnginePlugin dplPlugin = getDeploymentPlugin();
    if (this.processEnginePlugin != null)
    {
      dplPlugin.executeJob(id);
      return Response.ok().build();
    }

    log.error("ProcessEnginePlugin not available");
    return Response.serverError().build();
  }

  private Response createJsonResponse(Object wrapper)
  {
    Gson gson = GsonFactory.createInstance();
    String json = gson.toJson(wrapper);
    return Response.ok(json).type("application/json").build();
  }
}