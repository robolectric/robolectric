package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestActivity;

@RunWith(AndroidJUnit4.class)
public class ShadowWifiP2pManagerTest {

  private Context context;
  private WifiP2pManager manager;
  private ShadowWifiP2pManager shadowManager;
  @Mock private WifiP2pManager.ChannelListener mockListener;
  private WifiP2pManager.Channel channel;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    context = ApplicationProvider.getApplicationContext();
    manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
    shadowManager = shadowOf(manager);
    channel = manager.initialize(context, context.getMainLooper(), mockListener);
    assertThat(channel).isNotNull();
  }

  @Test
  public void createGroup_success() {
    TestActionListener testListener = new TestActionListener();
    manager.createGroup(channel, testListener);
    shadowMainLooper().idle();
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

    shadowMainLooper().pause();

    shadowManager.setNextActionFailure(WifiP2pManager.BUSY);
    manager.createGroup(channel, testListener);

    shadowMainLooper().idle();

    assertThat(testListener.success).isFalse();
    assertThat(testListener.reason).isEqualTo(WifiP2pManager.BUSY);
  }

  @Test
  public void clearActionFailure() {
    shadowManager.setNextActionFailure(WifiP2pManager.ERROR);

    TestActionListener testListener = new TestActionListener();
    manager.createGroup(channel, testListener);
    shadowMainLooper().idle();
    assertThat(testListener.success).isFalse();

    manager.createGroup(channel, testListener);
    shadowMainLooper().idle();
    assertThat(testListener.success).isTrue();
  }

  @Test
  public void removeGroup_success() {
    TestActionListener testListener = new TestActionListener();
    manager.removeGroup(channel, testListener);
    shadowMainLooper().idle();
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

    shadowManager.setNextActionFailure(WifiP2pManager.BUSY);
    manager.removeGroup(channel, testListener);
    shadowMainLooper().idle();

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
    shadowMainLooper().idle();

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

  @Test
  @Config(minSdk = O)
  public void wifiP2pManager_activityContextEnabled_retrievesSameGroupInfo() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");

    WifiP2pManager.Channel applicationChannel =
        manager.initialize(context, Looper.getMainLooper(), null);

    CountDownLatch latch = new CountDownLatch(2);
    final String[] applicationGroupNameHolder = new String[1];
    final String[] activityGroupNameHolder = new String[1];

    manager.requestGroupInfo(
        applicationChannel,
        group -> {
          if (group != null) {
            applicationGroupNameHolder[0] = group.getNetworkName();
          }
          latch.countDown();
        });

    Activity activity = null;
    try {
      activity = Robolectric.setupActivity(TestActivity.class);
      WifiP2pManager activityWifiP2pManager =
          (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        assertThat(manager).isNotSameInstanceAs(activityWifiP2pManager);
      } else {
        assertThat(manager).isSameInstanceAs(activityWifiP2pManager);
      }

      WifiP2pManager.Channel activityChannel =
          activityWifiP2pManager.initialize(activity, activity.getMainLooper(), null);

      activityWifiP2pManager.requestGroupInfo(
          activityChannel,
          group -> {
            if (group != null) {
              activityGroupNameHolder[0] = group.getNetworkName();
            }
            latch.countDown();
          });

      latch.await(5, TimeUnit.SECONDS); // Adjust timeout as necessary

      assertThat(applicationGroupNameHolder[0]).isEqualTo(activityGroupNameHolder[0]);
    } catch (InterruptedException e) {
      fail("Failed because of latch interrupt");
    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
