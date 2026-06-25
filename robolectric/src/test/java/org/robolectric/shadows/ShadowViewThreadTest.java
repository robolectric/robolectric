package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.Robolectric;
import org.robolectric.SingleSdkRobolectricTestRunner;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.UsesSdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.util.TestUtil;
import org.robolectric.util.inject.Injector;

/**
 * Unit tests for ShadowView's thread enforcement. This uses {@link SingleSdkRobolectricTestRunner}
 * to create a new SDK sandbox for this test class, so the system property
 * `robolectric.enforceViewMethodsCalledOnMainThread` can be set before the View methods are bound
 * using invokedynamic.
 */
@RunWith(JUnit4.class)
public class ShadowViewThreadTest {

  @Rule public final SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Before
  public void setUp() {
    setSystemPropertyRule.set("robolectric.enforceViewMethodsCalledOnMainThread", "true");
  }

  @Test
  public void testOldestSdk() throws Exception {
    Injector injector =
        SingleSdkRobolectricTestRunner.defaultInjector()
            .bind(SdkPicker.class, OldestSdkPicker.class)
            .build();
    SingleSdkRobolectricTestRunner runner =
        new SingleSdkRobolectricTestRunner(IsolatedTests.class, injector);
    Result result = new JUnitCore().run(runner);
    if (!result.wasSuccessful()) {
      fail("Isolated tests failed on oldest SDK: " + result.getFailures().get(0).getException());
    }
  }

  @Test
  public void testNewestSdk() throws Exception {
    Injector injector =
        SingleSdkRobolectricTestRunner.defaultInjector()
            .bind(SdkPicker.class, NewestSdkPicker.class)
            .build();
    SingleSdkRobolectricTestRunner runner =
        new SingleSdkRobolectricTestRunner(IsolatedTests.class, injector);
    Result result = new JUnitCore().run(runner);
    if (!result.wasSuccessful()) {
      fail("Isolated tests failed on newest SDK: " + result.getFailures().get(0).getException());
    }
  }

  public static class OldestSdkPicker implements SdkPicker {
    @Override
    public List<Sdk> selectSdks(Configuration configuration, UsesSdk usesSdk) {
      return Collections.singletonList(TestUtil.getSdkCollection().getSupportedSdks().first());
    }
  }

  public static class NewestSdkPicker implements SdkPicker {
    @Override
    public List<Sdk> selectSdks(Configuration configuration, UsesSdk usesSdk) {
      return Collections.singletonList(TestUtil.getSdkCollection().getSupportedSdks().last());
    }
  }

  @Ignore
  @RunWith(AndroidJUnit4.class)
  public static class IsolatedTests {

    @Before
    public void setUp() {
      // Simulate the UI test contamination (uninitialized sandbox avoids cross-test state leakage)
      View view = new View(ApplicationProvider.getApplicationContext());
      view.invalidate();
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
}
