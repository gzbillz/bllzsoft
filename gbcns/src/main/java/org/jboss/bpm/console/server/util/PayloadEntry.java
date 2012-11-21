package org.jboss.bpm.console.server.util;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="data")
public class PayloadEntry
{
  private String key;
  private Object value;

  public PayloadEntry()
  {
  }

  public PayloadEntry(String key, Object value)
  {
    this.key = key;
    this.value = value;
  }

  @XmlAttribute(name="key")
  public String getKey()
  {
    return this.key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  @XmlElement(name="value")
  public Object getValue()
  {
    return this.value;
  }

  public void setValue(Object value)
  {
    this.value = value;
  }

  @XmlAttribute
  public String getJavaType()
  {
    return this.value.getClass().getName();
  }
}