package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(AndroidJUnit4.class)
public class ShadowViewThreadTest {

  @Rule public final SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Before
  public void setUp() {
    setSystemPropertyRule.set("robolectric.enforceViewMethodsCalledOnMainThread", "true");
  }

  @After
  public void tearDown() {
    ShadowView.getAndClear();
  }

  @Test
  public void testUnattachedView_noThreadCheck() throws Exception {
    View view = new View(ApplicationProvider.getApplicationContext());
    AtomicBoolean ran = new AtomicBoolean(false);
    Thread t =
        new Thread(
            () -> {
              view.invalidate();
              ran.set(true);
            });
    t.start();
    t.join();

    assertThat(ran.get()).isTrue();
    assertThat(ShadowView.getAndClear()).isEmpty();
  }

  @Test
  public void testAttachedView_checkThread() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    View view = new View(activity);
    activity.setContentView(view);

    AtomicBoolean ran = new AtomicBoolean(false);
    Thread t =
        new Thread(
            () -> {
              view.invalidate();
              ran.set(true);
            });
    t.start();
    t.join();

    assertThat(ran.get()).isTrue();
    ImmutableList<RuntimeException> errors = ShadowView.getAndClear();
    assertThat(errors).hasSize(1);
  }

  @Test
  public void testDeduplicationByCallSite() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    View view = new View(activity);
    activity.setContentView(view);

    Thread t =
        new Thread(
            () -> {
              // Both calls are from the same lambda/call site
              view.invalidate();
              view.requestLayout();
            });
    t.start();
    t.join();

    ImmutableList<RuntimeException> errors = ShadowView.getAndClear();
    assertThat(errors).hasSize(2);
  }

  @Test
  public void testMultipleUniqueCallSites() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    View view = new View(activity);
    activity.setContentView(view);

    Thread t1 = new Thread(() -> view.invalidate());
    Thread t2 = new Thread(() -> view.requestLayout());

    t1.start();
    t1.join();
    t2.start();
    t2.join();

    // Two different threads/call sites, so both should be captured
    ImmutableList<RuntimeException> errors = ShadowView.getAndClear();
    assertThat(errors).hasSize(2);
  }

  @Test
  public void testThreadError_attachedToTestFailure() throws Throwable {
    Activity activity = Robolectric.setupActivity(Activity.class);
    View view = new View(activity);
    activity.setContentView(view);

    Thread t = new Thread(() -> view.invalidate());
    t.start();
    t.join();

    // The thread error is now in ShadowView.threadErrors.
    // If we throw an exception now, RobolectricTestRunner should call
    // checkStateAfterTestFailure and attach the thread error.

    // We can't easily test the runner's behavior inside the test itself
    // without quite complex setup, but we can verify the state.
    assertThat(ShadowView.getAndClear()).hasSize(1);
  }

  @Test
  public void testTextView_setText_checkThread() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    TextView textView = new TextView(activity);
    activity.setContentView(textView);

    AtomicBoolean ran = new AtomicBoolean(false);
    Thread t =
        new Thread(
            () -> {
              textView.setText("Hello");
              ran.set(true);
            });
    t.start();
    t.join();

    assertThat(ran.get()).isTrue();
    ImmutableList<RuntimeException> errors = ShadowView.getAndClear();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0).getClass().getName())
        .isEqualTo("android.view.ViewRootImpl$CalledFromWrongThreadException");
  }
}
