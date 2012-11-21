package org.jboss.bpm.console.client.process;

import com.mvc4g.client.ActionInterface;
import com.mvc4g.client.Controller;
import java.util.List;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;

public class UpdateHistoryDefinitionAction
  implements ActionInterface
{
  public static final String ID = UpdateHistoryDefinitionAction.class.getName();

  private MessageBus bus = ErraiBus.get();
  private Controller controller;

  public UpdateHistoryDefinitionAction()
  {
    this.bus.subscribe(ID, new MessageCallback()
    {
      public void callback(Message message)
      {
        DefinitionHistoryListView definitionList = (DefinitionHistoryListView)UpdateHistoryDefinitionAction.this.controller.getView(DefinitionHistoryListView.ID);

        ProcessDefinitionRef selectedDefinition = definitionList.getSelection();
        assert (selectedDefinition != null);

        HistoryInstanceListView view = (HistoryInstanceListView)UpdateHistoryDefinitionAction.this.controller.getView(HistoryInstanceListView.ID);

        if (view != null)
        {
          @SuppressWarnings("unchecked")
		List<Object> results = (List<Object>)message.get(List.class, "INSTANCE_LIST");
          view.update(new Object[] { selectedDefinition, results });
        }
      }
    });
  }

  public void execute(Controller controller, Object o)
  {
    this.controller = controller;

    ProcessDefinitionRef def = (ProcessDefinitionRef)o;

    ((MessageBuildSendableWithReply)MessageBuilder.createMessage().toSubject("JBPM_HISTORY_SERVICE").command("GET_FINISHED_PROCESS_INSTANCES").with(MessageParts.ReplyTo, ID).with("PROCESS_DEFINITION_ID", def.getId()).noErrorHandling()).sendNowWith(this.bus);
  }
}