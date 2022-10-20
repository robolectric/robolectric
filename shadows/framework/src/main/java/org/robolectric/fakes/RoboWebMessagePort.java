package org.robolectric.fakes;

import android.os.Handler;
import android.webkit.WebMessage;
import android.webkit.WebMessagePort;
import android.webkit.WebMessagePort.WebMessageCallback;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/** Robolectric implementation of {@link WebMessagePort}. */
public class RoboWebMessagePort extends WebMessagePort {
  private final List<String> receivedMessages = Collections.synchronizedList(new ArrayList<>());
  // The connected port receives all messages this port sends. This port receives messages sent by
  // the connected port.
  private RoboWebMessagePort connectedPort;
  private WebMessageCallback callback;
  private boolean closed = false;

  public static RoboWebMessagePort[] createPair() {
    RoboWebMessagePort portA = new RoboWebMessagePort();
    RoboWebMessagePort portB = new RoboWebMessagePort();

    portA.setConnectedPort(portB);
    portB.setConnectedPort(portA);

    return new RoboWebMessagePort[] {portA, portB};
  }

  @Override
  public void postMessage(WebMessage message) {
    if (closed || connectedPort == null) {
      return;
    }

    String data = message.getData();
    if (data == null) {
      return;
    }

    connectedPort.receivedMessages.add(data);
    if (connectedPort.callback != null) {
      connectedPort.callback.onMessage(connectedPort, message);
    }
  }

  @Override
  public void setWebMessageCallback(WebMessagePort.WebMessageCallback callback) {
    setWebMessageCallback(callback, null);
  }

  @Override
  public void setWebMessageCallback(
      WebMessagePort.WebMessageCallback callback, @Nullable Handler handler) {
    this.callback = callback;
  }

  /**
   * Links another port to this port. After set, messages which sent from this port will arrive at
   * the connected one.
   */
  public void setConnectedPort(@Nullable RoboWebMessagePort port) {
    this.connectedPort = port;
  }

  public RoboWebMessagePort getConnectedPort() {
    return this.connectedPort;
  }

  public WebMessageCallback getWebMessageCallback() {
    return this.callback;
  }

  /** Returns the list of all messages sent to its connected ports. */
  public ImmutableList<String> getOutgoingMessages() {
    return ImmutableList.copyOf(getConnectedPort().receivedMessages);
  }

  /** Returns the list of all messages received from its connected ports. */
  public ImmutableList<String> getReceivedMessages() {
    return ImmutableList.copyOf(receivedMessages);
  }

  @Override
  public void close() {
    this.closed = true;
  }

  public boolean isClosed() {
    return this.closed;
  }
}
