package org.jboss.bpm.console.client.process;

import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.ui.client.layout.ColumnLayout;
import org.gwt.mosaic.ui.client.layout.ColumnLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;

public class MergedProcessView
  implements WidgetProvider
{
  MosaicPanel panel;
  DefinitionListView definitionView;
  InstanceListView instanceView;

  public void provideWidget(ProvisioningCallback callback)
  {
 
    this.panel = new MosaicPanel();
    this.panel.setPadding(0);

    this.definitionView = new DefinitionListView();
    this.instanceView = new InstanceListView();

    final MosaicPanel splitPanel = new MosaicPanel(new ColumnLayout());
    splitPanel.setPadding(0);

    this.definitionView.provideWidget(new ProvisioningCallback()
    {
      public void onSuccess(Widget instance)
      {
        splitPanel.add(instance, new ColumnLayoutData("250 px"));
      }

      public void onUnavailable()
      {
        ConsoleLog.error("Failed to load DefinitionListView.class");
      }
    });
    this.instanceView.provideWidget(new ProvisioningCallback()
    {
      public void onSuccess(Widget instance)
      {
        splitPanel.add(instance);
      }

      public void onUnavailable()
      {
        ConsoleLog.error("Failed to load DefinitionListView.class");
      }
    });
    this.panel.add(splitPanel);

    callback.onSuccess(this.panel);
  }
}