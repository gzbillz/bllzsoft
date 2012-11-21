package org.jboss.bpm.console.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;

public class ConsoleConfig
{
  private String serverWebContext;
  private String overallReportFile;
  private String processSummaryReportFile;
  private String instanceSummaryReportFile;
  private String profileName;
  private String logo;
  private String consoleServerUrl;
  private String defaultEditor;

  public ConsoleConfig(String proxyUrl)
  {
    Dictionary theme = Dictionary.getDictionary("consoleConfig");
    this.profileName = theme.get("profileName");
    this.logo = theme.get("logo");

    this.serverWebContext = theme.get("serverWebContext");

    this.overallReportFile = theme.get("overallReportFile");
    this.processSummaryReportFile = theme.get("processSummaryReportFile");
    this.instanceSummaryReportFile = theme.get("instanceSummaryReportFile");

    this.defaultEditor = theme.get("defaultEditor");

    if (null == proxyUrl)
    {
      String base = GWT.getHostPageBaseURL();
      String protocol = base.substring(0, base.indexOf("//") + 2);
      String noProtocol = base.substring(base.indexOf(protocol) + protocol.length(), base.length());
      String host = noProtocol.substring(0, noProtocol.indexOf("/"));

      this.consoleServerUrl = (protocol + host + this.serverWebContext);
    }
    else
    {
      this.consoleServerUrl = proxyUrl;
    }
  }

  public String getHost()
  {
    String host = null;
    if (!GWT.isScript())
    {
      host = this.consoleServerUrl;
    }
    else
    {
      String baseUrl = GWT.getModuleBaseURL();
      host = baseUrl.substring(0, baseUrl.indexOf("app"));
    }

    return host;
  }

  public String getProfileName()
  {
    return this.profileName;
  }

  public String getLogo()
  {
    return this.logo;
  }

  public String getDefaultEditor()
  {
    return this.defaultEditor;
  }

  public String getConsoleServerUrl()
  {
    if (this.consoleServerUrl == null)
      throw new RuntimeException("Config not properly setup: console server URL is null");
    return this.consoleServerUrl;
  }

  public void setConsoleServerUrl(String consoleServerUrl)
  {
    this.consoleServerUrl = consoleServerUrl;
  }

  public String getServerWebContext()
  {
    return this.serverWebContext;
  }

  public void setServerWebContext(String serverWebContext)
  {
    this.serverWebContext = serverWebContext;
  }

  public String getOverallReportFile()
  {
    return this.overallReportFile;
  }

  public void setOverallReportFile(String overallReportFile)
  {
    this.overallReportFile = overallReportFile;
  }

  public String getProcessSummaryReportFile()
  {
    return this.processSummaryReportFile;
  }

  public void setProcessSummaryReportFile(String processSummaryReportFile)
  {
    this.processSummaryReportFile = processSummaryReportFile;
  }

  public String getInstanceSummaryReportFile()
  {
    return this.instanceSummaryReportFile;
  }

  public void setInstanceSummaryReportFile(String instanceSummaryReportFile)
  {
    this.instanceSummaryReportFile = instanceSummaryReportFile;
  }
}