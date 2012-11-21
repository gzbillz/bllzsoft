package org.jboss.bpm.console.client;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;

public class LoginAction
  implements ActionInterface
{
  public void execute(Controller controller, Object object)
  {
    LoginView loginView = (LoginView)controller.getView("loginView");
    loginView.display();
  }
}