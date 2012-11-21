package org.jboss.bpm.console.client.report;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwt.mosaic.ui.client.layout.GridLayout;
import org.gwt.mosaic.ui.client.layout.GridLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.report.model.ReportParameter;
import org.jboss.bpm.report.model.ReportReference;
import org.jboss.errai.workspaces.client.framework.Preferences;

public class ReportParameterForm extends MosaicPanel
{
  private List<InputField> fields = new ArrayList<InputField>();
  private Preferences prefs;

  public ReportParameterForm(ReportReference reportReference, ReportParamCallback callback)
  {
    this.prefs = ((Preferences)GWT.create(Preferences.class));
    add(getFormPanel(reportReference, callback));
  }

  private Widget getFormPanel(ReportReference reportRef, ReportParamCallback callback)
  {
    MosaicPanel p = new MosaicPanel();
    p.setPadding(5);
    p.add(createForm(reportRef, callback));
    return p;
  }

  private MosaicPanel createForm(final ReportReference reportRef, final ReportParamCallback callback)
  {
    boolean hasParameters = reportRef.getParameterMetaData().size() > 0;
    int numRows = hasParameters ? reportRef.getParameterMetaData().size() + 1 : 2;

    MosaicPanel form = new MosaicPanel(new GridLayout(2, numRows));

    Button createBtn = new Button("Create Report", new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        Map<String,String> values = new HashMap<String, String>();
        for (ReportParameterForm.InputField field : ReportParameterForm.this.fields)
        {
          values.put(field.id, field.getValue());
        }

        if (!values.isEmpty())
        {
          ReportParameterForm.this.writePrefs(values, reportRef);
        }

        callback.onSumbit(values);
      }
    });
    Map<String,String> preferenceValues = readPrefs(reportRef);

    for (final ReportParameter reportParam : reportRef.getParameterMetaData())
    {
      String promptText = reportParam.getPromptText() != null ? reportParam.getPromptText() : reportParam.getName();
      String helpText = reportParam.getHelptext() != null ? reportParam.getHelptext() : "";

      final TextBox textBox = new TextBox();
      String prefValue = (String)preferenceValues.get(reportParam.getName());
      if (prefValue != null) {
        textBox.setText(prefValue);
      }

      InputField field = new InputField(textBox, reportParam)
      {
        String getValue()
        {
          return textBox.getText();
        }
      };
      this.fields.add(field);

      form.add(new HTML("<b>" + promptText + "</b><br/>" + helpText));
      form.add(textBox);
    }

    if (!hasParameters)
    {
      form.add(new HTML("This report doesn't require any paramters."), new GridLayoutData(2, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP));
    }

    form.add(new HTML(""));
    form.add(createBtn, new GridLayoutData(HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_BOTTOM));

    return form;
  }

  private void writePrefs(Map<String, String> values, ReportReference reportRef)
  {
    String name = reportRef.getTitle().replaceAll(" ", "_");
    String prefKey = "bpm-form-" + name;
    StringBuffer sb = new StringBuffer();
    int i = 1;
    for (String key : values.keySet())
    {
      sb.append(key).append("=").append((String)values.get(key));
      if (i < values.keySet().size())
        sb.append(",");
      i++;
    }

    this.prefs.set(prefKey, sb.toString());
  }

  private Map<String, String> readPrefs(ReportReference reportRef)
  {
    Map<String, String> values = new HashMap<String, String>();
    String name = reportRef.getTitle().replaceAll(" ", "_");
    String prefKey = "bpm-form-" + name;

    if (this.prefs.has(prefKey))
    {
      String prefValue = this.prefs.get(prefKey);
      String[] tokens = prefValue.split(",");
      for (int i = 0; i < tokens.length; i++)
      {
        String[] tuple = tokens[i].split("=");
        values.put(tuple[0], tuple[1]);
      }
    }

    return values;
  }
  private class InputField {
     String id;

    @SuppressWarnings("unused")
	private InputField() {
    }
    public InputField(TextBox textBox, ReportParameter reportParam) {
		// TODO Auto-generated constructor stub
	}
	String getValue() {
      throw new IllegalArgumentException("Override this method");
    }
  }
}