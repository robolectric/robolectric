package org.robolectric.shadows;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.robolectric.Robolectric.shadowOf;

@Implements(LocalBroadcastManager.class)
public class ShadowLocalBroadcastManager {
  @Implementation
  public static LocalBroadcastManager getInstance(final Context context) {
    return shadowOf(context).getShadowApplication().getSingleton(LocalBroadcastManager.class, new Provider<LocalBroadcastManager>() {
      @Override
      public LocalBroadcastManager get() {
        return Robolectric.newInstance(LocalBroadcastManager.class, new Class[] {Context.class}, new Object[] {context});
      }
    });
  }
}
