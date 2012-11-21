package org.jboss.bpm.console.server.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class GWTJsonFilter
  implements Filter
{
  public FilterConfig filterConfig;

  public void init(FilterConfig filterConfig)
    throws ServletException
  {
    this.filterConfig = filterConfig;
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    boolean isSOPCallback = false;

    if (request.getParameter("callback") != null) {
      isSOPCallback = true;
    }

    OutputStream out = response.getOutputStream();
    BufferedResponseWrapper wrapper = new BufferedResponseWrapper((HttpServletResponse)response);

    chain.doFilter(request, wrapper);

    String contentType = response.getContentType() != null ? response.getContentType() : "application/octet-stream";
    boolean isJSONEncoding = contentType.equals("application/json");
    StringBuffer sb = null;
    if (isJSONEncoding)
    {
      String payload = new String(wrapper.getData());
      String gwtextFriendly = trimPayload(payload);

      sb = new StringBuffer();

      if (isSOPCallback)
      {
        sb.append(request.getParameter("callback"));
        sb.append("(");
      }

      sb.append(gwtextFriendly);

      if (isSOPCallback)
      {
        sb.append(");");
      }

    }

    if (sb != null)
      out.write(sb.toString().getBytes());
    else {
      out.write(wrapper.getData());
    }
    out.flush();
    out.close();
  }

  private String trimPayload(String payload)
  {
    String s = payload;
    if (s.startsWith("{\"wrapper\":"))
    {
      s = payload.substring("{\"wrapper\":".length(), payload.lastIndexOf("}"));
    }
    return s;
  }

  public void destroy()
  {
  }
}