package org.jboss.bpm.console.client.process;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;

public class UpdateHistoryInstanceAction
  implements ActionInterface
{
  public static final String ID = UpdateHistoryInstanceAction.class.getName();

  private MessageBus bus = ErraiBus.get();
  private Controller controller;

  public UpdateHistoryInstanceAction()
  {
    this.bus.subscribe(ID, new MessageCallback()
    {
      public void callback(Message message)
      {
        HistoryInstanceListView view = (HistoryInstanceListView)UpdateHistoryInstanceAction.this.controller.getView(HistoryInstanceListView.ID);

        if (view != null)
        {
          @SuppressWarnings("unchecked")
		List<Object> records = (List<Object>)message.get(List.class, "HISTORY_RECORDS");

          view.update(new Object[] { records });
        }
      }
    });
  }

  public void execute(Controller controller, Object o)
  {
    this.controller = controller;

    String instanceId = (String)o;

    ((MessageBuildSendableWithReply)MessageBuilder.createMessage().toSubject("JBPM_HISTORY_SERVICE").command("GET_PROCESS_INSTANCE_HISTORY").with(MessageParts.ReplyTo, ID).with("PROCESS_INSTANCE_ID", instanceId).noErrorHandling()).sendNowWith(this.bus);
  }
}