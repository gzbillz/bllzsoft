package org.jboss.bpm.console.server.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class RsDocBuilder
{
  private String webContext;
  @SuppressWarnings("rawtypes")
private Class[] rootResources;

  public RsDocBuilder(String webContext, @SuppressWarnings("rawtypes") Class[] rootResources)
  {
    this.rootResources = rootResources;
    this.webContext = ("/" + webContext);
  }

  public String getWebContext()
  {
    return this.webContext;
  }

  @SuppressWarnings("rawtypes")
public Class[] getRootResources()
  {
    return this.rootResources;
  }

  private String build2HTML(@SuppressWarnings("rawtypes") Class root, String pname)
  {
    @SuppressWarnings("unchecked")
	Path rootPath = (Path)root.getAnnotation(Path.class);
    @SuppressWarnings("unchecked")
	RsComment rootComment = (RsComment)root.getAnnotation(RsComment.class);

    if (!supports(rootComment.project(), getProjectName(pname))) {
      return "";
    }

    List<Representation> representations = new ArrayList<Representation>();

    for (Method m : root.getDeclaredMethods())
    {
      Path resPath = (Path)m.getAnnotation(Path.class);
      RsComment resComment = (RsComment)m.getAnnotation(RsComment.class);
      if ((resPath != null) && ((resComment == null) || (supports(resComment.project(), getProjectName(pname)))))
      {
        Representation r = new Representation();

        r.path = resPath.value();
        r.httpMethod = (m.getAnnotation(GET.class) != null ? "GET" : "POST");
        r.consume = (m.getAnnotation(Consumes.class) != null ? arrayToString(((Consumes)m.getAnnotation(Consumes.class)).value()) : "*/*");

        r.produce = (m.getAnnotation(Produces.class) != null ? arrayToString(((Produces)m.getAnnotation(Produces.class)).value()) : "*/*");

        representations.add(r);
      }
    }

    StringBuffer sb = new StringBuffer();

    if (rootComment != null)
    {
      sb.append("<tr>");
      sb.append("<td colspan=5 style='border-bottom:1px solid black;'>");
      sb.append("<b>").append(rootComment.title()).append("</b>").append("<br>");
      sb.append("<i>").append(rootComment.description()).append("</i>");
      sb.append("</td>");
      sb.append("</tr>");
    }

    for (Representation r : representations)
    {
      sb.append("<tr>");
      sb.append("<td>").append(r.httpMethod.toUpperCase()).append("</td>");
      sb.append("<td>").append(buildPath(rootPath.value(), r.path)).append("</td>");
      sb.append("<td>").append("").append("</td>");
      sb.append("<td>").append(r.consume).append("</td>");
      sb.append("<td>").append(r.produce).append("</td>");
      sb.append("</tr>");
    }

    sb.append("<tr><td colspan=5>&nbsp;</td></tr>");

    return sb.toString();
  }

  private String arrayToString(String[] arr)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < arr.length; i++)
    {
      sb.append(arr[i]);
      if (i < arr.length - 1)
        sb.append(",");
    }
    return sb.toString();
  }

  private String buildPath(String root, String resourcePath)
  {
    StringBuffer sb = new StringBuffer();
    sb.append(this.webContext);
    sb.append("/").append(root).append("/");
    sb.append(resourcePath);
    return sb.toString();
  }

  public StringBuffer build2HTML(String pname)
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<html>").append("<body style='font-family: sans-serif; font-size:10pt;'>");
    sb.append("<!--").append(" generated by RsDoc at ").append(new Date()).append(" -->");
    sb.append("<table style='margin-top:10px;' width='100%'>");

    sb.append("<tr>");
    sb.append("<th>").append("Method").append("</th>");
    sb.append("<th>").append("Path").append("</th>");
    sb.append("<th>").append("Description").append("</th>");
    sb.append("<th>").append("Consumes").append("</th>");
    sb.append("<th>").append("Produces").append("</th>");
    sb.append("</tr>");

    for (@SuppressWarnings("rawtypes") Class c : this.rootResources)
    {
      sb.append(build2HTML(c, pname));
    }

    sb.append("</table>");
    sb.append("</body>").append("<html>");
    return sb;
  }

  public StringBuffer build2Docbook(String pname)
  {
    StringBuffer sbuffer = new StringBuffer();
    sbuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sbuffer.append("<!DOCTYPE chapter PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" \"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd\" [\n]>\n");

    sbuffer.append("<!-- Auto generated by machine, do not manually edit this -->\n");
    sbuffer.append("<chapter id=\"restful_services\">\n");
    sbuffer.append("<title>Restful Services</title>\n");
    sbuffer.append("<para>\n");
    sbuffer.append("This is a list of Restful services that are used by the bpel console.\n");
    sbuffer.append("<table>\n<thead>\n<th>Method</th><th>Path</th><th>Description</th><th>Consumes</th><th>Produces</th>\n</thead>\n");
    sbuffer.append("<tbody>\n");

    for (@SuppressWarnings("rawtypes") Class c : this.rootResources) {
      sbuffer.append(build2Docbook(c, pname));
    }

    sbuffer.append("</tbody>\n").append("</table>\n").append("</para>\n").append("</chapter>");

    return sbuffer;
  }

  private String build2Docbook(@SuppressWarnings("rawtypes") Class root, String pname)
  {
    @SuppressWarnings("unchecked")
	Path rootPath = (Path)root.getAnnotation(Path.class);
    @SuppressWarnings("unchecked")
	RsComment rootComment = (RsComment)root.getAnnotation(RsComment.class);

    if (!supports(rootComment.project(), getProjectName(pname))) {
      return "";
    }

    List<Representation> representations = new ArrayList<Representation>();

    for (Method m : root.getDeclaredMethods())
    {
      Path resPath = (Path)m.getAnnotation(Path.class);
      RsComment resComment = (RsComment)m.getAnnotation(RsComment.class);

      if ((resPath != null) && ((resComment == null) || (supports(resComment.project(), getProjectName(pname)))))
      {
        Representation r = new Representation();

        r.path = resPath.value();
        r.httpMethod = (m.getAnnotation(GET.class) != null ? "GET" : "POST");
        r.consume = (m.getAnnotation(Consumes.class) != null ? arrayToString(((Consumes)m.getAnnotation(Consumes.class)).value()) : "*/*");

        r.produce = (m.getAnnotation(Produces.class) != null ? arrayToString(((Produces)m.getAnnotation(Produces.class)).value()) : "*/*");

        representations.add(r);
      }
    }

    StringBuffer sb = new StringBuffer();
    if (rootComment != null) {
      sb.append("<tr><td colspan=\"5\"><emphasis>").append(rootComment.title()).append("(").append(rootComment.description()).append(")</emphasis></td></tr>\n");
    }

    for (Representation r : representations)
    {
      sb.append("<tr>").append("<td>").append(r.httpMethod.toUpperCase()).append("</td><td>").append(buildPath(rootPath.value(), r.path)).append("</td><td>").append("").append("</td><td>").append(r.consume).append("</td><td>").append(r.produce).append("</td></tr>\n");
    }

    return sb.toString();
  }

  private boolean supports(ProjectName[] names, ProjectName name) {
    for (ProjectName aName : names) {
      if (name.equals(aName)) {
        return true;
      }
    }
    return false;
  }

  private ProjectName getProjectName(String projectName)
  {
    if ("RIFTSAW".equalsIgnoreCase(projectName.trim()))
      return ProjectName.RIFTSAW;
    if ("JBPM".equalsIgnoreCase(projectName.trim()))
      return ProjectName.JBPM;
    if ("DROOLS".equalsIgnoreCase(projectName.trim())) {
      return ProjectName.DROOLS;
    }
    return null;
  }

  private class Representation
  {
//    String description;
//    String title = "";
    String consume;
    String produce = "";
    String path;
    String httpMethod;

    private Representation()
    {
    }
  }
}