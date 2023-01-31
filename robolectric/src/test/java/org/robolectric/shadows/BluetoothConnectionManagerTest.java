package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class BluetoothConnectionManagerTest {
  private static final String REMOTE_ADDRESS1 = "R-A-1";
  private static final String REMOTE_ADDRESS2 = "R-A-2";

  private final BluetoothConnectionManager manager = BluetoothConnectionManager.getInstance();

  @After
  public void tearDown() {
    manager.resetConnections();
  }

  @Test
  public void test_getInstance() {
    BluetoothConnectionManager a = BluetoothConnectionManager.getInstance();
    assertThat(a).isNotNull();
    assertThat(a).isEqualTo(manager);
    assertThat(a.isConnected(REMOTE_ADDRESS1)).isFalse();
    assertThat(manager.isConnected(REMOTE_ADDRESS1)).isFalse();
    a.registerGattClientConnection(REMOTE_ADDRESS1);
    assertThat(a.isConnected(REMOTE_ADDRESS1)).isTrue();
    assertThat(manager.isConnected(REMOTE_ADDRESS1)).isTrue();
  }

  @Test
  public void test_hasGattClientConnection() {
    this.manager.registerGattClientConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.hasGattClientConnection(REMOTE_ADDRESS1)).isTrue();
    assertThat(this.manager.hasGattClientConnection(REMOTE_ADDRESS2)).isFalse();
  }

  @Test
  public void test_hasGattClientConnection_multiple() {
    this.manager.registerGattClientConnection(REMOTE_ADDRESS1);
    this.manager.registerGattClientConnection(REMOTE_ADDRESS2);
    assertThat(this.manager.hasGattClientConnection(REMOTE_ADDRESS1)).isTrue();
    assertThat(this.manager.hasGattClientConnection(REMOTE_ADDRESS2)).isTrue();
  }

  @Test
  public void test_hasGattServerConnection() {
    this.manager.registerGattServerConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.hasGattServerConnection(REMOTE_ADDRESS1)).isTrue();
    assertThat(this.manager.hasGattServerConnection(REMOTE_ADDRESS2)).isFalse();
  }

  @Test
  public void test_hasGattServerConnection_multiple() {
    this.manager.registerGattServerConnection(REMOTE_ADDRESS1);
    this.manager.registerGattServerConnection(REMOTE_ADDRESS2);
    assertThat(this.manager.hasGattServerConnection(REMOTE_ADDRESS1)).isTrue();
    assertThat(this.manager.hasGattServerConnection(REMOTE_ADDRESS2)).isTrue();
  }

  @Test
  public void test_isNotConnected_noConnection() {
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isFalse();
  }

  @Test
  public void test_isConnected_gattServerConnection() {
    this.manager.registerGattServerConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isTrue();
  }

  @Test
  public void test_isConnected_gattClientConnection() {
    this.manager.registerGattClientConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isTrue();
  }

  @Test
  public void test_isConnected_gattClientandServerConnection() {
    this.manager.registerGattClientConnection(REMOTE_ADDRESS1);
    this.manager.registerGattServerConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isTrue();
  }

  @Test
  public void test_isNotConnected_unregistedGattClientConnection() {
    this.manager.registerGattClientConnection(REMOTE_ADDRESS1);
    this.manager.unregisterGattClientConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isFalse();
  }

  @Test
  public void test_isNotConnected_unregistedGattServerConnection() {
    this.manager.registerGattServerConnection(REMOTE_ADDRESS1);
    this.manager.unregisterGattServerConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isFalse();
  }

  @Test
  public void test_isConnected_gattClientConnection_unregistedGattServerConnection() {
    this.manager.registerGattClientConnection(REMOTE_ADDRESS1);
    this.manager.registerGattServerConnection(REMOTE_ADDRESS1);
    this.manager.unregisterGattServerConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isTrue();
  }

  @Test
  public void test_isConnected_gattServerConnection_unregistedGattClientConnection() {
    this.manager.registerGattServerConnection(REMOTE_ADDRESS1);
    this.manager.registerGattClientConnection(REMOTE_ADDRESS1);
    this.manager.unregisterGattClientConnection(REMOTE_ADDRESS1);
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isTrue();
  }

  @Test
  public void test_resetConnections() {
    this.manager.registerGattServerConnection(REMOTE_ADDRESS1);
    this.manager.registerGattClientConnection(REMOTE_ADDRESS2);
    this.manager.resetConnections();
    assertThat(this.manager.isConnected(REMOTE_ADDRESS1)).isFalse();
    assertThat(this.manager.isConnected(REMOTE_ADDRESS2)).isFalse();
  }
}
