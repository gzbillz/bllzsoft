package org.jboss.bpm.console.client.engine;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.errai.workspaces.client.framework.Registry;

public class ViewDeploymentAction
  implements ActionInterface
{
  public static final String ID = ViewDeploymentAction.class.getName();
  @SuppressWarnings("unused")
private ApplicationContext appContext;

  public ViewDeploymentAction()
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
  }

  public void execute(Controller controller, Object object)
  {
    @SuppressWarnings("unused")
	String dplId = (String)object;

    throw new RuntimeException("Not implemented");
  }
}