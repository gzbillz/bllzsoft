package org.jboss.bpm.console.client.task;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.errai.workspaces.client.framework.Registry;

public class ReloadAllTaskListsAction
  implements ActionInterface
{
  public static final String ID = ReloadAllTaskListsAction.class.getName();
  private ApplicationContext appContext;

  public ReloadAllTaskListsAction()
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
  }

  public void execute(Controller controller, Object object)
  {
    String currentUser = this.appContext.getAuthentication().getUsername();
    controller.handleEvent(new Event(LoadTasksAction.ID, currentUser));

    controller.handleEvent(new Event(LoadTasksParticipationAction.ID, currentUser));
  }
}