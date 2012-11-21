package org.jboss.bpm.console.client.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

@LoadTool(name="Process Overview", group="Processes", icon="processIcon", priority=1)
public class ProcessModule
  implements WidgetProvider
{
  static MergedProcessView instance = null;

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
        if (ProcessModule.instance == null) {
          ProcessModule.instance = new MergedProcessView();
        }
        ProcessModule.instance.provideWidget(callback);
      }
    });
  }
}