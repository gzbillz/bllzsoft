package org.jboss.bpm.console.client.task;

import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.Authentication;
import org.jboss.bpm.console.client.model.TaskRef;
import org.jboss.errai.workspaces.client.framework.Registry;

public abstract class AbstractTaskList
  implements ViewInterface
{
  protected Controller controller;
  protected MosaicPanel taskList = null;
  protected ListBox<TaskRef> listBox;
  protected boolean isInitialized;
  protected String identity;
  protected List<TaskRef> cachedTasks;

  public boolean isInitialized()
  {
    return this.isInitialized;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public TaskRef getSelection()
  {
    TaskRef selection = null;
    if ((isInitialized()) && (this.listBox.getSelectedIndex() != -1))
    {
      selection = (TaskRef)this.listBox.getItem(this.listBox.getSelectedIndex());
    }
    return selection;
  }

  public String getAssignedIdentity()
  {
    return ((Authentication)Registry.get(Authentication.class)).getUsername();
  }
}