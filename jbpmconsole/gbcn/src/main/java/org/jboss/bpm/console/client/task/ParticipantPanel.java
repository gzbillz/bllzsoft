package org.jboss.bpm.console.client.task;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.model.ParticipantRef;
import org.jboss.bpm.console.client.model.TaskRef;
import org.jboss.bpm.console.client.task.events.AssignEvent;

@SuppressWarnings("deprecation")
class ParticipantPanel extends MosaicPanel
  implements ViewInterface
{
  public static final String ID = ParticipantPanel.class.getName();
  public static final String PARTICIPANTS = "Participants";
  public static final String GROUPS = "Groups";
  public static final String USERS = "Users";
  private Controller controller;
  private Tree tree;
  private Button assignmentBtn;
  private String selection = null;
  private TaskRef currentTask;

public ParticipantPanel()
  {
    super(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    setPadding(5);

    ScrollLayoutPanel treePanel = new ScrollLayoutPanel();

    treePanel.setStyleName("bpm-property-box");

    this.tree = new Tree();
    treePanel.add(this.tree);
    add(treePanel, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    this.tree.addTreeListener(new TreeListener()
    {
      public void onTreeItemSelected(TreeItem treeItem)
      {
        String name = treeItem.getText();
        if (((!name.equals("Participants") ? 1 : 0) & (!name.equals("Groups") ? 1 : 0) & (!name.equals("Users") ? 1 : 0)) != 0)
        {
          ParticipantPanel.this.selection = name;
          ParticipantPanel.this.assignmentBtn.setEnabled(true);
        }
        else
        {
          ParticipantPanel.this.selection = null;
          ParticipantPanel.this.assignmentBtn.setEnabled(false);
        }
      }

      public void onTreeItemStateChanged(TreeItem treeItem)
      {
      }
    });
    this.assignmentBtn = new Button("Assign", new ClickListener()
    {
      public void onClick(Widget widget)
      {
        if (ParticipantPanel.this.selection != null)
        {
          ParticipantPanel.this.controller.handleEvent(new Event(AssignTaskAction.ID, new AssignEvent(ParticipantPanel.this.selection, ParticipantPanel.this.currentTask)));
        }
      }
    });
    this.assignmentBtn.setStyleName("bpm-operation-ui");
    this.assignmentBtn.setEnabled(false);
    add(this.assignmentBtn);
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void update(TaskRef task)
  {
    this.currentTask = task;

    this.tree.clear();

    TreeItem root = this.tree.addItem("Participants");

    TreeItem groups = root.addItem("Groups");
    for (ParticipantRef gref : task.getParticipantGroups())
    {
      groups.addItem(gref.getIdRef());
    }

    TreeItem users = root.addItem("Users");
    for (ParticipantRef uref : task.getParticipantUsers())
    {
      users.addItem(uref.getIdRef());
    }

    root.setState(true);

    invalidate();
  }

  public void clearView()
  {
    this.tree.clear();
    this.currentTask = null;
  }
}