package org.jboss.bpm.console.server.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class Payload2XML
{
  public StringBuffer convert(String refId, Map<String, Object> javaPayload)
  {
    StringBuffer sb = new StringBuffer();
    try
    {
      List<Object> clz = new ArrayList<Object>(javaPayload.size() + 2);
      clz.add(PayloadCollection.class);
      clz.add(PayloadEntry.class);

      List<PayloadEntry> data = new ArrayList<PayloadEntry>();

      for (String key : javaPayload.keySet())
      {
        Object payload = javaPayload.get(key);
        clz.add(payload.getClass());
        data.add(new PayloadEntry(key, payload));
      }

      PayloadCollection dataset = new PayloadCollection(refId, data);
      JAXBContext jaxbContext = JAXBContext.newInstance((Class[])clz.toArray(new Class[0]));
      ByteArrayOutputStream bout = new ByteArrayOutputStream();

      Marshaller m = jaxbContext.createMarshaller();

      m.setProperty("jaxb.formatted.output", Boolean.TRUE);

      m.marshal(dataset, bout);
      sb.append(new String(bout.toByteArray()));
    }
    catch (JAXBException e)
    {
      throw new RuntimeException("Payload2XML conversion failed", e);
    }

    return sb;
  }
}