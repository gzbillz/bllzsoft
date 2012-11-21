package org.jboss.bpm.console.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.server.integration.ManagementFactory;
import org.jboss.bpm.console.server.integration.ProcessManagement;
import org.jboss.bpm.console.server.integration.TaskManagement;
import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
import org.jboss.bpm.console.server.plugin.PluginMgr;
import org.jboss.bpm.console.server.util.RsComment;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("form")
@RsComment(title="Form Processing", description="Web based form processing", project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, example = "")
public class FormProcessingFacade
{
  private static final Logger log = LoggerFactory.getLogger(FormProcessingFacade.class);
  private FormDispatcherPlugin formPlugin;
  private ProcessManagement processManagement;
  private TaskManagement taskManagement;
  @SuppressWarnings("unused")
private static final String SUCCESSFULLY_PROCESSED_INPUT = "<div style='font-family:sans-serif; padding:10px;'><h3>Successfully processed input</h3><p/>You can now close this window.</div>";

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

  private FormDispatcherPlugin getFormDispatcherPlugin()
  {
    if (null == this.formPlugin)
    {
      this.formPlugin = ((FormDispatcherPlugin)PluginMgr.load(FormDispatcherPlugin.class));
      log.debug("Using FormDispatcherPlugin impl:" + this.formPlugin);
    }

    return this.formPlugin;
  }

  @GET
  @Path("task/{id}/render")
  @Produces({"text/html"})
  public Response renderTaskUI(@PathParam("id") String taskId)
  {
    return provideForm(new FormAuthorityRef(taskId));
  }

  @GET
  @Path("process/{id}/render")
  @Produces({"text/html"})
  public Response renderProcessUI(@PathParam("id") String definitionId)
  {
    return provideForm(new FormAuthorityRef(definitionId, FormAuthorityRef.Type.PROCESS));
  }

  @POST
  @Path("task/{id}/complete")
  @Produces({"text/html"})
  @Consumes({"multipart/form-data"})
  public Response closeTaskWithUI(@Context HttpServletRequest request, @PathParam("id") String taskId, MultipartFormDataInput payload)
  {
    FieldMapping mapping = createFieldMapping(payload);

    String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;

    String outcomeDirective = (String)mapping.directives.get("outcome");

    if (outcomeDirective != null)
    {
      getTaskManagement().completeTask(Long.valueOf(taskId).longValue(), outcomeDirective, mapping.processVars, username);
    }
    else
    {
      getTaskManagement().completeTask(Long.valueOf(taskId).longValue(), mapping.processVars, username);
    }

    return Response.ok("<div style='font-family:sans-serif; padding:10px;'><h3>Successfully processed input</h3><p/>You can now close this window.</div>").build();
  }

  @POST
  @Path("process/{id}/complete")
  @Produces({"text/html"})
  @Consumes({"multipart/form-data"})
  public Response startProcessWithUI(@Context HttpServletRequest request, @PathParam("id") String definitionId, MultipartFormDataInput payload)
  {
    FieldMapping mapping = createFieldMapping(payload);

    @SuppressWarnings("unused")
	ProcessInstanceRef instance = getProcessManagement().newInstance(definitionId, mapping.processVars);

    return Response.ok("<div style='font-family:sans-serif; padding:10px;'><h3>Successfully processed input</h3><p/>You can now close this window.</div>").build();
  }

  private Response provideForm(FormAuthorityRef authorityRef)
  {
    DataHandler dh = getFormDispatcherPlugin().provideForm(authorityRef);

    if (null == dh)
    {
      throw new RuntimeException("No UI associated with " + authorityRef.getType() + " " + authorityRef.getReferenceId());
    }

    return Response.ok(dh.getDataSource()).type("text/html").build();
  }

  private FieldMapping createFieldMapping(MultipartFormDataInput payload)
  {
    FieldMapping mapping = new FieldMapping();

    @SuppressWarnings("deprecation")
	Map<String, InputPart> formData = payload.getFormData();
    Iterator<String> partNames = formData.keySet().iterator();

    while (partNames.hasNext())
    {
      final String partName = partNames.next();
      InputPart part = formData.get(partName);
      final MediaType mediaType = part.getMediaType();

      String mType = mediaType.getType();
      String mSubtype = mediaType.getSubtype();
      try
      {
        if (("text".equals(mType)) && ("plain".equals(mSubtype)))
        {
          if (mapping.isReserved(partName))
            mapping.directives.put(partName, part.getBodyAsString());
          else {
            mapping.processVars.put(partName, part.getBodyAsString());
          }
        }
        else
        {
          final byte[] data = part.getBodyAsString().getBytes();
          DataHandler dh = new DataHandler(new DataSource()
          {
            public InputStream getInputStream()
              throws IOException
            {
              return new ByteArrayInputStream(data);
            }

            public OutputStream getOutputStream() throws IOException
            {
              throw new RuntimeException("This is a readonly DataHandler");
            }

            public String getContentType()
            {
              return mediaType.getType();
            }

            public String getName()
            {
              return partName;
            }
          });
          mapping.processVars.put(partName, dh);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return mapping;
  }

  private class FieldMapping
  {
    final String[] reservedNames = { "outcome", "form" };

    Map<String, Object> processVars = new HashMap<String, Object>();
    Map<String, String> directives = new HashMap<String, String>();

    private FieldMapping() {
    }
    public boolean isReserved(String name) { boolean result = false;
      for (String s : this.reservedNames)
      {
        if (s.equals(name))
        {
          result = true;
          break;
        }
      }
      return result;
    }
  }
}