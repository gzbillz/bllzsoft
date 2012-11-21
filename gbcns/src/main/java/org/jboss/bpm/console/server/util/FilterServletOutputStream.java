package org.jboss.bpm.console.server.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;

public class FilterServletOutputStream extends ServletOutputStream
{
  private DataOutputStream stream;

  public FilterServletOutputStream(OutputStream output)
  {
    this.stream = new DataOutputStream(output);
  }

  public void write(int b) throws IOException {
    this.stream.write(b);
  }

  public void write(byte[] b) throws IOException {
    this.stream.write(b);
  }

  public void write(byte[] b, int off, int len) throws IOException
  {
    this.stream.write(b, off, len);
  }
}