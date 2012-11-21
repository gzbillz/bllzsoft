package org.jboss.bpm.console.client.task;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

@LoadTool(name="Personal Tasks", group="Tasks", priority=2, icon="userIcon")
public class AssignedTasksModule
  implements WidgetProvider
{
  static AssignedTasksView instance = null;

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
        if (AssignedTasksModule.instance == null) {
          AssignedTasksModule.instance = new AssignedTasksView();
        }
        AssignedTasksModule.instance.provideWidget(callback);
      }
    });
  }
}