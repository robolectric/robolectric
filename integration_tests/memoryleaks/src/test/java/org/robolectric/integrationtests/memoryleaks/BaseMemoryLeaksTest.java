package org.robolectric.integrationtests.memoryleaks;

import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * A test that verifies that activities and fragments become GC candidates after being destroyed, or
 * after a test terminates.
 *
 * <p>For internal reasons, this class is subclassed rather than inlining {@link #assertNotLeaking}
 * in this file.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
public abstract class BaseMemoryLeaksTest {

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

  public abstract <T> void assertNotLeaking(Callable<T> potentiallyLeakingCallable);
}
