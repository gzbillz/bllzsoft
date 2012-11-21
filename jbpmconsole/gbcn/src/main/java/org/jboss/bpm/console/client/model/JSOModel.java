package org.jboss.bpm.console.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.jboss.bpm.console.client.util.SimpleDateFormat;

public abstract class JSOModel extends JavaScriptObject
{
  public static native JSOModel create();

  public static native JSOModel fromJson(String paramString);

  public static native JsArray<JSOModel> arrayFromJson(String paramString);

  public final native boolean hasKey(String paramString);

  public final native JsArrayString keys();

  @Deprecated
  public final Set<String> keySet()
  {
    JsArrayString array = keys();
    Set<String> set = new HashSet<String>();
    for (int i = 0; i < array.length(); i++) {
      set.add(array.get(i));
    }
    return set;
  }

  public final native String get(String paramString);

  public final native String get(String paramString1, String paramString2);

  public final native void set(String paramString1, String paramString2);

  public final int getInt(String key)
  {
    return Integer.parseInt(get(key));
  }

  public final boolean getBoolean(String key) {
    return Boolean.parseBoolean(get(key));
  }

  public final native JSOModel getObject(String paramString);

  public final native JsArray<JSOModel> getArray(String paramString);

  public final long getLong(String key)
  {
    return Long.valueOf(get(key)).longValue();
  }

  public final Date getDate(String key)
  {
    Date result = null;
    String value = get(key);
    if (!isNull(value))
    {
      SimpleDateFormat df = new SimpleDateFormat();
      result = df.parse(value);
    }

    return result;
  }

  public final Date getDate(String key, Date fallback)
  {
    Date date = getDate(key);
    return date != null ? date : fallback;
  }

  private final boolean isNull(String val)
  {
    return ((val != null) && ("null".equals(val))) || ("undefined".equals(val));
  }
}