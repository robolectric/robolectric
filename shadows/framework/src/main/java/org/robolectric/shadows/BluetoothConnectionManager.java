package org.robolectric.shadows;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages remote address connections for {@link ShadowBluetoothGatt} and {@link
 * ShadowBluetoothGattServer}.
 */
final class BluetoothConnectionManager {

  private static volatile BluetoothConnectionManager instance;

  /** Connection metadata for Gatt Server and Client connections. */
  private static class BluetoothConnectionMetadata {
    boolean hasGattClientConnection = false;
    boolean hasGattServerConnection = false;

    void setHasGattClientConnection(boolean hasGattClientConnection) {
      this.hasGattClientConnection = hasGattClientConnection;
    }

    void setHasGattServerConnection(boolean hasGattServerConnection) {
      this.hasGattServerConnection = hasGattServerConnection;
    }

    boolean hasGattClientConnection() {
      return hasGattClientConnection;
    }

    boolean hasGattServerConnection() {
      return hasGattServerConnection;
    }

    boolean isConnected() {
      return hasGattClientConnection || hasGattServerConnection;
    }
  }

  private BluetoothConnectionManager() {}

  static BluetoothConnectionManager getInstance() {
    if (instance == null) {
      synchronized (BluetoothConnectionManager.class) {
        if (instance == null) {
          instance = new BluetoothConnectionManager();
        }
      }
    }
    return instance;
  }

  /**
   * Map representing remote address connections, mapping a remote address to a {@link
   * BluetoothConnectionMetadata}.
   */
  private final Map<String, BluetoothConnectionMetadata> remoteAddressConnectionMap =
      new HashMap<>();

  /**
   * Register a Gatt Client Connection. Intended for use by {@link
   * ShadowBluetoothGatt#notifyConnection} when simulating a successful Gatt Client Connection.
   */
  void registerGattClientConnection(String remoteAddress) {
    if (!remoteAddressConnectionMap.containsKey(remoteAddress)) {
      remoteAddressConnectionMap.put(remoteAddress, new BluetoothConnectionMetadata());
    }
    remoteAddressConnectionMap.get(remoteAddress).setHasGattClientConnection(true);
  }

  /**
   * Unregister a Gatt Client Connection. Intended for use by {@link
   * ShadowBluetoothGatt#notifyDisconnection} when simulating a successful Gatt client
   * disconnection.
   */
  void unregisterGattClientConnection(String remoteAddress) {
    if (remoteAddressConnectionMap.containsKey(remoteAddress)) {
      remoteAddressConnectionMap.get(remoteAddress).setHasGattClientConnection(false);
    }
  }

  /**
   * Register a Gatt Server Connection. Intended for use by {@link
   * ShadowBluetoothGattServer#notifyConnection} when simulating a successful Gatt server
   * connection.
   */
  void registerGattServerConnection(String remoteAddress) {
    if (!remoteAddressConnectionMap.containsKey(remoteAddress)) {
      remoteAddressConnectionMap.put(remoteAddress, new BluetoothConnectionMetadata());
    }
    remoteAddressConnectionMap.get(remoteAddress).setHasGattServerConnection(true);
  }

  /**
   * Unregister a Gatt Server Connection. Intended for use by {@link
   * ShadowBluetoothGattServer#notifyDisconnection} when simulating a successful Gatt server
   * disconnection.
   */
  void unregisterGattServerConnection(String remoteAddress) {
    if (remoteAddressConnectionMap.containsKey(remoteAddress)) {
      remoteAddressConnectionMap.get(remoteAddress).setHasGattServerConnection(false);
    }
  }

  /**
   * Returns true if remote address has an active gatt client connection.
   *
   * @param remoteAddress remote address
   */
  boolean hasGattClientConnection(String remoteAddress) {
    return remoteAddressConnectionMap.containsKey(remoteAddress)
        && remoteAddressConnectionMap.get(remoteAddress).hasGattClientConnection();
  }

  /**
   * Returns true if remote address has an active gatt server connection.
   *
   * @param remoteAddress remote address
   */
  boolean hasGattServerConnection(String remoteAddress) {
    return remoteAddressConnectionMap.containsKey(remoteAddress)
        && remoteAddressConnectionMap.get(remoteAddress).hasGattServerConnection();
  }

  /**
   * Returns true if remote address has an active connection.
   *
   * @param remoteAddress remote address
   */
  boolean isConnected(String remoteAddress) {
    return remoteAddressConnectionMap.containsKey(remoteAddress)
        && remoteAddressConnectionMap.get(remoteAddress).isConnected();
  }

  /** Clears all connection information */
  void resetConnections() {
    this.remoteAddressConnectionMap.clear();
  }
}
