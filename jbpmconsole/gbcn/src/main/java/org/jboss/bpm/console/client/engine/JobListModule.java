package org.jboss.bpm.console.client.engine;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

@LoadTool(name="Jobs", group="Runtime", icon="jobsIcon")
@RequireRoles({"admin"})
public class JobListModule
  implements WidgetProvider
{
  static JobListView instance;

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
        if (JobListModule.instance == null) {
          JobListModule.instance = new JobListView();
        }
        JobListModule.instance.provideWidget(callback);
      }
    });
  }
}