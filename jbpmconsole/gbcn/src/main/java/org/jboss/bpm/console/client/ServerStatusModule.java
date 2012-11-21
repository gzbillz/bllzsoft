package org.jboss.bpm.console.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

@LoadTool(name="System", group="Settings", icon="docIcon")
public class ServerStatusModule
  implements WidgetProvider
{
  static ServerStatusView instance = null;

  public void provideWidget(final ProvisioningCallback callback)
  {
    GWT.runAsync(new RunAsyncCallback()
    {
      public void onFailure(Throwable err)
      {
        ConsoleLog.error("Failed to load tool", err);
      }

      public void onSuccess()
      {
        if (ServerStatusModule.instance == null) {
          ServerStatusModule.instance = new ServerStatusView();
        }
        ServerStatusModule.instance.provideWidget(callback);
      }
    });
  }
}