package org.jboss.bpm.console.client.engine;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.AbstractRESTAction;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.ModelCommands;
import org.jboss.bpm.console.client.common.ModelParts;
import org.jboss.bpm.console.client.model.DTOParser;
import org.jboss.bpm.console.client.model.DeploymentRef;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;

public class UpdateDeploymentsAction extends AbstractRESTAction
{
  public static final String ID = UpdateDeploymentsAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    return URLBuilder.getInstance().getDeploymentsUrl();
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  protected DataDriven getDataDriven(Controller controller)
  {
    return (DeploymentListView)controller.getView(DeploymentListView.ID);
  }

  @SuppressWarnings("unused")
public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    DeploymentListView view = (DeploymentListView)controller.getView(DeploymentListView.ID);

    if (view != null)
    {
      JSONValue json = JSONParser.parse(response.getText());
      List<DeploymentRef> deployments = DTOParser.parseDeploymentRefList(json);

      if (null == view) {
        throw new RuntimeException("View not initialized: " + DeploymentListView.ID);
      }
      view.update(new Object[] { deployments });

      String deploymentId = (String)event;
      if (deploymentId != null) {
        view.select(deploymentId);
      }

      ((MessageBuildSendableWithReply)MessageBuilder.createMessage().toSubject("appContext.model.listener").command(ModelCommands.HAS_BEEN_UPDATED).with(ModelParts.CLASS, "deploymentModel").noErrorHandling()).sendNowWith(ErraiBus.get());
    }
  }
}