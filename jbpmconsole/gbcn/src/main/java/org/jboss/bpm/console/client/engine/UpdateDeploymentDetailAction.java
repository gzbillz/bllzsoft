package org.jboss.bpm.console.client.engine;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import org.jboss.bpm.console.client.model.DeploymentRef;

public class UpdateDeploymentDetailAction
  implements ActionInterface
{
  public static final String ID = UpdateDeploymentDetailAction.class.getName();

  public void execute(Controller controller, Object object)
  {
    DeploymentRef ref = object != null ? (DeploymentRef)object : null;
    DeploymentDetailView view = (DeploymentDetailView)controller.getView(DeploymentDetailView.ID);

    if (null == ref)
    {
      view.clearView();
    }
    else
    {
      view.update(ref);
    }
  }
}