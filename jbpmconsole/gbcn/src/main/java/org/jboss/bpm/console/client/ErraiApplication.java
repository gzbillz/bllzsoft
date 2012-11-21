package org.jboss.bpm.console.client;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import org.gwt.mosaic.ui.client.MessageBox;
import org.jboss.bpm.console.client.icons.ConsoleIconBundle;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.client.security.SecurityService;
import org.jboss.errai.workspaces.client.api.annotations.DefaultBundle;
import org.jboss.errai.workspaces.client.api.annotations.GroupOrder;
import org.jboss.errai.workspaces.client.framework.Registry;

@GroupOrder("Tasks, Processes, Reporting, Runtime, Settings")
@DefaultBundle(ConsoleIconBundle.class)
public class ErraiApplication
  implements EntryPoint, SubscribeListener
{
  public void onModuleLoad()
  {
    Log.setUncaughtExceptionHandler();

    DeferredCommand.addCommand(new Command()
    {
      public void execute()
      {
        DOM.getElementById("splash_loading").getStyle().setProperty("display", "none");
        DOM.getElementById("splash").getStyle().setProperty("zIndex", "-1");

        ErraiApplication.this.onModuleLoad2();
      }
    });
    ErraiBus.get().subscribe("appContext.model.listener", new MessageCallback()
    {
      public void callback(Message message)
      {
        Log.debug("Data model: " + message.getCommandType());
      }
    });
  }

  public void onModuleLoad2()
  {
    ClientMessageBus bus = (ClientMessageBus)ErraiBus.get();

    bus.addPostInitTask(new Runnable()
    {
      public void run()
      {
        ((SecurityService)Registry.get(SecurityService.class)).setDeferredNotification(true);
      }
    });
    bus.addSubscribeListener(this);

    Controller mainController = new Controller();
    Registry.set(Controller.class, mainController);

    String proxyUrl = null;
    if (!GWT.isScript())
    {
      proxyUrl = GWT.getModuleBaseURL() + "xhp";
    }

    final ConsoleConfig config = new ConsoleConfig(proxyUrl);
    ConsoleLog.debug("Console server: " + config.getConsoleServerUrl());

    URLBuilder.configureInstance(config);

    ApplicationContext appContext = new ApplicationContext()
    {
      public void displayMessage(String message, boolean isError)
      {
        if (isError)
          MessageBox.error("Error", message);
        else
          MessageBox.alert("Warn", message);
      }

      public Authentication getAuthentication()
      {
        return (Authentication)Registry.get(Authentication.class);
      }

      public ConsoleConfig getConfig()
      {
        return config;
      }

      public void refreshView()
      {
      }
    };
    Registry.set(ApplicationContext.class, appContext);

    registerGlobalViewsAndActions(mainController);

    mainController.addAction("login", new LoginAction());
    mainController.addAction(BootstrapAction.ID, new BootstrapAction());
    mainController.addView("loginView", new LoginView());

    mainController.handleEvent(new Event(BootstrapAction.ID, Boolean.TRUE));

    mainController.handleEvent(new Event("login", null));
  }

  private void registerGlobalViewsAndActions(Controller controller)
  {
    controller.addAction(LoadingStatusAction.ID, new LoadingStatusAction());
    controller.addAction(BootstrapAction.ID, new BootstrapAction());
  }

  public void onSubscribe(SubscriptionEvent event) {
    ConsoleLog.debug("New Subscription: " + event.getSubject());
  }
}