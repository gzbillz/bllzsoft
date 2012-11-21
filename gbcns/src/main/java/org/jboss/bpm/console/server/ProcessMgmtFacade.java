package org.jboss.bpm.console.server;

import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jboss.bpm.console.client.model.ActiveNodeInfo;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessDefinitionRefWrapper;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRefWrapper;
import org.jboss.bpm.console.server.gson.GsonFactory;
import org.jboss.bpm.console.server.integration.ManagementFactory;
import org.jboss.bpm.console.server.integration.ProcessManagement;
import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
import org.jboss.bpm.console.server.plugin.GraphViewerPlugin;
import org.jboss.bpm.console.server.plugin.PluginMgr;
import org.jboss.bpm.console.server.plugin.ProcessActivityPlugin;
import org.jboss.bpm.console.server.util.Payload2XML;
import org.jboss.bpm.console.server.util.RsComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("process")
@RsComment(title="Process Management", description="Process related data.", example = "", project = { })
public class ProcessMgmtFacade
{
  private static final Logger log = LoggerFactory.getLogger(ProcessMgmtFacade.class);
  private ProcessManagement processManagement;
  private GraphViewerPlugin graphViewerPlugin;
  private ProcessActivityPlugin activityPlugin;
  private FormDispatcherPlugin formPlugin;

  private FormDispatcherPlugin getFormDispatcherPlugin()
  {
    if (null == this.formPlugin)
    {
      this.formPlugin = ((FormDispatcherPlugin)PluginMgr.load(FormDispatcherPlugin.class));
    }

    return this.formPlugin;
  }

  private ProcessManagement getProcessManagement()
  {
    if (null == this.processManagement)
    {
      ManagementFactory factory = ManagementFactory.newInstance();
      this.processManagement = factory.createProcessManagement();
      log.debug("Using ManagementFactory impl:" + factory.getClass().getName());
    }

    return this.processManagement;
  }

  private GraphViewerPlugin getGraphViewerPlugin()
  {
    if (this.graphViewerPlugin == null)
    {
      this.graphViewerPlugin = ((GraphViewerPlugin)PluginMgr.load(GraphViewerPlugin.class));
    }

    return this.graphViewerPlugin;
  }

  private ProcessActivityPlugin getActivityPlugin()
  {
    if (this.activityPlugin == null)
    {
      this.activityPlugin = ((ProcessActivityPlugin)PluginMgr.load(ProcessActivityPlugin.class));
    }

    return this.activityPlugin;
  }
  @GET
  @Path("definitions")
  @Produces({"application/json"})
  public Response getDefinitionsJSON() {
    List<ProcessDefinitionRef> rocessDefinitions = getProcessManagement().getProcessDefinitions();
    return decorateProcessDefintions(rocessDefinitions);
  }

  private Response decorateProcessDefintions(List<ProcessDefinitionRef> processDefinitions)
  {
    FormDispatcherPlugin formPlugin = getFormDispatcherPlugin();
    if (formPlugin != null)
    {
      for (ProcessDefinitionRef def : processDefinitions)
      {
        URL processFormURL = formPlugin.getDispatchUrl(new FormAuthorityRef(def.getId(), FormAuthorityRef.Type.PROCESS));

        if (processFormURL != null)
        {
          def.setFormUrl(processFormURL.toExternalForm());
        }
      }

    }

    GraphViewerPlugin graphViewer = getGraphViewerPlugin();
    if (graphViewer != null)
    {
      for (ProcessDefinitionRef def : processDefinitions)
      {
        URL diagramUrl = graphViewer.getDiagramURL(def.getId());
        if (diagramUrl != null)
        {
          def.setDiagramUrl(diagramUrl.toExternalForm());
        }
      }
    }

    ProcessDefinitionRefWrapper wrapper = new ProcessDefinitionRefWrapper(processDefinitions);

    return createJsonResponse(wrapper);
  }

  @POST
  @Path("definition/{id}/remove")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response removeDefinitionsJSON(@PathParam("id") String definitionId)
  {
    ProcessDefinitionRefWrapper wrapper = new ProcessDefinitionRefWrapper(getProcessManagement().removeProcessDefinition(definitionId));

    return createJsonResponse(wrapper);
  }

  @GET
  @Path("definition/{id}/instances")
  @Produces({"application/json"})
  public Response getInstancesJSON(@PathParam("id") String definitionId)
  {
    ProcessInstanceRefWrapper wrapper = new ProcessInstanceRefWrapper(getProcessManagement().getProcessInstances(definitionId));

    return createJsonResponse(wrapper);
  }

  @POST
  @Path("definition/{id}/new_instance")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response newInstance(@PathParam("id") String definitionId)
  {
    ProcessInstanceRef instance = null;
    try
    {
      instance = getProcessManagement().newInstance(definitionId);
      return createJsonResponse(instance);
    }
    catch (Throwable t)
    {
      throw new WebApplicationException(t, 500);
    }
  }

