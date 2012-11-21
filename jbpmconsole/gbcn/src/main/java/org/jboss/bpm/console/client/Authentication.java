package org.jboss.bpm.console.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.gwt.mosaic.ui.client.MessageBox;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.bpm.console.client.util.JSONWalk;

public class Authentication
{
  private AuthCallback callback;
  private List<String> rolesAssigned = new ArrayList<String>();
  private String sid;
  private String username;
  private String password;
  private ConsoleConfig config;
  private String rolesUrl;
  private Date loggedInSince;

  public Authentication(ConsoleConfig config, String sessionID, String rolesUrl)
  {
    this.config = config;
    this.sid = sessionID;
    this.rolesUrl = rolesUrl;
    this.loggedInSince = new Date();
  }

  public String getSid()
  {
    return this.sid;
  }

  public void login(String user, String pass)
  {
    this.username = user;
    this.password = pass;

    String formAction = this.config.getConsoleServerUrl() + "/rs/identity/secure/j_security_check";
    RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, formAction);
    rb.setHeader("Content-Type", "application/x-www-form-urlencoded");
    try
    {
      rb.sendRequest("j_username=" + user + "&j_password=" + pass, new RequestCallback()
      {
        public void onResponseReceived(Request request, Response response)
        {
          ConsoleLog.debug("postLoginCredentials() HTTP " + response.getStatusCode());

          if (response.getText().indexOf("HTTP 401") != -1)
          {
            if (Authentication.this.callback != null)
              Authentication.this.callback.onLoginFailed(request, new Exception("Authentication failed. Please try again"));
            else
              throw new RuntimeException("Unknown exception upon login attempt");
          }
          else if (response.getText().indexOf("403") != -1)
          {
            Authentication.logout(Authentication.this.config);
            if (Authentication.this.callback != null)
              Authentication.this.callback.onLoginFailed(request, new Exception("You are not authorized to use this application"));
            else
              throw new RuntimeException("Unknown exception upon login attempt");
          }
          else if (response.getStatusCode() == 200)
          {
            DeferredCommand.addCommand(new Command()
            {
              public void execute()
              {
                Authentication.this.requestAssignedRoles();
              }
            });
          }
        }

        public void onError(Request request, Throwable t)
        {
          if (Authentication.this.callback != null)
            Authentication.this.callback.onLoginFailed(request, new Exception("Authentication failed"));
          else {
            throw new RuntimeException("Unknown exception upon login attempt");
          }
        }
      });
    }
    catch (RequestException e)
    {
      ConsoleLog.error("Request error", e);
    }
  }

  public Date getLoggedInSince()
  {
    return this.loggedInSince;
  }

  private void requestAssignedRoles()
  {
    RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, this.rolesUrl);

    ConsoleLog.debug("Request roles: " + rb.getUrl());
    try
    {
      rb.sendRequest(null, new RequestCallback()
      {
        public void onResponseReceived(Request request, Response response)
        {
          ConsoleLog.debug("requestAssignedRoles() HTTP " + response.getStatusCode());

          if (200 == response.getStatusCode())
          {
            Authentication.this.rolesAssigned = Authentication.parseRolesAssigned(response.getText());
            if (Authentication.this.callback != null) Authentication.this.callback.onLoginSuccess(request, response);
          }
          else
          {
            onError(request, new Exception(response.getText()));
          }
        }

        public void onError(Request request, Throwable t)
        {
          if (Authentication.this.callback != null)
            Authentication.this.callback.onLoginFailed(request, t);
          else {
            throw new RuntimeException("Unknown exception upon login attempt", t);
          }
        }

      });
    }
    catch (RequestException e1)
    {
      throw new RuntimeException("Unknown error upon login attempt", e1);
    }
  }

  public void setCallback(AuthCallback callback)
  {
    this.callback = callback;
  }

  private native void reload();

  public static void logout(ConsoleConfig conf)
  {
    RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, conf.getConsoleServerUrl() + "/rs/identity/sid/invalidate");
    try
    {
      rb.sendRequest(null, new RequestCallback()
      {
        public void onResponseReceived(Request request, Response response)
        {
          ConsoleLog.debug("logout() HTTP " + response.getStatusCode());

          if (response.getStatusCode() != 200)
          {
            ConsoleLog.error(response.getText());
          }
        }

        public void onError(Request request, Throwable t)
        {
          ConsoleLog.error("Failed to invalidate session", t);
        }
      });
    }
    catch (RequestException e)
    {
      ConsoleLog.error("Request error", e);
    }
  }

  public void logoutAndReload()
  {
    RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, this.config.getConsoleServerUrl() + "/rs/identity/sid/invalidate");
    try
    {
      rb.sendRequest(null, new RequestCallback()
      {
        public void onResponseReceived(Request request, Response response)
        {
          ConsoleLog.debug("logoutAndReload() HTTP " + response.getStatusCode());
          Authentication.this.resetState();
          Authentication.this.reload();
        }

        public void onError(Request request, Throwable t)
        {
          ConsoleLog.error("Failed to invalidate session", t);
        }
      });
    }
    catch (RequestException e)
    {
      ConsoleLog.error("Request error", e);
    }
  }

  private void resetState()
  {
    this.sid = null;
    this.username = null;
    this.password = null;
    this.rolesAssigned = new ArrayList<String>();
    this.loggedInSince = null;
  }

  public void handleSessionTimeout()
  {
    MessageBox.confirm("Session expired", "Please login again", new MessageBox.ConfirmationCallback()
    {
      public void onResult(boolean b)
      {
        Authentication.this.logoutAndReload();
      }
    });
  }

  public List<String> getRolesAssigned()
  {
    return this.rolesAssigned;
  }

  public String getUsername()
  {
    return this.username;
  }

  public String getPassword()
  {
    return this.password;
  }

  public static List<String> parseRolesAssigned(String json)
  {
    List<String> roles = new ArrayList<String>();

    JSONValue root = JSONParser.parse(json);
    JSONArray array = JSONWalk.on(root).next("roles").asArray();

    for (int i = 0; i < array.size(); i++)
    {
      JSONObject item = array.get(i).isObject();
      boolean assigned = JSONWalk.on(item).next("assigned").asBool();
      String roleName = JSONWalk.on(item).next("role").asString();

      if (assigned)
      {
        roles.add(roleName);
      }
    }

    return roles;
  }

  public static abstract interface AuthCallback
  {
    public abstract void onLoginSuccess(Request paramRequest, Response paramResponse);

    public abstract void onLoginFailed(Request paramRequest, Throwable paramThrowable);
  }
}