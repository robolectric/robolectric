package org.robolectric.util;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.widget.LinearLayout;
import org.robolectric.Robolectric;
import org.robolectric.ShadowsAdapter;

/**
 * Controller class for driving fragment lifecycles, similar to {@link org.robolectric.util.ActivityController}. Only
 * necessary if more complex lifecycle management is needed, otherwise {@link org.robolectric.util.FragmentTestUtil}
 * should be sufficient.
 */
public class FragmentController<F extends Fragment> extends ComponentController<FragmentController<F>, F> {
  private final F fragment;
  private final ActivityController<? extends Activity> activityController;

  private FragmentController(ShadowsAdapter shadowsAdapter, F fragment, Class<? extends Activity> activityClass) {
    super(shadowsAdapter, fragment);
    this.fragment = fragment;
    this.activityController = Robolectric.buildActivity(activityClass);
  }

  public static <F extends Fragment> FragmentController<F> of(F fragment) {
    return new FragmentController<>(Robolectric.getShadowsAdapter(), fragment, FragmentControllerActivity.class);
  }

  public static <F extends Fragment> FragmentController<F> of(F fragment, Class<? extends Activity> activityClass) {
    return new FragmentController<>(Robolectric.getShadowsAdapter(), fragment, activityClass);
  }

  @Override
  public FragmentController<F> attach() {
    activityController.attach();
    return this;
  }

  public FragmentController<F> create(final Bundle bundle) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        if (!attached) attach();
        activityController.create(bundle).get().getFragmentManager().beginTransaction().add(fragment, null).commit();
      }
    });
    return this;
  }

  @Override
  public FragmentController<F> create() {
    return create(null);
  }

  @Override
  public FragmentController<F> destroy() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.destroy();
      }
    });
    return this;
  }

  public FragmentController<F> start() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.start();
      }
    });
    return this;
  }

  public FragmentController<F> resume() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.resume();
      }
    });
    return this;
  }

  public FragmentController<F> pause() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.pause();
      }
    });
    return this;
  }

  public FragmentController<F> stop() {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.stop();
      }
    });
    return this;
  }

  public FragmentController<F> saveInstanceState(final Bundle outState) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        activityController.saveInstanceState(outState);
      }
    });
    return this;
  }

  private static class FragmentControllerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }
}
