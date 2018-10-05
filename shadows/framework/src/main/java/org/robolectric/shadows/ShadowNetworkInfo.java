package org.robolectric.shadows;

import android.net.NetworkInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(NetworkInfo.class)
public class ShadowNetworkInfo {
  private boolean isAvailable;
  private NetworkInfo.State state;
  private int connectionType;
  private int connectionSubType;
  private NetworkInfo.DetailedState detailedState;

  @Implementation
  protected static void __staticInitializer__() {}

  /** Allows developers to create a {@link NetworkInfo} instance for testing. */
  public static NetworkInfo newInstance(
      NetworkInfo.DetailedState detailedState,
      int type,
      int subType,
      boolean isAvailable,
      NetworkInfo.State state) {
    NetworkInfo networkInfo = Shadow.newInstanceOf(NetworkInfo.class);
    final ShadowNetworkInfo info = Shadow.extract(networkInfo);
    info.setConnectionType(type);
    info.setSubType(subType);
    info.setDetailedState(detailedState);
    info.setAvailableStatus(isAvailable);
    info.setConnectionStatus(state);
    return networkInfo;
  }

  @Implementation
  protected boolean isConnected() {
    return state == NetworkInfo.State.CONNECTED;
  }

  @Implementation
  protected boolean isConnectedOrConnecting() {
    return isConnected() || state == NetworkInfo.State.CONNECTING;
  }

  @Implementation
  protected NetworkInfo.State getState() {
    return state;
  }

  @Implementation
  protected NetworkInfo.DetailedState getDetailedState() {
    return detailedState;
  }

  @Implementation
  protected int getType() {
    return connectionType;
  }

  @Implementation
  protected int getSubtype() {
    return connectionSubType;
  }

  @Implementation
  protected boolean isAvailable() {
    return isAvailable;
  }

  /**
   * Sets up the return value of {@link #isAvailable()}.
   *
   * @param isAvailable the value that {@link #isAvailable()} will return.
   */
  public void setAvailableStatus(boolean isAvailable) {
    this.isAvailable = isAvailable;
  }

  /**
   * Sets up the return value of {@link #isConnectedOrConnecting()}, {@link #isConnected()}, and
   * {@link #getState()}. If the input is true, state will be {@link NetworkInfo.State#CONNECTED},
   * else it will be {@link NetworkInfo.State#DISCONNECTED}.
   *
   * @param isConnected the value that {@link #isConnectedOrConnecting()} and {@link #isConnected()}
   *     will return.
   * @deprecated use {@link #setConnectionStatus(NetworkInfo.State)} instead
   */
  @Deprecated
  public void setConnectionStatus(boolean isConnected) {
    setConnectionStatus(isConnected ? NetworkInfo.State.CONNECTED : NetworkInfo.State.DISCONNECTED);
  }

  /**
   * Sets up the return value of {@link #getState()}.
   *
   * @param state the value that {@link #getState()} will return.
   */
  public void setConnectionStatus(NetworkInfo.State state) {
    this.state = state;
  }

  /**
   * Sets up the return value of {@link #getType()}.
   *
   * @param connectionType the value that {@link #getType()} will return.
   */
  public void setConnectionType(int connectionType) {
    this.connectionType = connectionType;
  }

  public void setSubType(int subType) {
    this.connectionSubType = subType;
  }

  public void setDetailedState(NetworkInfo.DetailedState detailedState) {
    this.detailedState = detailedState;
  }
}
