package org.jboss.bpm.console.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.jboss.bpm.console.server.InfoFacade;

public class RsDocGenerator
{
  private File output;
  @SuppressWarnings("unused")
private String projectName;
  @SuppressWarnings("unused")
private static final String contextPath = "/gwt-console-server/rs";
  private RsDocBuilder builder;

  public RsDocGenerator(String outputDir)
    throws Exception
  {
    try
    {
      this.output = new File(outputDir);
      if (!this.output.exists())
        this.output.mkdirs();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    this.builder = new RsDocBuilder("/gwt-console-server/rs", InfoFacade.getRSResources());
  }

  public void generate(String project, String type)
  {
    String filename = this.output.getAbsolutePath() + "/" + project + "_restful_service." + type;

    String result = null;

    if ("html".equalsIgnoreCase(type))
      result = this.builder.build2HTML(project).toString();
    else if ("xml".equalsIgnoreCase(type)) {
      result = this.builder.build2Docbook(project).toString();
    }

    Writer out = null;
    try {
      out = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
      out.write(result);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (out != null)
        try {
          out.close();
        } catch (IOException ie) {
          throw new RuntimeException("Error in closing IO.", ie);
        }
    }
  }

  public static void main(String[] args)
    throws Exception
  {
    String dir = args[0];

    RsDocGenerator generator = new RsDocGenerator(dir);
    generator.generate("riftsaw", "html");
    generator.generate("riftsaw", "xml");

    generator.generate("jbpm", "html");
    generator.generate("jbpm", "xml");
  }
}