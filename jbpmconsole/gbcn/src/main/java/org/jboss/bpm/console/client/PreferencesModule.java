package org.jboss.bpm.console.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

@LoadTool(name="Preferences", group="Settings", icon="runtimeIcon")
public class PreferencesModule
  implements WidgetProvider
{
  static PreferencesView instance = null;

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
        if (PreferencesModule.instance == null) {
          PreferencesModule.instance = new PreferencesView();
        }
        PreferencesModule.instance.provideWidget(callback);
      }
    });
  }
}