package org.jboss.bpm.console.server.util;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dataset")
public class PayloadCollection
{
  List<PayloadEntry> payloadEntries;
  String ref;

  public PayloadCollection()
  {
  }

  public PayloadCollection(String ref, List<PayloadEntry> payloadEntries)
  {
    this.ref = ref;
    this.payloadEntries = payloadEntries;
  }

  @XmlElement(name="data")
  public List<PayloadEntry> getPayload()
  {
    return this.payloadEntries;
  }

  public void setPayload(List<PayloadEntry> payloadEntries)
  {
    this.payloadEntries = payloadEntries;
  }

  @XmlAttribute
  public String getRef()
  {
    return this.ref;
  }

  public void setRef(String ref)
  {
    this.ref = ref;
  }
}