  @GET
  @Path("instance/{id}/dataset")
  @Produces({"text/xml"})
  public Response getInstanceData(@PathParam("id") String instanceId)
  {
    try
    {
      Map<String, Object> javaPayload = getProcessManagement().getInstanceData(instanceId);
      Payload2XML payload2XML = new Payload2XML();
      StringBuffer sb = payload2XML.convert(instanceId, javaPayload);
      return Response.ok(sb.toString()).build();
    } catch (Exception e) {
      e.printStackTrace();
      Response.ResponseBuilder builder = Response.fromResponse(Response.ok(e.getMessage()).build());
      builder.status(Response.Status.INTERNAL_SERVER_ERROR);

      return builder.build();
    }
  }

  @POST
  @Path("instance/{id}/state/{next}")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response changeState(@PathParam("id") String executionId, @PathParam("next") String next)
  {
    ProcessInstanceRef.STATE state = ProcessInstanceRef.STATE.valueOf(next);
    log.debug("Change instance (ID " + executionId + ") to state " + state);
    getProcessManagement().setProcessState(executionId, state);
    return Response.ok().type("application/json").build();
  }

  @POST
  @Path("instance/{id}/end/{result}")
  @Produces({"application/json"})
  public Response endInstance(@PathParam("id") String executionId, @PathParam("result") String resultValue)
  {
    ProcessInstanceRef.RESULT result = ProcessInstanceRef.RESULT.valueOf(resultValue);
    log.debug("Change instance (ID " + executionId + ") to state " + ProcessInstanceRef.STATE.ENDED);
    getProcessManagement().endInstance(executionId, result);
    return Response.ok().type("application/json").build();
  }

  @POST
  @Path("instance/{id}/delete")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response deleteInstance(@PathParam("id") String executionId)
  {
    log.debug("Delete instance (ID " + executionId + ")");
    getProcessManagement().deleteInstance(executionId);
    return Response.ok().type("application/json").build();
  }

  @POST
  @Path("tokens/{id}/transition")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response signalExecution(@PathParam("id") String id, @QueryParam("signal") String signalName)
  {
    try
    {
      id = URLDecoder.decode(id, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    log.debug("Signal token " + id + " -> " + signalName);

    if ("default transition".equals(signalName)) {
      signalName = null;
    }
    getProcessManagement().signalExecution(id, signalName);
    return Response.ok().type("application/json").build();
  }

  @POST
  @Path("tokens/{id}/transition/default")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response signalExecutionDefault(@PathParam("id") String id) {
    try {
      id = URLDecoder.decode(id, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    log.debug("Signal token " + id);

    getProcessManagement().signalExecution(id, null);
    return Response.ok().type("application/json").build();
  }

  @GET
  @Path("definition/{id}/image")
  @Produces({"image/*"})
  public Response getProcessImage(@Context HttpServletRequest request, @PathParam("id") String id)
  {
    GraphViewerPlugin plugin = getGraphViewerPlugin();
    if (plugin != null)
    {
      byte[] processImage = plugin.getProcessImage(id);
      if (processImage != null) {
        return Response.ok(processImage).type("image/png").build();
      }
      return Response.status(404).build();
    }

    throw new RuntimeException(GraphViewerPlugin.class.getName() + " not available.");
  }

  @GET
  @Path("definition/{id}/image/{instance}")
  @Produces({"image/*"})
  public Response getProcessInstanceImage(@Context HttpServletRequest request, @PathParam("id") String id, @PathParam("instance") String instance)
  {
    ProcessActivityPlugin plugin = getActivityPlugin();
    if (plugin != null)
    {
      byte[] processImage = plugin.getProcessInstanceImage(id, instance);
      if (processImage != null) {
        return Response.ok(processImage).type("image/png").build();
      }
      return Response.status(404).build();
    }

    throw new RuntimeException(ProcessActivityPlugin.class.getName() + " not available.");
  }

  @GET
  @Path("instance/{id}/activeNodeInfo")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response getActiveNodeInfo(@PathParam("id") String id)
  {
    GraphViewerPlugin plugin = getGraphViewerPlugin();
    if (plugin != null)
    {
      List<ActiveNodeInfo>  info = plugin.getActiveNodeInfo(id);
      return createJsonResponse(info);
    }

    throw new RuntimeException(GraphViewerPlugin.class.getName() + " not available.");
  }

  @GET
  @Path("definition/history/{id}/nodeInfo")
  @Produces({"application/json"})
  @RsComment(project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, description = "", example = "", title = "")
  public Response getNodeInfoForActivities(@PathParam("id") String id, @QueryParam("activity") String[] activities)
  {
    GraphViewerPlugin plugin = getGraphViewerPlugin();
    if (plugin != null)
    {
      List<ActiveNodeInfo>  info = plugin.getNodeInfoForActivities(id, Arrays.asList(activities));
      return createJsonResponse(info);
    }

    throw new RuntimeException(GraphViewerPlugin.class.getName() + " not available.");
  }

  private Response createJsonResponse(Object wrapper)
  {
    Gson gson = GsonFactory.createInstance();
    String json = gson.toJson(wrapper);
    return Response.ok(json).type("application/json").build();
  }
}