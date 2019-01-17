package org.robolectric.shadows.support.v4;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.LinearLayout;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.ComponentController;

/** Version of FragmentController that can be used for android.support.v4.Fragment. */
public class SupportFragmentController<F extends Fragment>
    extends ComponentController<SupportFragmentController<F>, F> {
  private final F fragment;
  private final ActivityController<? extends FragmentActivity> activityController;

  protected SupportFragmentController(F fragment, Class<? extends FragmentActivity> activityClass) {
    this(fragment, activityClass, null);
  }

  protected SupportFragmentController(
      F fragment, Class<? extends FragmentActivity> activityClass, Intent intent) {
    super(fragment, intent);
    this.fragment = fragment;
    this.activityController = Robolectric.buildActivity(activityClass, intent);
  }

  public static <F extends Fragment> SupportFragmentController<F> of(F fragment) {
    return new SupportFragmentController<>(fragment, FragmentControllerActivity.class);
  }

  public static <F extends Fragment> SupportFragmentController<F> of(
      F fragment, Class<? extends FragmentActivity> activityClass) {
    return new SupportFragmentController<>(fragment, activityClass);
  }

  public static <F extends Fragment> SupportFragmentController<F> of(
      F fragment, Class<? extends FragmentActivity> activityClass, Intent intent) {
    return new SupportFragmentController<>(fragment, activityClass, intent);
  }

  /**
   * Sets up the given fragment by attaching it to an activity, calling its onCreate() through
   * onResume() lifecycle methods, and then making it visible. Note that the fragment will be added
   * to the view with ID 1.
   */
  public static <F extends Fragment> F setupFragment(F fragment) {
    return SupportFragmentController.of(fragment).create().start().resume().visible().get();
  }

  /**
   * Sets up the given fragment by attaching it to an activity, calling its onCreate() through
   * onResume() lifecycle methods, and then making it visible. Note that the fragment will be added
   * to the view with ID 1.
   */
  public static <F extends Fragment> F setupFragment(
      F fragment, Class<? extends FragmentActivity> fragmentActivityClass) {
    return SupportFragmentController.of(fragment, fragmentActivityClass)
        .create()
        .start()
        .resume()
        .visible()
        .get();
  }

  /**
   * Sets up the given fragment by attaching it to an activity created with the given bundle,
   * calling its onCreate() through onResume() lifecycle methods, and then making it visible. Note
   * that the fragment will be added to the view with ID 1.
   */
  public static <F extends Fragment> F setupFragment(
      F fragment, Class<? extends FragmentActivity> fragmentActivityClass, Bundle bundle) {
    return SupportFragmentController.of(fragment, fragmentActivityClass)
        .create(bundle)
        .start()
        .resume()
        .visible()
        .get();
  }

  /**
   * Sets up the given fragment by attaching it to an activity created with the given bundle and
   * container id, calling its onCreate() through onResume() lifecycle methods, and then making it
   * visible.
   */
  public static <F extends Fragment> F setupFragment(
      F fragment,
      Class<? extends FragmentActivity> fragmentActivityClass,
      int containerViewId,
      Bundle bundle) {
    return SupportFragmentController.of(fragment, fragmentActivityClass)
        .create(containerViewId, bundle)
        .start()
        .postCreate(bundle)
        .resume()
        .visible()
        .get();
  }

  /**
   * Creates the activity with {@link Bundle} and adds the fragment to the view with ID
   * {@code contentViewId}.
   */
  public SupportFragmentController<F> create(final int contentViewId, final Bundle bundle) {
    shadowMainLooper.runPaused(
        new Runnable() {
          @Override
          public void run() {
            activityController
                .create(bundle)
                .get()
                .getSupportFragmentManager()
                .beginTransaction()
                .add(contentViewId, fragment)
                .commitNow();
          }
        });
    return this;
  }

  /**
   * Creates the activity with {@link Bundle} and adds the fragment to it. Note that the fragment
   * will be added to the view with ID 1.
   */
  public SupportFragmentController<F> create(final Bundle bundle) {
    return create(1, bundle);
  }

  @Override
  public SupportFragmentController<F> create() {
    return create(null);
  }

  public SupportFragmentController<F> postCreate(Bundle bundle) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.postCreate(bundle);
      }
    });
    return this;
  }

  @Override
  public SupportFragmentController<F> destroy() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.destroy();
      }
    });
    return this;
  }

  public SupportFragmentController<F> start() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.start();
      }
    });
    return this;
  }

  public SupportFragmentController<F> resume() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.resume();
      }
    });
    return this;
  }

  public SupportFragmentController<F> pause() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.pause();
      }
    });
    return this;
  }

  public SupportFragmentController<F> stop() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.stop();
      }
    });
    return this;
  }

  public SupportFragmentController<F> visible() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.visible();
      }
    });
    return this;
  }

  public SupportFragmentController<F> saveInstanceState(final Bundle outState) {
    shadowMainLooper.runPaused(
        new Runnable() {
          @Override
          public void run() {
            activityController.saveInstanceState(outState);
          }
        });
    return this;
  }

  private static class FragmentControllerActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }
}
