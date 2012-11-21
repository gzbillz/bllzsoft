package org.jboss.bpm.console.client;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.GridLayout;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.common.HeaderLabel;
import org.jboss.bpm.console.client.model.PluginInfo;
import org.jboss.bpm.console.client.model.ServerStatus;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

public class ServerStatusView
  implements ViewInterface, LazyPanel, WidgetProvider
{
  public static final String ID = ServerStatusView.class.getName();
  private Controller controller;
  @SuppressWarnings("unused")
private ApplicationContext appContext;
  private boolean initialized;
  MosaicPanel layoutPanel;
  MosaicPanel pluginPanel;

  public ServerStatusView()
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
    this.controller = ((Controller)Registry.get(Controller.class));
  }

  public void provideWidget(ProvisioningCallback callback)
  {
    this.layoutPanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    HeaderLabel console = new HeaderLabel("Console Info");
    this.layoutPanel.add(console, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    MosaicPanel layout1 = new MosaicPanel(new GridLayout(2, 1));
    layout1.add(new HTML("Version:"));
    layout1.add(new HTML("2.3.5.Final"));

    this.layoutPanel.add(layout1, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    HeaderLabel server = new HeaderLabel("Server Info");
    this.layoutPanel.add(server, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    MosaicPanel layout2 = new MosaicPanel(new GridLayout(2, 2));
    layout2.add(new HTML("Host:"));
    layout2.add(new HTML(((ApplicationContext)Registry.get(ApplicationContext.class)).getConfig().getConsoleServerUrl()));

    this.pluginPanel = new MosaicPanel();
    layout2.add(new Label("Plugins:"));
    layout2.add(this.pluginPanel);

    this.layoutPanel.add(layout2, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    this.controller.addView(ID, this);

    update(ServerPlugins.getStatus());

    callback.onSuccess(this.layoutPanel);
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public boolean isInitialized()
  {
    return this.initialized;
  }

  public void initialize()
  {
    if (!this.initialized)
    {
      update(ServerPlugins.getStatus());
      this.initialized = true;
    }
  }

  private void update(ServerStatus status)
  {
    this.pluginPanel.clear();

    Grid g = new Grid(status.getPlugins().size(), 2);
    g.setWidth("100%");

    for (int row = 0; row < status.getPlugins().size(); row++)
    {
      PluginInfo p = (PluginInfo)status.getPlugins().get(row);
      String type = p.getType().substring(p.getType().lastIndexOf(".") + 1, p.getType().length());

      g.setText(row, 0, type);

      Image img = p.isAvailable() ? new Image("images/icons/confirm_small.png") : new Image("images/icons/deny_small.png");

      g.setWidget(row, 1, img);
    }

    this.pluginPanel.add(g);

    this.pluginPanel.layout();
  }
}