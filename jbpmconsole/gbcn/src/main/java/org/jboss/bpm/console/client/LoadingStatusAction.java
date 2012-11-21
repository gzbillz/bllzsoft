package org.jboss.bpm.console.client;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;

public class LoadingStatusAction
  implements ActionInterface
{
  public static final String ID = LoadingStatusAction.class.getName();

  public static final Event ON = new Event(ID, Boolean.valueOf(true));
  public static final Event OFF = new Event(ID, Boolean.valueOf(false));

  public void execute(Controller controller, Object object)
  {
//    Boolean b = (Boolean)object;
  }
}