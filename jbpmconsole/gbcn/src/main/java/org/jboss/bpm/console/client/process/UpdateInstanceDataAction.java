package org.jboss.bpm.console.client.process;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
import com.mvc4g.client.Controller;
import org.gwt.mosaic.ui.client.MessageBox;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.util.ConsoleLog;

public class UpdateInstanceDataAction extends AbstractRESTAction
{
  public static final String ID = UpdateInstanceDataAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    String id = (String)event;
    return URLBuilder.getInstance().getInstanceDataURL(id);
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    String id = (String)event;
    String xml = response.getText();
    Document messageDom = XMLParser.parse(xml);
    InstanceDataView view = (InstanceDataView)controller.getView(InstanceDataView.ID);
    view.update(id, messageDom);
  }

  protected void handleError(String url, Throwable t)
  {
    String message = t.getMessage();

    message = message.replaceFirst("HTTP \\d*: ", "");

    ConsoleLog.warn("Server reported following warning: " + message + " for url " + url);
    MessageBox.alert("Status information", message);
  }
}