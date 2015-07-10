package org.robolectric.shadows.support.v4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.support.v4.ShadowLocalBroadcastManager;
import org.robolectric.util.TestRunnerWithManifest;
import org.robolectric.util.Transcript;

import static junit.framework.Assert.*;

@RunWith(TestRunnerWithManifest.class)
public class ShadowLocalBroadcastManagerTest {
  private Transcript transcript = new Transcript();

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
    assertFalse(called[0]);
    instance.sendBroadcast(new Intent("com.foo"));
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
    assertFalse(called[0]);
    instance.sendBroadcast(new Intent("com.foo", Uri.parse("http://robolectric.org")));
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
    assertFalse(called[0]);
  }

  @Test
  public void testGetBroadcastIntents() throws Exception {
    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    broadcastManager.registerReceiver(new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        transcript.add("got intent " + intent.getAction());
      }
    }, IntentFilter.create("foo", "blatz"));

    Intent intent1 = new Intent("foo");
    broadcastManager.sendBroadcast(intent1);
    Intent intent2 = new Intent("bar");
    broadcastManager.sendBroadcast(intent2);

    transcript.assertEventsSoFar("got intent foo");
  }

  @Test
  public void testGetRegisteredBroadcastReceivers() throws Exception {
    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
    ShadowLocalBroadcastManager shadowLocalBroadcastManager = shadowOf(broadcastManager);
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
    ShadowLocalBroadcastManager shadowLocalBroadcastManager = shadowOf(broadcastManager);
    assertEquals(0, shadowLocalBroadcastManager.getSentBroadcastIntents().size());

    Intent broadcastIntent = new Intent("foo");
    broadcastManager.sendBroadcast(broadcastIntent);

    assertEquals(1, shadowLocalBroadcastManager.getSentBroadcastIntents().size());
    assertEquals(broadcastIntent, shadowLocalBroadcastManager.getSentBroadcastIntents().get(0));
  }

  private ShadowLocalBroadcastManager shadowOf(LocalBroadcastManager localBroadcastManager) {
    return (ShadowLocalBroadcastManager) ShadowExtractor.extract(localBroadcastManager);
  }
}
