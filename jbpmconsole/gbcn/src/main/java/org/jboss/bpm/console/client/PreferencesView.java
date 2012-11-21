package org.jboss.bpm.console.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.GridLayout;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.common.HeaderLabel;
import org.jboss.errai.workspaces.client.Workspace;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.ToolSet;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Preferences;
import org.jboss.errai.workspaces.client.framework.Registry;

public class PreferencesView
  implements ViewInterface, WidgetProvider
{
  public static final String ID = PreferencesView.class.getName();
  @SuppressWarnings("unused")
private Controller controller;
  @SuppressWarnings("unused")
private ApplicationContext appContext;
  MosaicPanel panel;

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void provideWidget(ProvisioningCallback callback)
  {
    this.panel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
    this.panel.add(new HeaderLabel("User Preferences"), new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    MosaicPanel layout = new MosaicPanel(new GridLayout(2, 1));
    layout.add(new HTML("<b>Default Tool</b><br>Select the tool that should be loaded upon login."));

    final List<ToolSet> toolsets = Workspace.getInstance().getToolsets();

    final ListBox multiBox = new ListBox();
    multiBox.setVisibleItemCount(5);
    layout.add(multiBox);

    final Preferences prefs = (Preferences)GWT.create(Preferences.class);
    String prefEditor = prefs.get("workspace.default.tool");
    for (ToolSet ts : toolsets)
    {
      multiBox.addItem(ts.getToolSetName());
      if (ts.getToolSetName().equals(prefEditor)) {
        multiBox.setItemSelected(multiBox.getItemCount() - 1, true);
      }
    }
    multiBox.addClickHandler(new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        String title = multiBox.getItemText(multiBox.getSelectedIndex());
        for (ToolSet ts : toolsets)
        {
          if (ts.getToolSetName().equals(title))
          {
            prefs.set("workspace.default.tool", ts.getToolSetName());
          }
        }
      }
    });
    this.panel.add(layout, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    callback.onSuccess(this.panel);
  }
}