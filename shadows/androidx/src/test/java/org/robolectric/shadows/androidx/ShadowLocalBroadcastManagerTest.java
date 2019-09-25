package org.robolectric.shadows.androidx;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class ShadowLocalBroadcastManagerTest {
  private List<String> transcript = new ArrayList<>();

  @Test
  public void shouldGetInstance() throws Exception {
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    assertNotNull(instance);
    assertSame(instance, LocalBroadcastManager.getInstance(RuntimeEnvironment.application));
  }

  @Test
  public void shouldSendBroadcasts() throws Exception {
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    final boolean[] called = new boolean[1];
    final BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        called[0] = true;
      }
    };
    instance.registerReceiver(receiver, new IntentFilter("com.foo"));

    instance.sendBroadcast(new Intent("com.bar"));
    shadowMainLooper().idle();
    assertFalse(called[0]);
    instance.sendBroadcast(new Intent("com.foo"));
    shadowMainLooper().idle();
    assertTrue(called[0]);
  }
  
  @Test
  public void shouldSendBroadcastsWithDataScheme() throws Exception {
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    final boolean[] called = new boolean[1];
    final BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        called[0] = true;
      }
    };
    IntentFilter intentFilter = new IntentFilter("com.foo");
    intentFilter.addDataScheme("http");
    instance.registerReceiver(receiver, intentFilter);
    
    instance.sendBroadcast(new Intent("com.foo", Uri.parse("ftp://robolectric.org")));
    shadowMainLooper().idle();
    assertFalse(called[0]);
    instance.sendBroadcast(new Intent("com.foo", Uri.parse("http://robolectric.org")));
    shadowMainLooper().idle();
    assertTrue(called[0]);
  }

  @Test
  public void shouldUnregisterReceiver() throws Exception {
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    final boolean[] called = new boolean[1];
    final BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        called[0] = true;
      }
    };
    instance.registerReceiver(receiver, new IntentFilter("com.foo"));
    instance.unregisterReceiver(receiver);
    instance.sendBroadcast(new Intent("com.foo"));
    shadowMainLooper().idle();
    assertFalse(called[0]);
  }

  @Test
  public void testGetBroadcastIntents() throws Exception {
    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    broadcastManager.registerReceiver(new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        transcript.add("got intent " + intent.getAction());
      }
    }, IntentFilter.create("foo", "application/blatz"));

    Intent intent1 = new Intent("foo").setType("application/blatz");
    broadcastManager.sendBroadcast(intent1);
    shadowMainLooper().idle();
    Intent intent2 = new Intent("bar").setType("application/blatz");
    broadcastManager.sendBroadcast(intent2);
    shadowMainLooper().idle();

    assertThat(transcript).containsExactly("got intent foo");
  }

  @Test
  public void testGetRegisteredBroadcastReceivers() throws Exception {
    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    ShadowLocalBroadcastManager shadowLocalBroadcastManager = shadowBroadcast(broadcastManager);
    assertEquals(0, shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().size());

    BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {}
    };
    IntentFilter filter = new IntentFilter("foo");

    broadcastManager.registerReceiver(receiver, filter);

    assertEquals(1, shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().size());
    ShadowLocalBroadcastManager.Wrapper capturedWrapper = shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().get(0);
    assertEquals(receiver, capturedWrapper.broadcastReceiver);
    assertEquals(filter, capturedWrapper.intentFilter);

    broadcastManager.unregisterReceiver(receiver);
    assertEquals(0, shadowLocalBroadcastManager.getRegisteredBroadcastReceivers().size());
  }

  @Test
  public void testGetSentBroadcastIntents() throws Exception {
    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    ShadowLocalBroadcastManager shadowLocalBroadcastManager = shadowBroadcast(broadcastManager);
    assertEquals(0, shadowLocalBroadcastManager.getSentBroadcastIntents().size());

    Intent broadcastIntent = new Intent("foo");
    broadcastManager.sendBroadcast(broadcastIntent);
    shadowMainLooper().idle();

    assertEquals(1, shadowLocalBroadcastManager.getSentBroadcastIntents().size());
    assertEquals(broadcastIntent, shadowLocalBroadcastManager.getSentBroadcastIntents().get(0));
  }

  private ShadowLocalBroadcastManager shadowBroadcast(LocalBroadcastManager localBroadcastManager) {
    return (ShadowLocalBroadcastManager) Shadow.extract(localBroadcastManager);
  }
}
