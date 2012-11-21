package org.jboss.bpm.console.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import java.util.List;
import org.gwt.mosaic.ui.client.Caption;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.WindowPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.client.security.SecurityService;
import org.jboss.errai.workspaces.client.framework.Registry;

/**
 * @description 登录主页
 * @class LoginView
 * @author 不了了之
 * @date 2012-11-17
 */
@SuppressWarnings("deprecation")
public class LoginView
  implements ViewInterface
{
  public static final String NAME = "loginView";
  private ConsoleConfig config;
  private Authentication auth;
  private WindowPanel window = null;
  private TextBox usernameInput;
  private PasswordTextBox passwordInput;
  public static final String[] KNOWN_ROLES = { "admin", "manager", "user" };

  private HTML messagePanel = new HTML("Authentication required");

  public LoginView()
  {
    this.config = ((ApplicationContext)Registry.get(ApplicationContext.class)).getConfig();
  }

  public void setController(Controller controller)
  {
  }

  public void display()
  {
    Authentication.logout(this.config);

    requestSessionID();
  }

  private void requestSessionID()
  {
    RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, this.config.getConsoleServerUrl() + "/rs/identity/sid");
    try
    {
      rb.sendRequest(null, new RequestCallback()
      {
        public void onResponseReceived(Request request, Response response)
        {
          ConsoleLog.debug("SID: " + response.getText());
          ConsoleLog.debug("Cookies: " + Cookies.getCookieNames());
          String sid = response.getText();

          LoginView.this.auth = new Authentication(LoginView.this.config, sid, URLBuilder.getInstance().getUserInRoleURL(LoginView.KNOWN_ROLES));

          LoginView.this.auth.setCallback(new Authentication.AuthCallback()
          {
            public void onLoginSuccess(Request request, Response response)
            {
              LoginView.this.usernameInput.setText("");
              LoginView.this.passwordInput.setText("");

              LoginView.this.window.hide();

              DeferredCommand.addCommand(new Command()
              {
                public void execute()
                {
                  DOM.getElementById("splash").getStyle().setProperty("zIndex", "1000");
                  DOM.getElementById("ui_loading").getStyle().setProperty("visibility", "visible");

                  GWT.runAsync(new RunAsyncCallback()
                  {
                    public void onFailure(Throwable throwable)
                    {
                      GWT.log("Code splitting failed", throwable);
                      MessageBox.error("Code splitting failed", throwable.getMessage());
                    }

                    public void onSuccess()
                    {
                      List<String> roles = LoginView.this.auth.getRolesAssigned();
                      StringBuilder roleString = new StringBuilder();
                      int index = 1;
                      for (String s : roles)
                      {
                        roleString.append(s);
                        if (index < roles.size())
                          roleString.append(",");
                        index++;
                      }

                      ((SecurityService)Registry.get(SecurityService.class)).setDeferredNotification(false);

                      ((MessageBuildSendableWithReply)MessageBuilder.createMessage().toSubject("AuthorizationListener").signalling().with(SecurityParts.Name, LoginView.this.auth.getUsername()).with(SecurityParts.Roles, roleString.toString()).noErrorHandling()).sendNowWith(ErraiBus.get());

                      Timer t = new Timer()
                      {
                        public void run()
                        {
                          DeferredCommand.addCommand(new Command()
                          {
                            public void execute()
                            {
                              DOM.getElementById("ui_loading").getStyle().setProperty("visibility", "hidden");
                              DOM.getElementById("splash").getStyle().setProperty("visibility", "hidden");
                            }
                          });
                        }
                      };
                      t.schedule(2000);
                    }
                  });
                }
              });
              LoginView.this.window = null;
            }

            public void onLoginFailed(Request request, Throwable t)
            {
              LoginView.this.usernameInput.setText("");
              LoginView.this.passwordInput.setText("");
              LoginView.this.messagePanel.setHTML("<div style='color:#CC0000;'>" + t.getMessage() + "</div>");
            }
          });
          Registry.set(Authentication.class, LoginView.this.auth);

          LoginView.this.createLayoutWindowPanel();
          LoginView.this.window.pack();
          LoginView.this.window.center();

          LoginView.this.usernameInput.setFocus(true);
        }

        public void onError(Request request, Throwable t)
        {
          ConsoleLog.error("Failed to initiate session", t);
        }
      });
    }
    catch (RequestException e)
    {
      ConsoleLog.error("Request error", e);
    }
  }

  /**
   * 登录窗口
   */
  private void createLayoutWindowPanel()
  {
    this.window = new WindowPanel(this.config.getProfileName(), false);
    Widget closeBtn = this.window.getHeader().getWidget(0, Caption.CaptionRegion.RIGHT);
    closeBtn.setVisible(false);
    this.window.setAnimationEnabled(false);

    MosaicPanel panel = new MosaicPanel();
    panel.addStyleName("bpm-login");

    createLayoutContent(panel);
    this.window.setWidget(panel);
  }

  private void createLayoutContent(MosaicPanel layoutPanel)
  {
    layoutPanel.setLayout(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    layoutPanel.setPadding(10);

    Widget form = createForm();

    Button submit = new Button("登录");
    submit.addClickHandler(new ClickHandler()
    {
      public void onClick(ClickEvent clickEvent)
      {
        LoginView.this.engageLogin();
      }
    });
//    HTML html = new HTML("Version: 2.3.5.Final");
    HTML html = new HTML("版本号：2.3.5.Final");
    html.setStyleName("bpm-login-info");

    MosaicPanel btnPanel = new MosaicPanel(new BoxLayout());
    btnPanel.add(html, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    btnPanel.add(submit);

    layoutPanel.add(this.messagePanel, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    layoutPanel.add(form, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
    layoutPanel.add(btnPanel, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
  }

  private Widget createForm()
  {
    MosaicPanel panel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    panel.setPadding(0);

    MosaicPanel box1 = new MosaicPanel(new BoxLayout());
    box1.setPadding(0);
    MosaicPanel box2 = new MosaicPanel(new BoxLayout());
    box2.setPadding(0);

    this.usernameInput = new TextBox();
    this.passwordInput = new PasswordTextBox();

    BoxLayoutData bld1 = new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL);
    bld1.setPreferredWidth("70px");

    box1.add(new Label("Username:"), bld1);
    box1.add(this.usernameInput);

    box2.add(new Label("Password:"), bld1);
    box2.add(this.passwordInput);

    this.passwordInput.addKeyboardListener(new KeyboardListener()
    {
      public void onKeyDown(Widget widget, char c, int i)
      {
      }

      public void onKeyPress(Widget widget, char c, int i)
      {
      }

      public void onKeyUp(Widget widget, char c, int i)
      {
        if (c == '\r')
        {
          LoginView.this.engageLogin();
        }
      }
    });
    panel.add(box1);
    panel.add(box2);
    return panel;
  }

  private void engageLogin()
  {
    requestProtectedResource();
  }

  private void requestProtectedResource()
  {
    RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, this.config.getConsoleServerUrl() + "/rs/identity/secure/sid");
    try
    {
      rb.sendRequest(null, new RequestCallback()
      {
        public void onResponseReceived(Request request, Response response)
        {
          ConsoleLog.debug("requestProtectedResource() HTTP " + response.getStatusCode());
          LoginView.this.auth.login(LoginView.this.getUsername(), LoginView.this.getPassword());
        }

        public void onError(Request request, Throwable t)
        {
          ConsoleLog.error("Failed to request protected resource", t);
        }
      });
    }
    catch (RequestException e)
    {
      ConsoleLog.error("Request error", e);
    }
  }

  private String getUsername()
  {
    return this.usernameInput.getText();
  }

  private String getPassword()
  {
    return this.passwordInput.getText();
  }
}