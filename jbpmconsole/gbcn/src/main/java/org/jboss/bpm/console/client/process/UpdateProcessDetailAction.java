package org.jboss.bpm.console.client.process;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;

public class UpdateProcessDetailAction
  implements ActionInterface
{
  public static final String ID = UpdateProcessDetailAction.class.getName();

  public void execute(Controller controller, Object object)
  {
    ProcessDefinitionRef process = object != null ? (ProcessDefinitionRef)object : null;
    ProcessDetailView view = (ProcessDetailView)controller.getView(ProcessDetailView.ID);

    if (process != null)
      view.update(process);
    else
      view.clearView();
  }
}