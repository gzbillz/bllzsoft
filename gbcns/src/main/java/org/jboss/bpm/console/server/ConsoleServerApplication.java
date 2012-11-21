package org.jboss.bpm.console.server;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

public class ConsoleServerApplication extends Application
{
  HashSet<Object> singletons = new HashSet<Object>();

  public ConsoleServerApplication(@Context ServletContext servletContext)
  {
    this.singletons.add(new InfoFacade());
    this.singletons.add(new ProcessMgmtFacade());
    this.singletons.add(new TaskListFacade());
    this.singletons.add(new TaskMgmtFacade());
    this.singletons.add(new UserMgmtFacade());
    this.singletons.add(new EngineFacade());
    this.singletons.add(new FormProcessingFacade());
    try
    {
      @SuppressWarnings("rawtypes")
	Class reportFacadeDefinition = Class.forName("org.jboss.bpm.report.ReportFacade");

      if (System.getProperty("reporting.needcontext") != null)
      {
        @SuppressWarnings({ "rawtypes", "unchecked" })
		Constructor reportFacadeConstructor = reportFacadeDefinition.getConstructor(new Class[] { ServletContext.class });

        this.singletons.add(reportFacadeConstructor.newInstance(new Object[] { servletContext }));
      } else {
        this.singletons.add(reportFacadeDefinition.newInstance());
      }
    } catch (Exception e) {
      System.out.println("Unable to load ReportFacade: " + e.getMessage());
    }
    this.singletons.add(new ProcessHistoryFacade());
  }

  @SuppressWarnings("unchecked")
public Set<Class<?>> getClasses()
  {
    @SuppressWarnings("rawtypes")
	HashSet set = new HashSet();
    return set;
  }

  public Set<Object> getSingletons()
  {
    return this.singletons;
  }
}