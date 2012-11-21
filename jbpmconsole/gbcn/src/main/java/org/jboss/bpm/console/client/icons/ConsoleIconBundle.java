package org.jboss.bpm.console.client.icons;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public abstract interface ConsoleIconBundle extends ClientBundle
{
  @ClientBundle.Source({"processIcon.png"})
  public abstract ImageResource processIcon();

  @ClientBundle.Source({"play.png"})
  public abstract ImageResource instanceIcon();

  @ClientBundle.Source({"toolsIcon.png"})
  public abstract ImageResource settingsIcon();

  @ClientBundle.Source({"taskIcon.png"})
  public abstract ImageResource taskIcon();

  @ClientBundle.Source({"userIcon.png"})
  public abstract ImageResource userIcon();

  @ClientBundle.Source({"tool-button-collapse-down.png"})
  public abstract ImageResource collapseDownIcon();

  @ClientBundle.Source({"tool-button-collapse-left.png"})
  public abstract ImageResource collapseLeftIcon();

  @ClientBundle.Source({"errorIcon.png"})
  public abstract ImageResource errorIcon();

  @ClientBundle.Source({"dialog-information.png"})
  public abstract ImageResource infoIcon();

  @ClientBundle.Source({"dialog-warning.png"})
  public abstract ImageResource warnIcon();

  @ClientBundle.Source({"dialog-question.png"})
  public abstract ImageResource questionIcon();

  @ClientBundle.Source({"loading.gif"})
  public abstract ImageResource loadingIcon();

  @ClientBundle.Source({"reload.png"})
  public abstract ImageResource reloadIcon();

  @ClientBundle.Source({"report.png"})
  public abstract ImageResource reportIcon();

  @ClientBundle.Source({"runtime.png"})
  public abstract ImageResource runtimeIcon();

  @ClientBundle.Source({"deployment.png"})
  public abstract ImageResource deploymentIcon();

  @ClientBundle.Source({"jobs.png"})
  public abstract ImageResource jobsIcon();

  @ClientBundle.Source({"ws.png"})
  public abstract ImageResource webserviceIcon();

  @ClientBundle.Source({"arrow_blue.png"})
  public abstract ImageResource arrowIcon();

  @ClientBundle.Source({"piece.png"})
  public abstract ImageResource pieceIcon();

  @ClientBundle.Source({"reportInstance.png"})
  public abstract ImageResource reportInstanceIcon();

  @ClientBundle.Source({"docIcon.png"})
  public abstract ImageResource docIcon();

  @ClientBundle.Source({"filter.png"})
  public abstract ImageResource filterIcon();

  @ClientBundle.Source({"database.gif"})
  public abstract ImageResource databaseIcon();

  @ClientBundle.Source({"red.png"})
  public abstract ImageResource redIcon();

  @ClientBundle.Source({"green.png"})
  public abstract ImageResource greenIcon();

  @ClientBundle.Source({"blue.png"})
  public abstract ImageResource blueIcon();

  @ClientBundle.Source({"yellow.png"})
  public abstract ImageResource yellowIcon();

  @ClientBundle.Source({"grey.png"})
  public abstract ImageResource greyIcon();

  @ClientBundle.Source({"large.png"})
  public abstract ImageResource historySearchIcon();
}