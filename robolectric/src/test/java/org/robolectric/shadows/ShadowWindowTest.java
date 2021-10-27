package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.R;
import android.app.Activity;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.FrameMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowWindowTest {
  @Test
  public void getFlag_shouldReturnWindowFlags() {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();

    assertThat(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN)).isFalse();
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    assertThat(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN)).isTrue();
    window.setFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON, WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    assertThat(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN)).isTrue();
  }

  @Test
  public void getSystemFlag_isFalseByDefault() {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();
    int fakeSystemFlag1 = 0b1;

    assertThat(shadowOf(window).getPrivateFlag(fakeSystemFlag1)).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void getSystemFlag_shouldReturnFlagsSetViaAddSystemFlags() throws Exception {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();
    int fakeSystemFlag1 = 0b1;

    window.addSystemFlags(fakeSystemFlag1);

    assertThat(shadowOf(window).getPrivateFlag(fakeSystemFlag1)).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void getSystemFlag_callingAddSystemFlagsShouldNotOverrideExistingFlags() throws Exception {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();
    int fakeSystemFlag1 = 0b1;
    int fakeSystemFlag2 = 0b10;
    window.addSystemFlags(fakeSystemFlag1);

    window.addSystemFlags(fakeSystemFlag2);

    assertThat(shadowOf(window).getPrivateFlag(fakeSystemFlag1)).isTrue();
  }

  @Test
  @Config(minSdk = KITKAT, maxSdk = VERSION_CODES.R)
  public void getSystemFlag_shouldReturnFlagsSetViaAddPrivateFlags() throws Exception {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();
    int fakeSystemFlag1 = 0b1;

    window.addPrivateFlags(fakeSystemFlag1);

    assertThat(shadowOf(window).getPrivateFlag(fakeSystemFlag1)).isTrue();
  }

  @Test
  @Config(minSdk = KITKAT, maxSdk = VERSION_CODES.R)
  public void getSystemFlag_callingAddPrivateFlagsShouldNotOverrideExistingFlags()
      throws Exception {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();
    int fakeSystemFlag1 = 0b1;
    int fakeSystemFlag2 = 0b10;
    window.addPrivateFlags(fakeSystemFlag1);

    window.addPrivateFlags(fakeSystemFlag2);

    assertThat(shadowOf(window).getPrivateFlag(fakeSystemFlag1)).isTrue();
  }

  @Test
  public void getTitle_shouldReturnWindowTitle() {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();
    window.setTitle("My Window Title");
    assertThat(shadowOf(window).getTitle().toString()).isEqualTo("My Window Title");
  }

  @Test
  public void getBackgroundDrawable_returnsSetDrawable() {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();
    ShadowWindow shadowWindow = shadowOf(window);

    assertThat(shadowWindow.getBackgroundDrawable()).isNull();

    window.setBackgroundDrawableResource(R.drawable.btn_star);
    assertThat(shadowOf(shadowWindow.getBackgroundDrawable()).createdFromResId).isEqualTo(R.drawable.btn_star);
  }

  @Test
  public void getSoftInputMode_returnsSoftInputMode() {
    TestActivity activity = Robolectric.buildActivity(TestActivity.class).create().get();
    Window window = activity.getWindow();
    ShadowWindow shadowWindow = shadowOf(window);

    window.setSoftInputMode(7);

    assertThat(shadowWindow.getSoftInputMode()).isEqualTo(7);
  }

  @Test @Config(maxSdk = LOLLIPOP_MR1)
  public void forPreM_create_shouldCreateImplPhoneWindow() throws Exception {
    assertThat(
            ShadowWindow.create(ApplicationProvider.getApplicationContext()).getClass().getName())
        .isEqualTo("com.android.internal.policy.impl.PhoneWindow");
  }

  @Test @Config(minSdk = M)
  public void forM_create_shouldCreatePhoneWindow() throws Exception {
    assertThat(
            ShadowWindow.create(ApplicationProvider.getApplicationContext()).getClass().getName())
        .isEqualTo("com.android.internal.policy.PhoneWindow");
  }

  @Test
  @Config(minSdk = N)
  public void reportOnFrameMetricsAvailable_notifiesListeners() throws Exception {
    ActivityController<Activity> activityController = Robolectric.buildActivity(Activity.class);

    // Attaches the ViewRootImpl to the window.
    // When the ViewRootImpl is attached, android will check to see if hardware acceleration is
    // enabled, and only attach listeners if it is. Attaching, rather than just using a created
    // window, allows for verification that triggering works even then.
    activityController.setup();

    Window window = activityController.get().getWindow();
    Window.OnFrameMetricsAvailableListener listener =
        Mockito.mock(Window.OnFrameMetricsAvailableListener.class);
    FrameMetrics frameMetrics = new FrameMetricsBuilder().build();

    window.addOnFrameMetricsAvailableListener(listener, new Handler(Looper.getMainLooper()));
    shadowOf(window).reportOnFrameMetricsAvailable(frameMetrics);

    verify(listener)
        .onFrameMetricsAvailable(window, frameMetrics, /* dropCountSinceLastInvocation= */ 0);
  }

  @Test
  @Config(minSdk = N)
  public void reportOnFrameMetricsAvailable_nonZeroDropCount_notifiesListeners() throws Exception {
    ActivityController<Activity> activityController = Robolectric.buildActivity(Activity.class);
    activityController.setup();

    Window window = activityController.get().getWindow();
    Window.OnFrameMetricsAvailableListener listener =
        Mockito.mock(Window.OnFrameMetricsAvailableListener.class);
    FrameMetrics frameMetrics = new FrameMetricsBuilder().build();

    window.addOnFrameMetricsAvailableListener(listener, new Handler(Looper.getMainLooper()));
    shadowOf(window)
        .reportOnFrameMetricsAvailable(frameMetrics, /* dropCountSinceLastInvocation= */ 3);

    verify(listener)
        .onFrameMetricsAvailable(window, frameMetrics, /* dropCountSinceLastInvocation= */ 3);
  }

  @Test
  @Config(minSdk = N)
  public void reportOnFrameMetricsAvailable_listenerRemoved_doesntNotifyListener()
      throws Exception {
    ActivityController<Activity> activityController = Robolectric.buildActivity(Activity.class);
    activityController.setup();

    Window window = activityController.get().getWindow();
    Window.OnFrameMetricsAvailableListener listener =
        Mockito.mock(Window.OnFrameMetricsAvailableListener.class);
    FrameMetrics frameMetrics = new FrameMetricsBuilder().build();

    window.addOnFrameMetricsAvailableListener(listener, new Handler(Looper.getMainLooper()));
    window.removeOnFrameMetricsAvailableListener(listener);
    shadowOf(window).reportOnFrameMetricsAvailable(frameMetrics);

    verify(listener, never())
        .onFrameMetricsAvailable(
            any(Window.class),
            any(FrameMetrics.class),
            /* dropCountSinceLastInvocation= */ anyInt());
  }

  @Test
  @Config(minSdk = N)
  public void reportOnFrameMetricsAvailable_noListener_doesntCrash() throws Exception {
    Window window = ShadowWindow.create(ApplicationProvider.getApplicationContext());

    // Shouldn't crash.
    shadowOf(window).reportOnFrameMetricsAvailable(new FrameMetricsBuilder().build());
  }

  public static class TestActivity extends Activity {
    public int requestFeature = Window.FEATURE_PROGRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(R.style.Theme_Holo_Light);
      getWindow().requestFeature(requestFeature);
      setContentView(new LinearLayout(this));
      getActionBar().setIcon(R.drawable.ic_lock_power_off);
    }
  }
}
