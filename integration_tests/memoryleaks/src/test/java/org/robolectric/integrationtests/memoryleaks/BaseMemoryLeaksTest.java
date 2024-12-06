package org.robolectric.integrationtests.memoryleaks;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import com.google.common.testing.GcFinalization;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.res.android.Registries;
import org.robolectric.util.ReflectionHelpers;

/**
 * A test that verifies that activities and fragments become GC candidates after being destroyed, or
 * after a test terminates.
 *
 * <p>For internal reasons, this class is subclassed rather than inlining {@link #assertNotLeaking}
 * in this file.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseMemoryLeaksTest {

  private static WeakReference<Activity> awr = null;

  // A weak reference to the application. This is used to ensure that the application can be
  // collected after a test.
  private static WeakReference<Application> ar = null;

  @Before
  public void setUp() {
    if (ar != null) {
      assertNotLeaking(ar::get);
    }
  }

  @After
  public void tearDown() {
    // Do a null check in case we are running with lazy application.
    if (RuntimeEnvironment.application != null) {
      ar = new WeakReference<>(RuntimeEnvironment.application);
    }
  }

  @Test
  // Do not shard these two tests, they must run on the same machine sequentially.
  // These tests are prefixed with aaa_ to ensure they run first. Some leaks are caused by
  // static setup that occurs once at the start of a test class, so these must be the first tests
  // that run.
  public void aaa_activityCanBeGcdBetweenTest_1() {
    assertThat(awr).isNull();
    try (ActivityController<Activity> ac = Robolectric.buildActivity(Activity.class)) {
      ac.setup();
      awr = new WeakReference<>(ac.get());
    }
  }

  @Test
  // Do not shard these two tests, they must run on the same machine sequentially.
  // These tests are prefixed with aaa_ to ensure they run first. Some leaks are caused by
  // static setup that occurs once at the start of a test class, so these must be the first tests
  // that run.
  public void aaa_activityCanBeGcdBetweenTest_2() {
    assertThat(awr).isNotNull();
    assertNotLeaking(awr::get);
  }

  @Test
  public void activityCanBeGcdAfterDestroyed() {
    assertNotLeaking(
        () -> {
          ActivityController<Activity> ac = Robolectric.buildActivity(Activity.class).setup();
          Activity activity = ac.get();
          ac.pause().stop().destroy();
          return activity;
        });
  }

  @Test
  public void activityCanBeGcdAfterConfigChange() {
    assertNotLeaking(
        () -> {
          ActivityController<Activity> ac = Robolectric.buildActivity(Activity.class).setup();
          Activity activity = ac.get();
          Configuration currentConfiguration = activity.getResources().getConfiguration();
          Configuration newConfiguration = new Configuration(currentConfiguration);
          newConfiguration.orientation =
              currentConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE
                  ? Configuration.ORIENTATION_PORTRAIT
                  : Configuration.ORIENTATION_LANDSCAPE;
          ac.configurationChange(newConfiguration);
          return activity;
        });
  }

  @Test
  public void fragmentCanBeGcdAfterActivityDestroyed() {
    assertNotLeaking(
        () -> {
          ActivityController<FragmentActivity> ac =
              Robolectric.buildActivity(FragmentActivity.class).setup();
          FragmentContainerView contentView = new FragmentContainerView(ac.get());
          contentView.setId(android.R.id.list_container);
          ac.get().setContentView(contentView);
          Fragment f = new Fragment();
          ac.get()
              .getSupportFragmentManager()
              .beginTransaction()
              .replace(android.R.id.list_container, f)
              .commitNow();
          ac.pause().stop().destroy();
          // Idle any potential Fragment animations.
          shadowOf(Looper.getMainLooper()).idle();
          return f;
        });
  }

  @Test
  public void fragmentCanBeGcdAfterRemoved() {
    assertNotLeaking(
        () -> {
          ActivityController<FragmentActivity> ac =
              Robolectric.buildActivity(FragmentActivity.class).setup();
          FragmentContainerView contentView = new FragmentContainerView(ac.get());
          contentView.setId(android.R.id.list_container);
          ac.get().setContentView(contentView);
          Fragment f = new Fragment();
          ac.get()
              .getSupportFragmentManager()
              .beginTransaction()
              .replace(android.R.id.list_container, f)
              .commitNow();
          ac.get()
              .getSupportFragmentManager()
              .beginTransaction()
              .replace(android.R.id.list_container, new Fragment())
              .commitNow();
          return f;
        });
  }

  @Test
  public void typedArrayData() {
    assertNotLeaking(
        () -> {
          Context context = RuntimeEnvironment.getApplication();
          TypedArray typedArray = context.obtainStyledAttributes(new int[] {});
          return ReflectionHelpers.getField(typedArray, "mData");
        });
  }

  @Test
  @Config(minSdk = N)
  public void themeObjectInNativeObjectRegistry() {
    final AtomicLong themeId = new AtomicLong(0);
    assertNotLeaking(
        () -> {
          Theme theme = RuntimeEnvironment.getApplication().getResources().newTheme();
          long nativeId =
              ReflectionHelpers.getField(ReflectionHelpers.getField(theme, "mThemeImpl"), "mTheme");
          themeId.set(nativeId);
          return theme;
        });

    // Also wait for the theme to be cleared from the registry.
    GcFinalization.awaitDone(
        () -> Registries.NATIVE_THEME9_REGISTRY.peekNativeObject(themeId.get()) == null);
  }

  public abstract <T> void assertNotLeaking(Callable<T> potentiallyLeakingCallable);
}
