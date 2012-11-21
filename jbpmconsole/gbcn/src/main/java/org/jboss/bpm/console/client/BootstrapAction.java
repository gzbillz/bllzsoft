package org.jboss.bpm.console.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.mvc4g.client.Controller;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.model.JSOParser;
import org.jboss.bpm.console.client.model.ServerStatus;
import org.jboss.errai.workspaces.client.framework.Registry;

public class BootstrapAction extends AbstractRESTAction
{
  public static final String ID = BootstrapAction.class.getName();
  @SuppressWarnings("unused")
private ApplicationContext appContext;

  public BootstrapAction()
  {
    this.appContext = ((ApplicationContext)Registry.get(ApplicationContext.class));
  }

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    return URLBuilder.getInstance().getServerStatusURL();
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    ServerStatus status = JSOParser.parseStatus(response.getText());
    ServerPlugins.setStatus(status);
  }
}