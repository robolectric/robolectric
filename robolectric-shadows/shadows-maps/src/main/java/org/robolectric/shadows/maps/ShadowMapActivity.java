package org.robolectric.shadows.maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.maps.MapActivity;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity;

/**
 * Shadow for {@link com.google.android.maps.MapActivity}.
 */
@Implements(MapActivity.class)
public class ShadowMapActivity extends ShadowActivity {
  private ConnectivityBroadcastReceiver connectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();

  public void __constructor__() {
  }

  @Implementation
  public void onCreate(Bundle bundle) {
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
