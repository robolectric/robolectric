package org.robolectric.shadows;

import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestApplication;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowContextImplTest {
  private final Context context = RuntimeEnvironment.application;

  @Test
  @Config(sdk = {
    Build.VERSION_CODES.JELLY_BEAN_MR2,
    Build.VERSION_CODES.KITKAT,
    Build.VERSION_CODES.LOLLIPOP,
    Build.VERSION_CODES.LOLLIPOP_MR1
  })
  public void getSystemService_shouldReturnBluetoothAdapter() {
    assertThat(context.getSystemService(Context.BLUETOOTH_SERVICE)).isInstanceOf(BluetoothManager.class);
  }

  @Test
  public void getSystemService_shouldReturnWallpaperManager() {
    assertThat(context.getSystemService(Context.WALLPAPER_SERVICE)).isInstanceOf(WallpaperManager.class);
  }

  @Test
  public void startIntentSender_activityIntent() throws IntentSender.SendIntentException {
    PendingIntent intent = PendingIntent.getActivity(context, 0,
        new Intent().setClassName(RuntimeEnvironment.application, "ActivityIntent"),
        PendingIntent.FLAG_UPDATE_CURRENT);

    context.startIntentSender(intent.getIntentSender(), null, 0, 0, 0);

    assertThat(ShadowApplication.getInstance().getNextStartedActivity().getComponent().getClassName()).isEqualTo("ActivityIntent");
  }

  @Test
  public void startIntentSender_broadcastIntent() throws IntentSender.SendIntentException {
    PendingIntent intent = PendingIntent.getBroadcast(context, 0,
        new Intent().setClassName(RuntimeEnvironment.application, "BroadcastIntent"),
        PendingIntent.FLAG_UPDATE_CURRENT);

    context.startIntentSender(intent.getIntentSender(), null, 0, 0, 0);

    assertThat(ShadowApplication.getInstance().getBroadcastIntents().get(0).getComponent().getClassName()).isEqualTo("BroadcastIntent");
  }

  @Test
  public void startIntentSender_serviceIntent() throws IntentSender.SendIntentException {
    PendingIntent intent = PendingIntent.getService(context, 0,
        new Intent().setClassName(RuntimeEnvironment.application, "ServiceIntent"),
        PendingIntent.FLAG_UPDATE_CURRENT);
    ((TestApplication) context).getBaseContext().getApplicationContext();

    context.startIntentSender(intent.getIntentSender(), null, 0, 0, 0);

    assertThat(ShadowApplication.getInstance().getNextStartedService().getComponent().getClassName()).isEqualTo("ServiceIntent");
  }

}

