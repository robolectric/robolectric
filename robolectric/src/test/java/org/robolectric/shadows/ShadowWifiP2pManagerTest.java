package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

@RunWith(AndroidJUnit4.class)
public class ShadowWifiP2pManagerTest {

  private WifiP2pManager manager;
  private ShadowWifiP2pManager shadowManager;
  @Mock private WifiP2pManager.ChannelListener mockListener;
  private WifiP2pManager.Channel channel;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Application context = ApplicationProvider.getApplicationContext();
    manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
    shadowManager = shadowOf(manager);
    channel = manager.initialize(context, context.getMainLooper(), mockListener);
    assertThat(channel).isNotNull();
  }

  @Test
  public void createGroup_success() {
    TestActionListener testListener = new TestActionListener();
    manager.createGroup(channel, testListener);
    assertThat(testListener.success).isTrue();
  }

  @Test
  public void createGroup_nullListener() {
    manager.createGroup(channel, null);

    // Should not fail with a null listener
  }

  @Test
  public void createGroup_fail() {
    TestActionListener testListener = new TestActionListener();

    RuntimeEnvironment.getMasterScheduler().pause();

    manager.createGroup(channel, testListener);
    shadowManager.setNextActionFailure(WifiP2pManager.BUSY);

    RuntimeEnvironment.getMasterScheduler().unPause();

    assertThat(testListener.success).isFalse();
    assertThat(testListener.reason).isEqualTo(WifiP2pManager.BUSY);
  }

  @Test
  public void clearActionFailure() {
    shadowManager.setNextActionFailure(WifiP2pManager.ERROR);

    TestActionListener testListener = new TestActionListener();
    manager.createGroup(channel, testListener);
    assertThat(testListener.success).isFalse();

    manager.createGroup(channel, testListener);
    assertThat(testListener.success).isTrue();
  }

  @Test
  public void removeGroup_success() {
    TestActionListener testListener = new TestActionListener();
    manager.removeGroup(channel, testListener);
    assertThat(testListener.success).isTrue();
  }

  @Test
  public void removeGroup_nullListener() {
    manager.removeGroup(channel, null);

    // Should not fail with a null listener
  }

  @Test
  public void removeGroup_failure() {
    TestActionListener testListener = new TestActionListener();

    RuntimeEnvironment.getMasterScheduler().pause();
    manager.removeGroup(channel, testListener);

    shadowManager.setNextActionFailure(WifiP2pManager.BUSY);
    RuntimeEnvironment.getMasterScheduler().unPause();

    assertThat(testListener.success).isFalse();
    assertThat(testListener.reason).isEqualTo(WifiP2pManager.BUSY);
  }

  @Test
  public void requestGroupInfo() {
    TestGroupInfoListener listener = new TestGroupInfoListener();

    WifiP2pGroup wifiP2pGroup = new WifiP2pGroup();
    shadowOf(wifiP2pGroup).setInterface("ssid");
    shadowOf(wifiP2pGroup).setPassphrase("passphrase");
    shadowOf(wifiP2pGroup).setNetworkName("networkname");

    shadowManager.setGroupInfo(channel, wifiP2pGroup);

    manager.requestGroupInfo(channel, listener);

    assertThat(listener.group.getNetworkName()).isEqualTo(wifiP2pGroup.getNetworkName());
    assertThat(listener.group.getInterface()).isEqualTo(wifiP2pGroup.getInterface());
    assertThat(listener.group.getPassphrase()).isEqualTo(wifiP2pGroup.getPassphrase());
  }

  @Test
  public void requestGroupInfo_nullListener() {
    WifiP2pGroup wifiP2pGroup = new WifiP2pGroup();
    shadowManager.setGroupInfo(channel, wifiP2pGroup);

    manager.requestGroupInfo(channel, null);

    // Should not fail with a null listener
  }

  private static class TestActionListener implements WifiP2pManager.ActionListener {
    private int reason;
    private boolean success;

    @Override
    public void onSuccess() {
      success = true;
    }

    @Override
    public void onFailure(int reason) {
      success = false;
      this.reason = reason;
    }
  }

  private static class TestGroupInfoListener implements WifiP2pManager.GroupInfoListener {
    private WifiP2pGroup group;

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
      this.group = group;
    }
  }
}
