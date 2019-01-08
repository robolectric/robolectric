package org.robolectric.integrationtests.memoryleaks;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.google.common.testing.GcFinalization;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.Callable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * Test that verifies that activities and fragments become GC candidates after being destroyed, or
 * after a test terminates.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
public final class MemoryLeaksTest {

  private static WeakReference<Activity> awr = null;

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
          Fragment f = new Fragment();
          ac.get()
              .getSupportFragmentManager()
              .beginTransaction()
              .replace(android.R.id.content, f)
              .commitNow();
          ac.pause().stop().destroy();
          return f;
        });
  }

  @Test
  public void fragmentCanBeGcdAfterRemoved() {
    assertNotLeaking(
        () -> {
          ActivityController<FragmentActivity> ac =
              Robolectric.buildActivity(FragmentActivity.class).setup();
          Fragment f = new Fragment();
          ac.get()
              .getSupportFragmentManager()
              .beginTransaction()
              .replace(android.R.id.content, f)
              .commitNow();
          ac.get()
              .getSupportFragmentManager()
              .beginTransaction()
              .replace(android.R.id.content, new Fragment())
              .commitNow();
          return f;
        });
  }

  @Test
  // Do not shard these two tests, they must run on the same machine sequentially.
  public void activityCanBeGcdBetweenTest_1() {
    if (awr == null) {
      ActivityController<Activity> ac = Robolectric.buildActivity(Activity.class).setup();
      awr = new WeakReference<>(ac.get());
    } else {
      assertNotLeaking(awr::get);
    }
  }

  @Test
  // Do not shard these two tests, they must run on the same machine sequentially.
  public void activityCanBeGcdBetweenTest_2() {
    if (awr == null) {
      ActivityController<Activity> ac = Robolectric.buildActivity(Activity.class).setup();
      awr = new WeakReference<>(ac.get());
    } else {
      assertNotLeaking(awr::get);
    }
  }

  // Allow assigning null to potentiallyLeakingCallable to clear the stack's reference on the
  // callable.
  @SuppressWarnings("assignment.type.incompatible")
  public static <T> void assertNotLeaking(Callable<T> potentiallyLeakingCallable) {
    WeakReference<T> wr;
    try {
      wr = new WeakReference<>(potentiallyLeakingCallable.call());
      // Make it explicit that the callable isn't reachable from this method's stack, in case it
      // holds a strong reference on the supplied instance.
      potentiallyLeakingCallable = null;
    } catch (Exception e) {
      throw new IllegalStateException("encountered an error in the callable", e);
    }
    assertReferentWeaklyReachable(wr);
  }

  private static <T> void assertReferentWeaklyReachable(WeakReference<T> wr) {
    try {
      GcFinalization.awaitClear(wr);
    } catch (RuntimeException e) {
      T notWeaklyReachable = wr.get();

      if (notWeaklyReachable == null) {
        // Looks like it is weakly reachable after all.
        return;
      }

      // GcFinalization throws a RuntimeException instead of a TimeoutException when we timeout to
      // clear the weak reference, so we catch any exception and consider that the assertion failed
      // in that case.
      throw new AssertionError(
          String.format(
              Locale.ROOT,
              "Not true that <%s> is not leaking, encountered an error while attempting to GC it.",
              notWeaklyReachable),
          e);
    }
  }
}
