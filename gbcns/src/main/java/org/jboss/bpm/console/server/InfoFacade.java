package org.jboss.bpm.console.server;

import com.google.gson.Gson;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.jboss.bpm.console.client.model.PluginInfo;
import org.jboss.bpm.console.client.model.ServerStatus;
import org.jboss.bpm.console.server.gson.GsonFactory;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
import org.jboss.bpm.console.server.plugin.GraphViewerPlugin;
import org.jboss.bpm.console.server.plugin.PluginMgr;
import org.jboss.bpm.console.server.plugin.ProcessEnginePlugin;
import org.jboss.bpm.console.server.util.RsComment;
import org.jboss.bpm.console.server.util.RsDocBuilder;

@Path("server")
@RsComment(title="Server Info", description="General REST server information", example = "", project = { })
public class InfoFacade
{
  @SuppressWarnings("rawtypes")
private Class[] pluginInterfaces = { FormDispatcherPlugin.class, GraphViewerPlugin.class, ProcessEnginePlugin.class };

  private ServerStatus status = null;

  @GET
  @Path("status")
  @Produces({"application/json"})
  @RsComment(title="Plugins", description="Plugin availability", example = "", project = { })
  public Response getServerInfo()
  {
    ServerStatus status = getServerStatus();
    return createJsonResponse(status);
  }

  private ServerStatus getServerStatus()
  {
    if (null == this.status)
    {
      this.status = new ServerStatus();
      for (@SuppressWarnings("rawtypes") Class type : this.pluginInterfaces)
      {
        @SuppressWarnings("unchecked")
		Object impl = PluginMgr.load(type);
        boolean isAvailable = impl != null;

        this.status.getPlugins().add(new PluginInfo(type.getName(), isAvailable));
      }
    }
    return this.status;
  }

  @GET
  @Path("resources/{project}")
  @Produces({"text/html"})
  public Response getPublishedUrls(@Context HttpServletRequest request, @PathParam("project") String projectName)
  {
    @SuppressWarnings("rawtypes")
	Class[] rootResources = getRSResources();

    String rsServer = request.getContextPath();
    if ((request.getServletPath() != null) && (!"".equals(request.getServletPath()))) {
      rsServer = request.getContextPath() + request.getServletPath();
    }

    RsDocBuilder rsDocBuilder = new RsDocBuilder(rsServer, rootResources);
    StringBuffer sb = rsDocBuilder.build2HTML(projectName);
    return Response.ok(sb.toString()).build();
  }

  private Response createJsonResponse(Object wrapper)
  {
    Gson gson = GsonFactory.createInstance();
    String json = gson.toJson(wrapper);
    return Response.ok(json).type("application/json").build();
  }

  @SuppressWarnings("rawtypes")
public static Class[] getRSResources()
  {
    return new Class[] { InfoFacade.class, ProcessMgmtFacade.class, TaskListFacade.class, TaskMgmtFacade.class, UserMgmtFacade.class, EngineFacade.class, FormProcessingFacade.class, ProcessHistoryFacade.class };
  }
}