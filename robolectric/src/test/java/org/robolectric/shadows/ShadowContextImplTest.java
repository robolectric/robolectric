package org.robolectric.shadows;

import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestApplication;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowContextImplTest {
  private final Context context = RuntimeEnvironment.application;

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
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

  @Test
  public void createPackageContext() throws Exception {
    Context packageContext = context.createPackageContext(RuntimeEnvironment.application.getPackageName(), 0);

    LayoutInflater inflater = (LayoutInflater) RuntimeEnvironment.application.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.cloneInContext(packageContext);

    inflater.inflate(R.layout.remote_views, new FrameLayout(RuntimeEnvironment.application), false);
  }

  @Test
  public void createPackageContextRemoteViews() throws Exception {
    RemoteViews remoteViews = new RemoteViews(RuntimeEnvironment.application.getPackageName(), R.layout.remote_views);
    remoteViews.apply(RuntimeEnvironment.application, new FrameLayout(RuntimeEnvironment.application));
  }
}

