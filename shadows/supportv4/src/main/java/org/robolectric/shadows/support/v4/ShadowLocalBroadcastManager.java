package org.robolectric.shadows.support.v4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.Provider;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(LocalBroadcastManager.class)
public class ShadowLocalBroadcastManager {
  private final List<Intent> sentBroadcastIntents = new ArrayList<>();
  private final List<Wrapper> registeredReceivers = new ArrayList<>();

  @Implementation
  protected static LocalBroadcastManager getInstance(final Context context) {
    return ShadowApplication.getInstance().getSingleton(LocalBroadcastManager.class, new Provider<LocalBroadcastManager>() {
      @Override
      public LocalBroadcastManager get() {
        return ReflectionHelpers.callConstructor(LocalBroadcastManager.class, ClassParameter.from(Context.class, context));
      }
    });
  }

  @Implementation
  protected void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    registeredReceivers.add(new Wrapper(receiver, filter));
  }

  @Implementation
  protected void unregisterReceiver(BroadcastReceiver receiver) {
    Iterator<Wrapper> iterator = registeredReceivers.iterator();
    while (iterator.hasNext()) {
      Wrapper wrapper = iterator.next();
      if (wrapper.broadcastReceiver == receiver) {
        iterator.remove();
      }
    }
  }

  @Implementation
  protected boolean sendBroadcast(Intent intent) {
    boolean sent = false;
    sentBroadcastIntents.add(intent);
    List<Wrapper> copy = new ArrayList<>();
    copy.addAll(registeredReceivers);
    for (Wrapper wrapper : copy) {
      if (wrapper.intentFilter.matchAction(intent.getAction())) {
        final int match = wrapper.intentFilter.matchData(intent.getType(), intent.getScheme(), intent.getData());
        if (match != IntentFilter.NO_MATCH_DATA && match != IntentFilter.NO_MATCH_TYPE) {
          sent = true;
          final BroadcastReceiver receiver = wrapper.broadcastReceiver;
          final Intent broadcastIntent = intent;
          Robolectric.getForegroundThreadScheduler().post(new Runnable() {
            @Override
            public void run() {
              receiver.onReceive(RuntimeEnvironment.application, broadcastIntent);
            }
          });
        }
      }
    }
    return sent;
  }

  public List<Intent> getSentBroadcastIntents() {
    return sentBroadcastIntents;
  }

  public List<Wrapper> getRegisteredBroadcastReceivers() {
    return registeredReceivers;
  }

  public static class Wrapper {
    public final BroadcastReceiver broadcastReceiver;
    public final IntentFilter intentFilter;

    public Wrapper(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
      this.broadcastReceiver = broadcastReceiver;
      this.intentFilter = intentFilter;
    }
  }
}
