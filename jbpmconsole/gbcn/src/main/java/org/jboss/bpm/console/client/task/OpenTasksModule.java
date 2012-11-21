package org.jboss.bpm.console.client.task;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

@LoadTool(name="Group Tasks", group="Tasks", priority=1, icon="taskIcon")
public class OpenTasksModule
  implements WidgetProvider
{
  static OpenTasksView instance = null;

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
        if (OpenTasksModule.instance == null) {
          OpenTasksModule.instance = new OpenTasksView();
        }
        OpenTasksModule.instance.provideWidget(callback);
      }
    });
  }
}