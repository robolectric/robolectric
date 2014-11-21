package org.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.google.android.maps.MapActivity;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow {@code MapActivity} that registers and unregisters a {@code BroadcastReceiver} when {@link #onResume()} and
 * {@link #onPause()} are called respectively.
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapActivity.class)
public class ShadowMapActivity extends ShadowActivity {
  private ConnectivityBroadcastReceiver connectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();

  public void __constructor__() {
  }

  @Implementation
  public void onCreate(android.os.Bundle bundle) {
    // todo: this should call Activity#onCreate(), but also invoke any shadows.
  }

  @Implementation
  public void onResume() {
    registerReceiver(connectivityBroadcastReceiver, new IntentFilter());
  }

  @Implementation
  public void onPause() {
    unregisterReceiver(connectivityBroadcastReceiver);
  }

  @Implementation
  public void onDestroy() {
  }

  @Implementation
  public boolean isRouteDisplayed() {
    return false;
  }

  private static class ConnectivityBroadcastReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
    }
  }
}
