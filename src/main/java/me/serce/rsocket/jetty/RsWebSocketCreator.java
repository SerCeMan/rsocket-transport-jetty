package me.serce.rsocket.jetty;

import io.rsocket.Closeable;
import io.rsocket.transport.ServerTransport;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import reactor.core.publisher.Mono;

public class RsWebSocketCreator implements WebSocketCreator, ServerTransport<Closeable> {
  private ConnectionAcceptor acceptor;

  @Override
  public Mono<Closeable> start(ConnectionAcceptor acceptor, int mtu) {
    this.acceptor = acceptor;
    return Mono.empty();
  }

  @Override
  public WebSocketListener createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
    return new RsWebSocket(req, resp, acceptor);
  }
}
