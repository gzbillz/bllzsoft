package org.jboss.bpm.console.client.search;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;

@SuppressWarnings("deprecation")
public class SearchDefinitionView extends MosaicPanel
  implements ViewInterface
{
  @SuppressWarnings("unused")
private Controller controller;
  @SuppressWarnings("unused")
private ApplicationContext appContext;
  private SearchDelegate delegate;
  private SuggestBox suggestBox;
  private String selection = null;
  private SearchWindow parent;

  public SearchDefinitionView(ApplicationContext appContext, SearchDelegate delegate)
  {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));

    this.appContext = appContext;
    this.delegate = delegate;
    setPadding(5);

    add(new Label("Loading, please wait..."));
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  private MultiWordSuggestOracle createOracle(List<ProcessDefinitionRef> definitions)
  {
    MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

    for (ProcessDefinitionRef p : definitions)
    {
      oracle.add(p.getId());
    }

    return oracle;
  }

 public void update(List<ProcessDefinitionRef> definitions)
  {
    clear();
    this.selection = null;

    Label desc = new Label("Please enter a process definition ID");

    add(desc, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    this.suggestBox = new SuggestBox(createOracle(definitions));

    this.suggestBox.addEventHandler(new SuggestionHandler()
    {
      public void onSuggestionSelected(final SuggestionEvent suggestionEvent)
      {
        SearchDefinitionView.this.selection = suggestionEvent.getSelectedSuggestion().getReplacementString();
      }
    });
    add(this.suggestBox);

    Grid g = new Grid(2, 2);
    g.setWidget(0, 0, new Label("ID: "));
    g.setWidget(0, 1, this.suggestBox);

    Button button = new Button(this.delegate.getActionName(), new ClickListener()
    {
      public void onClick(final Widget widget)
      {
        if (SearchDefinitionView.this.selection != null)
        {
          SearchDefinitionView.this.delegate.handleResult(SearchDefinitionView.this.selection);
          SearchDefinitionView.this.parent.close();
        }
      }
    });
    g.setWidget(1, 1, button);
    add(g, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

    invalidate();
  }

  void setParent(SearchWindow window)
  {
    this.parent = window;
  }
}