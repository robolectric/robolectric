package org.robolectric.shadows.testing;

import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import javax.annotation.Nullable;

/** A fake {@link ConnectionService} implementation for testing. */
public class TestConnectionService extends ConnectionService {

  /** Listens for calls to {@link TestConnectionService} methods. */
  public interface Listener {
    @Nullable
    default Connection onCreateIncomingConnection(
        PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
      return null;
    }

    default void onCreateIncomingConnectionFailed(
        PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {}

    @Nullable
    default Connection onCreateOutgoingConnection(
        PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
      return null;
    }

    default void onCreateOutgoingConnectionFailed(
        PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {}
  }

  private static Listener listener = new Listener() {};

  public static void setListener(Listener listener) {
    TestConnectionService.listener = listener == null ? new Listener() {} : listener;
  }

  @Override
  @Nullable
  public Connection onCreateIncomingConnection(
      PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
    return listener.onCreateIncomingConnection(connectionManagerPhoneAccount, request);
  }

  @Override
  public void onCreateIncomingConnectionFailed(
      PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
    listener.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request);
  }

  @Override
  @Nullable
  public Connection onCreateOutgoingConnection(
      PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
    return listener.onCreateOutgoingConnection(connectionManagerPhoneAccount, request);
  }

  @Override
  public void onCreateOutgoingConnectionFailed(
      PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
    listener.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
  }
}
