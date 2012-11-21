package org.jboss.bpm.console.client.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

@LoadTool(name="Execution History", group="Processes", icon="databaseIcon", priority=2)
public class ProcessHistoryModule
  implements WidgetProvider
{
  static MergedProcessHistoryView instance = null;

  public void provideWidget(ProvisioningCallback callback)
  {
    createInstance(callback);
  }

  public static void createInstance(final ProvisioningCallback callback)
  {
    GWT.runAsync(new RunAsyncCallback()
    {
      public void onFailure(Throwable err)
      {
        ConsoleLog.error("Failed to load tool", err);
      }

      public void onSuccess()
      {
        if (ProcessHistoryModule.instance == null) {
          ProcessHistoryModule.instance = new MergedProcessHistoryView();
        }
        ProcessHistoryModule.instance.provideWidget(callback);
      }
    });
  }
}