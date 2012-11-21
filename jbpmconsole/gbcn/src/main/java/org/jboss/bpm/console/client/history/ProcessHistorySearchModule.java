package org.jboss.bpm.console.client.history;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

@LoadTool(name="History Query", group="Processes", icon="historySearchIcon", priority=3)
public class ProcessHistorySearchModule
  implements WidgetProvider
{
  private ProcessHistorySearchView instance;

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
        if (ProcessHistorySearchModule.this.instance == null) {
          ProcessHistorySearchModule.this.instance = new ProcessHistorySearchView();
        }
        ProcessHistorySearchModule.this.instance.provideWidget(callback);
      }
    });
  }
}