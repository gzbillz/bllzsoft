package org.jboss.bpm.console.client.process;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;

public class ClearInstancesAction
  implements ActionInterface
{
  public static String ID = ClearInstancesAction.class.getName();

  public void execute(Controller controller, Object o)
  {
    InstanceListView view = (InstanceListView)controller.getView(InstanceListView.ID);
    view.reset();
  }
}