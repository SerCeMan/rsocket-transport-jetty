package me.serce.rsocket.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class RsWebScoketServlet extends WebSocketServlet {
  private final RsWebSocketCreator creator;

  public RsWebScoketServlet(RsWebSocketCreator creator) {
    this.creator = creator;
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.setCreator(creator);
  }
}
