package org.jboss.bpm.console.client.process;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import org.jboss.bpm.console.client.process.events.InstanceEvent;

public class UpdateInstanceDetailAction
  implements ActionInterface
{
  public static final String ID = UpdateInstanceDetailAction.class.getName();

  public void execute(Controller controller, Object object)
  {
    InstanceEvent event = (InstanceEvent)object;

    InstanceDetailView view = (InstanceDetailView)controller.getView(InstanceDetailView.ID);

    if (event.getInstance() != null)
      view.update(event.getDefinition(), event.getInstance());
    else
      view.clearView();
  }
}