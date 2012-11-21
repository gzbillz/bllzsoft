package org.jboss.bpm.console.client.process;

import com.google.gwt.user.client.ui.HTML;
import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.jboss.bpm.console.client.model.ActiveNodeInfo;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.process.events.ActivityDiagramResultEvent;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class ActivityDiagramView extends ScrollLayoutPanel
  implements ViewInterface
{
  public static final String ID = ActivityDiagramView.class.getName();
  @SuppressWarnings("unused")
private ProcessDefinitionRef processRef;
  @SuppressWarnings("unused")
private ProcessInstanceRef instanceRef;
  @SuppressWarnings("unused")
private Controller controller;

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void update(ActivityDiagramResultEvent event)
  {
    List<ActiveNodeInfo> activeNodeInfos = event.getActiveNodeInfo();
    String imageUrl = event.getImageUrl();
    String imageUrlNoCache = imageUrl + "?" + System.currentTimeMillis();
    ConsoleLog.debug("Getting image from " + imageUrlNoCache);

    clear();

    String s = "<div style='width:1024px; height:768px; background-color:#ffffff;'><div id=\"imageContainer\" style=\"position:relative;top:-1;left:-1;\"><img src=\"" + imageUrlNoCache + "\" style=\"position:absolute;top:0;left:0\" />";

    for (ActiveNodeInfo activeNodeInfo : activeNodeInfos)
    {
      s = s + "<div class=\"bpm-graphView-activityImage\" style=\"position:absolute;top:" + (activeNodeInfo.getActiveNode().getY() - 8) + "px;left:" + (activeNodeInfo.getActiveNode().getX() - 8) + "px;width:50px;height:50px; z-index:1000;background-image: url(images/icons/play_red_big.png);background-repeat:no-repeat;\"></div>";
    }
    s = s + "</div></div>";

    HTML html = new HTML(s);

    add(html);
    invalidate();
  }

  public void update(String imageUrl)
  {
    clear();
    String imageUrlNoCache = imageUrl + "?" + System.currentTimeMillis();
    ConsoleLog.debug("Getting image from " + imageUrlNoCache);
    String s = "<div style='width:1024px; height:768px; background-color:#ffffff;'><div id=\"imageContainer\" style=\"position:relative;top:-1;left:-1;\"><img src=\"" + imageUrlNoCache + "\" style=\"position:absolute;top:0;left:0\" />";

    s = s + "</div></div>";

    HTML html = new HTML(s);

    add(html);
    invalidate();
  }
}