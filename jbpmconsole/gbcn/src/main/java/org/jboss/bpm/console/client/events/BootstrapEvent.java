package org.jboss.bpm.console.client.events;

import org.jboss.bpm.console.client.Authentication;
import org.jboss.bpm.console.client.ConsoleConfig;
import org.jboss.bpm.console.client.URLBuilder;

public final class BootstrapEvent
{
  Authentication auth;
  URLBuilder urlBuilder;
  ConsoleConfig config;

  public BootstrapEvent(Authentication auth, URLBuilder urlBuilder, ConsoleConfig config)
  {
    this.auth = auth;
    this.urlBuilder = urlBuilder;
    this.config = config;
  }

  public Authentication getAuth()
  {
    return this.auth;
  }

  public URLBuilder getUrlBuilder()
  {
    return this.urlBuilder;
  }

  public ConsoleConfig getConfig()
  {
    return this.config;
  }
}