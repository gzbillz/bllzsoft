package org.jboss.bpm.console.server;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
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
import org.jboss.bpm.console.client.model.RoleAssignmentRef;
import org.jboss.bpm.console.client.model.RoleAssignmentRefWrapper;
import org.jboss.bpm.console.server.gson.GsonFactory;
import org.jboss.bpm.console.server.integration.ManagementFactory;
import org.jboss.bpm.console.server.integration.UserManagement;
import org.jboss.bpm.console.server.util.RsComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("identity")
@RsComment(title="User management", description="Manage user and groups", project={org.jboss.bpm.console.server.util.ProjectName.JBPM}, example = "")
public class UserMgmtFacade
{
  private static final Logger log = LoggerFactory.getLogger(UserMgmtFacade.class);
  private UserManagement userManagement;

  private UserManagement getUserManagement()
  {
    if (null == this.userManagement)
    {
      ManagementFactory factory = ManagementFactory.newInstance();
      this.userManagement = factory.createUserManagement();
    }

    return this.userManagement;
  }

  @GET
  @Path("sid")
  @Produces({"text/plain"})
  public Response getSessionID(@Context HttpServletRequest request)
  {
    return Response.ok(request.getSession().getId()).build();
  }

  @POST
  @Path("sid/invalidate")
  @Produces({"text/plain"})
  public Response destroySession(@Context HttpServletRequest request)
  {
    request.getSession().invalidate();
    return Response.ok().build();
  }

  @GET
  @Path("secure/sid")
  @Produces({"text/plain"})
  public Response getSessionIDSecure(@Context HttpServletRequest request)
  {
    return Response.ok(request.getSession().getId()).build();
  }

  @GET
  @Path("user/roles")
  @Produces({"application/json"})
  public Response getRolesForJAASPrincipal(@Context HttpServletRequest request, @QueryParam("roleCheck") String roleCheck)
  {
    if (null == roleCheck) {
      throw new WebApplicationException(new IllegalArgumentException("Missing parameter 'roleCheck'"));
    }
    log.debug("Role check user: " + request.getUserPrincipal().getName() + ", actualRoles requested: " + roleCheck);

    List<RoleAssignmentRef> actualRoles = new ArrayList<RoleAssignmentRef>();

    StringTokenizer tok = new StringTokenizer(roleCheck, ",");
    while (tok.hasMoreTokens())
    {
      String possibleRole = tok.nextToken();
      actualRoles.add(new RoleAssignmentRef(possibleRole, request.isUserInRole(possibleRole)));
    }
    return createJsonResponse(new RoleAssignmentRefWrapper(actualRoles));
  }

  @GET
  @Path("user/{actorId}/groups/")
  @Produces({"application/json"})
  public Response getGroupsForActor(@PathParam("actorId") String actorId)
  {
    List<String> groups = getUserManagement().getGroupsForActor(actorId);
    return createJsonResponse(groups);
  }

  @GET
  @Path("group/{groupName}/members")
  @Produces({"application/json"})
  public Response getActorsForGroup(@PathParam("groupName") String groupName)
  {
    List<String> groups = getUserManagement().getActorsForGroup(groupName);
    return createJsonResponse(groups);
  }

  @GET
  @Path("user/{actorId}/actors")
  @Produces({"application/json"})
  public Response getAvailableActors(@PathParam("actorId") String actorId)
  {
    Set<String> users = new HashSet<String>();
    List<String> groups = getUserManagement().getGroupsForActor(actorId);
    for (String group : groups)
    {
      List<String> actors = getUserManagement().getActorsForGroup(group);
      users.addAll(actors);
    }

    List<String> availableActors = new ArrayList<String>();
    availableActors.addAll(users);
    availableActors.addAll(groups);
    return createJsonResponse(availableActors);
  }

  private Response createJsonResponse(Object wrapper)
  {
    Gson gson = GsonFactory.createInstance();
    String json = gson.toJson(wrapper);
    return Response.ok(json).type("application/json").build();
  }
}