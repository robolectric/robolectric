package org.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import static junit.framework.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class LocalBroadcastManagerTest {
  private Transcript transcript = new Transcript();

  @Test
  public void shouldGetInstance() throws Exception {
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
    assertNotNull(instance);
    assertSame(instance, LocalBroadcastManager.getInstance(Robolectric.application));
  }

  @Test
  public void shouldSendBroadcasts() throws Exception {
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
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
  public void shouldUnregisterReceiver() throws Exception {
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(Robolectric.application);
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
    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(Robolectric.application);
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
}
