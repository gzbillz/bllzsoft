package org.jboss.bpm.console.client.process;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.gwt.mosaic.ui.client.table.AbstractScrollTable;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.LazyPanel;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.bpm.console.client.util.DOMUtil;
import org.jboss.errai.workspaces.client.framework.Registry;

public class InstanceDataView extends MosaicPanel
  implements ViewInterface, LazyPanel
{
  public static final String ID = InstanceDataView.class.getName();
  @SuppressWarnings("unused")
private Controller controller;
  @SuppressWarnings("rawtypes")
private ListBox listBox;
  @SuppressWarnings("unused")
private String instanceId;
  private boolean isInitialized;
  boolean isRiftsawInstance = false;

  public InstanceDataView()
  {
    setPadding(5);
    ApplicationContext appContext = (ApplicationContext)Registry.get(ApplicationContext.class);
    this.isRiftsawInstance = appContext.getConfig().getProfileName().equals("BPEL Console");
  }

  @SuppressWarnings("unchecked")
public void clear()
  {
    bindData(Collections.EMPTY_LIST);
  }

  @SuppressWarnings("unchecked")
public void initialize() {
    if (!this.isInitialized) {
      this.listBox = new ListBox<InstanceDataView.DataEntry>(new String[] { "Key", "XSD Type", "Java Type", "Value" });

      this.listBox.setColumnResizePolicy(AbstractScrollTable.ColumnResizePolicy.MULTI_CELL);

      this.listBox.setCellRenderer(new ListBox.CellRenderer<InstanceDataView.DataEntry>()
      {
        public void renderCell(final ListBox<InstanceDataView.DataEntry> listBox, final int row, final int column, final InstanceDataView.DataEntry item) {
          switch (column) {
          case 0:
            listBox.setText(row, column, item.key);
            break;
          case 1:
            listBox.setText(row, column, item.xsd);
            break;
          case 2:
            listBox.setText(row, column, item.java);
            break;
          case 3:
            if (InstanceDataView.this.isRiftsawInstance) {
              final JSONTree tree = new JSONTree(item.value);
              listBox.setWidget(row, column, tree);
            } else {
              listBox.setText(row, column, item.value);
            }
            break;
          default:
            throw new RuntimeException("Unexpected column size");
          }
        }
      });
      add(this.listBox);

      this.isInitialized = true;
    }
  }

  public boolean isInitialized() {
    return this.isInitialized;
  }

  public void setController(Controller controller) {
    this.controller = controller;
  }

  public void update(String instanceId, Document xml) {
    this.instanceId = instanceId;
    parseMessage(xml);
  }

  private void parseMessage(Document messageDom)
  {
    try
    {
      Node dataSetNode = messageDom.getElementsByTagName("dataset").item(0);

      List<Node> dataSetNodeChildren = DOMUtil.getChildElements(dataSetNode.getChildNodes());

      List<DataEntry> results = new ArrayList<DataEntry>();

      for (Node dataNode : dataSetNodeChildren) {
        DataEntry dataEntry = new DataEntry();
        NamedNodeMap dataNodeAttributes = dataNode.getAttributes();

        Node valueNode = (Node)DOMUtil.getChildElements(dataNode.getChildNodes()).get(0);

        NamedNodeMap valueNodeAttributes = valueNode.getAttributes();

        dataEntry.key = dataNodeAttributes.getNamedItem("key").getNodeValue();

        dataEntry.java = dataNodeAttributes.getNamedItem("javaType").getNodeValue();

        dataEntry.xsd = valueNodeAttributes.getNamedItem("xsi:type").getNodeValue();

        List<Node> valueChildElements = DOMUtil.getChildElements(valueNode.getChildNodes());

        if ((valueChildElements.isEmpty()) && (valueNode.hasChildNodes()) && (3 == valueNode.getChildNodes().item(0).getNodeType()))
        {
          dataEntry.value = valueNode.getFirstChild().getNodeValue();
        }
        else {
          dataEntry.value = "n/a";
        }

        results.add(dataEntry);
      }

      bindData(results);
    } catch (Throwable e) {
      ConsoleLog.error("Failed to parse XML document", e);
    }
  }

  private void bindData(List<DataEntry> data)
  {
    initialize();

    @SuppressWarnings("unchecked")
	DefaultListModel<DataEntry> model = (DefaultListModel<DataEntry>)this.listBox.getModel();

    model.clear();

    for (DataEntry d : data) {
      model.add(d);
    }

    layout();
  }

  private class DataEntry
  {
    String key;
    String xsd;
    String java;
    String value;

    private DataEntry()
    {
    }
  }
}