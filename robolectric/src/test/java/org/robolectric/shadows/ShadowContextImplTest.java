package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowContextImplTest {
  private final Application context = ApplicationProvider.getApplicationContext();
  private final ShadowContextImpl shadowContext = Shadow.extract(context.getBaseContext());

  @Test
  @Config(minSdk = N)
  public void deviceProtectedContext() {
    // Regular context should be credential protected, not device protected.
    assertThat(context.isDeviceProtectedStorage()).isFalse();
    assertThat(context.isCredentialProtectedStorage()).isFalse();

    // Device protected storage context should have device protected rather than credential
    // protected storage.
    Context deviceProtectedStorageContext = context.createDeviceProtectedStorageContext();
    assertThat(deviceProtectedStorageContext.isDeviceProtectedStorage()).isTrue();
    assertThat(deviceProtectedStorageContext.isCredentialProtectedStorage()).isFalse();

    // Data dirs of these two contexts must be different locations.
    assertThat(context.getDataDir()).isNotEqualTo(deviceProtectedStorageContext.getDataDir());
  }

  @Test
  @Config(minSdk = N)
  public void testMoveSharedPreferencesFrom() throws Exception {
    String PREFS = "PREFS";
    String PREF_NAME = "TOKEN_PREF";

    context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(PREF_NAME, "token")
        .commit();

    Context dpContext = context.createDeviceProtectedStorageContext();

    assertThat(dpContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(PREF_NAME))
        .isFalse();
    assertThat(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(PREF_NAME))
        .isTrue();

    assertThat(dpContext.moveSharedPreferencesFrom(context, PREFS)).isTrue();

    assertThat(dpContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(PREF_NAME))
        .isTrue();
    assertThat(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(PREF_NAME))
        .isFalse();
  }

  @Config(minSdk = KITKAT)
  @Test
  public void getExternalFilesDirs() {
    File[] dirs = context.getExternalFilesDirs("something");
    assertThat(dirs).asList().hasSize(1);
    assertThat(dirs[0].isDirectory()).isTrue();
    assertThat(dirs[0].canWrite()).isTrue();
    assertThat(dirs[0].getName()).isEqualTo("something");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getSystemService_shouldReturnBluetoothAdapter() {
    assertThat(context.getSystemService(Context.BLUETOOTH_SERVICE))
        .isInstanceOf(BluetoothManager.class);
  }

  @Test
  public void getSystemService_shouldReturnWallpaperManager() {
    assertThat(context.getSystemService(Context.WALLPAPER_SERVICE))
        .isInstanceOf(WallpaperManager.class);
  }

  @Test
  public void removeSystemService_getSystemServiceReturnsNull() {
    shadowContext.removeSystemService(Context.WALLPAPER_SERVICE);
    assertThat(context.getSystemService(Context.WALLPAPER_SERVICE)).isNull();
  }

  @Test
  public void startIntentSender_activityIntent() throws IntentSender.SendIntentException {
    PendingIntent intent =
        PendingIntent.getActivity(
            context,
            0,
            new Intent()
                .setClassName(context, "ActivityIntent")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT);

    context.startIntentSender(intent.getIntentSender(), null, 0, 0, 0);

    assertThat(shadowOf(context).getNextStartedActivity().getComponent().getClassName())
        .isEqualTo("ActivityIntent");
  }

  @Test
  public void startIntentSender_broadcastIntent() throws IntentSender.SendIntentException {
    PendingIntent intent =
        PendingIntent.getBroadcast(
            context,
            0,
            new Intent().setClassName(context, "BroadcastIntent"),
            PendingIntent.FLAG_UPDATE_CURRENT);

    context.startIntentSender(intent.getIntentSender(), null, 0, 0, 0);

    assertThat(shadowOf(context).getBroadcastIntents().get(0).getComponent().getClassName())
        .isEqualTo("BroadcastIntent");
  }

  @Test
  public void startIntentSender_serviceIntent() throws IntentSender.SendIntentException {
    PendingIntent intent =
        PendingIntent.getService(
            context,
            0,
            new Intent().setClassName(context, "ServiceIntent"),
            PendingIntent.FLAG_UPDATE_CURRENT);

    context.startIntentSender(intent.getIntentSender(), null, 0, 0, 0);

    assertThat(shadowOf(context).getNextStartedService().getComponent().getClassName())
        .isEqualTo("ServiceIntent");
  }

  @Test
  public void createPackageContext() throws Exception {
    Context packageContext = context.createPackageContext(context.getPackageName(), 0);

    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.cloneInContext(packageContext);

    inflater.inflate(R.layout.remote_views, new FrameLayout(context), false);
  }

  @Test
  public void createPackageContextRemoteViews() throws Exception {
    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_views);
    remoteViews.apply(context, new FrameLayout(context));
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void bindServiceAsUser() throws Exception {
    Intent serviceIntent = new Intent();
    ServiceConnection serviceConnection = buildServiceConnection();
    int flags = 0;

    assertThat(
            context.bindServiceAsUser(
                serviceIntent, serviceConnection, flags, Process.myUserHandle()))
        .isTrue();

    assertThat(shadowOf(context).getBoundServiceConnections()).hasSize(1);
  }

  @Test
  public void bindService() throws Exception {
    Intent serviceIntent = new Intent();
    ServiceConnection serviceConnection = buildServiceConnection();
    int flags = 0;

    assertThat(context.bindService(serviceIntent, serviceConnection, flags)).isTrue();

    assertThat(shadowOf(context).getBoundServiceConnections()).hasSize(1);
  }

  @Test
  public void bindService_unbindable() throws Exception {
    String action = "foo-action";
    Intent serviceIntent = new Intent(action);
    ServiceConnection serviceConnection = buildServiceConnection();
    int flags = 0;
    shadowOf(context).declareActionUnbindable(action);

    assertThat(context.bindService(serviceIntent, serviceConnection, flags)).isFalse();
  }

  private ServiceConnection buildServiceConnection() {
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {}

      @Override
      public void onServiceDisconnected(ComponentName name) {}
    };
  }
}
