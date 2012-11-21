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
import org.jboss.bpm.console.client.model.DTOParser;
import org.jboss.bpm.console.client.model.JobRef;

public class UpdateJobsAction extends AbstractRESTAction
{
  public static final String ID = UpdateJobsAction.class.getName();

  public String getId()
  {
    return ID;
  }

  public String getUrl(Object event)
  {
    return URLBuilder.getInstance().getJobsUrl();
  }

  public RequestBuilder.Method getRequestMethod()
  {
    return RequestBuilder.GET;
  }

  protected DataDriven getDataDriven(Controller controller)
  {
    return (JobListView)controller.getView(JobListView.ID);
  }

  public void handleSuccessfulResponse(Controller controller, Object event, Response response)
  {
    JSONValue json = JSONParser.parse(response.getText());
    List<JobRef> jobs = DTOParser.parseJobRefList(json);
    JobListView view = (JobListView)controller.getView(JobListView.ID);
    view.update(new Object[] { jobs });
  }
}