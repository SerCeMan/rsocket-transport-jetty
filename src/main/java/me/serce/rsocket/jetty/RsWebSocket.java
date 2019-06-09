package me.serce.rsocket.jetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.internal.BaseDuplexConnection;
import io.rsocket.transport.ServerTransport.ConnectionAcceptor;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class RsWebSocket extends BaseDuplexConnection implements WebSocketListener {

  private final ServletUpgradeRequest req;
  private final ServletUpgradeResponse resp;
  private final ConnectionAcceptor acceptor;
  private final Flux<ByteBuf> received;
  private volatile FluxSink<ByteBuf> output;

  public RsWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp, ConnectionAcceptor acceptor) {
    this.req = req;
    this.resp = resp;
    this.acceptor = acceptor;
    this.received = Flux.create(sink -> { //
      output = sink;
    });
  }

  private volatile Session session;
  private RemoteEndpoint remote;

  private FluxSink<ByteBuf> getOutput() {
    if (output == null) {
      throw new IllegalStateException("Socket needs to be subscribed on");
    }
    return output;
  }

  @Override
  public void onWebSocketBinary(byte[] payload, int offset, int len) {
    ByteBuf buf = Unpooled.wrappedBuffer(payload, offset, len);
    getOutput().next(buf);
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    getOutput().complete();
    dispose();
  }

  @Override
  public void onWebSocketConnect(Session sess) {
    acceptor.apply(this).subscribe(v -> sess.close());
    this.session = sess;
    this.remote = sess.getRemote();
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    getOutput().error(cause);
  }

  @Override
  public void onWebSocketText(String message) {
    throw new UnsupportedOperationException("only binary is supported");
  }

  @Override
  protected void doOnClose() {
    if (session.isOpen()) {
      session.close();
    }
  }

  @Override
  public Mono<Void> send(Publisher<ByteBuf> frames) {
    return Flux.from(frames)
        .map(byteBuf -> {
          try {
            ByteBuffer buf = byteBuf.nioBuffer();
            remote.sendBytes(buf);
            return buf;
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        })
        .then();
  }

  @Override
  public Flux<ByteBuf> receive() {
    return received;
  }
}
