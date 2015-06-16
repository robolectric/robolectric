package org.robolectric;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDisplay;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.StubViewRoot;
import org.robolectric.internal.Shadow;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.TestOnClickListener;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class RobolectricTest {

  private PrintStream originalSystemOut;
  private ByteArrayOutputStream buff;
  private String defaultLineSeparator;

  @Before
  public void setUp() {
    originalSystemOut = System.out;
    defaultLineSeparator = System.getProperty("line.separator");

    System.setProperty("line.separator", "\n");
    buff = new ByteArrayOutputStream();
    PrintStream testOut = new PrintStream(buff);
    System.setOut(testOut);
  }

  @After
  public void tearDown() throws Exception {
    System.setProperty("line.separator", defaultLineSeparator);
    System.setOut(originalSystemOut);
  }

  @Test(expected = RuntimeException.class)
  public void clickOn_shouldThrowIfViewIsDisabled() throws Exception {
    View view = new View(RuntimeEnvironment.application);
    view.setEnabled(false);
    ShadowView.clickOn(view);
  }

  @Test
  public void shouldResetBackgroundSchedulerBeforeTests() throws Exception {
    assertThat(Robolectric.getBackgroundThreadScheduler().isPaused()).isFalse();
    Robolectric.getBackgroundThreadScheduler().pause();
  }

  @Test
  public void shouldResetBackgroundSchedulerAfterTests() throws Exception {
    assertThat(Robolectric.getBackgroundThreadScheduler().isPaused()).isFalse();
    Robolectric.getBackgroundThreadScheduler().pause();
  }

  @Test
  public void idleMainLooper_executesScheduledTasks() {
    final boolean[] wasRun = new boolean[]{false};
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        wasRun[0] = true;
      }
    }, 2000);

    assertFalse(wasRun[0]);
    ShadowLooper.idleMainLooper(1999);
    assertFalse(wasRun[0]);
    ShadowLooper.idleMainLooper(1);
    assertTrue(wasRun[0]);
  }

  @Test
  public void shouldUseSetDensityForContexts() throws Exception {
    assertThat(new Activity().getResources().getDisplayMetrics().density).isEqualTo(1.0f);
    ShadowApplication.setDisplayMetricsDensity(1.5f);
    assertThat(new Activity().getResources().getDisplayMetrics().density).isEqualTo(1.5f);
  }

  @Test
  public void shouldUseSetDisplayForContexts() throws Exception {
    assertThat(new Activity().getResources().getDisplayMetrics().widthPixels).isEqualTo(480);
    assertThat(new Activity().getResources().getDisplayMetrics().heightPixels).isEqualTo(800);

    Display display = Shadow.newInstanceOf(Display.class);
    ShadowDisplay shadowDisplay = Shadows.shadowOf(display);
    shadowDisplay.setWidth(100);
    shadowDisplay.setHeight(200);
    ShadowApplication.setDefaultDisplay(display);

    assertThat(new Activity().getResources().getDisplayMetrics().widthPixels).isEqualTo(100);
    assertThat(new Activity().getResources().getDisplayMetrics().heightPixels).isEqualTo(200);
  }

  @Test
  public void clickOn_shouldCallClickListener() throws Exception {
    View view = new View(RuntimeEnvironment.application);
    shadowOf(view).setMyParent(new StubViewRoot());
    TestOnClickListener testOnClickListener = new TestOnClickListener();
    view.setOnClickListener(testOnClickListener);
    ShadowView.clickOn(view);
    assertTrue(testOnClickListener.clicked);
  }

  @Test(expected = ActivityNotFoundException.class)
  public void checkActivities_shouldSetValueOnShadowApplication() throws Exception {
    ShadowApplication.getInstance().checkActivities(true);
    RuntimeEnvironment.application.startActivity(new Intent("i.dont.exist.activity"));
  }

  @Test
  public void setupActivity_returnsAVisibleActivity() throws Exception {
    LifeCycleActivity activity = Robolectric.setupActivity(LifeCycleActivity.class);

    assertThat(activity.isCreated()).isTrue();
    assertThat(activity.isStarted()).isTrue();
    assertThat(activity.isResumed()).isTrue();
    assertThat(activity.isVisible()).isTrue();
  }

  private List<String> order = new ArrayList<>();
  
  private class MockProvider implements ShadowProvider {
    @Override
    public void reset() {
      order.add("shadowProvider");
      assertThat(RuntimeEnvironment.application).as("app during shadow reset").isNotNull();
      assertThat(RuntimeEnvironment.getActivityThread()).as("activityThread during shadow reset").isNotNull();
      assertThat(RuntimeEnvironment.getRobolectricPackageManager()).as("packageManager during shadow reset").isNotNull();
    }

    @Override
    public String[] getProvidedPackageNames() {
      return null;
    }

    @Override
    public Map<String, String> getShadowMap() {
      return null;
    }
  }

  @Test
  public void reset_shouldResetShadows_beforeClearingPackageManager() {
    Iterable<ShadowProvider> oldProviders = ReflectionHelpers.getStaticField(Robolectric.class, "providers");;
    ShadowProvider mockProvider = new MockProvider();
    List<ShadowProvider> mockProviders = Collections.singletonList(mockProvider);
    
    ReflectionHelpers.setStaticField(Robolectric.class, "providers", mockProviders);

    RobolectricPackageManager mockManager = mock(RobolectricPackageManager.class);
    doAnswer(new Answer<Void>() {
      public Void answer(InvocationOnMock invocation) {
        order.add("packageManager");
        return null;
      }
    }).when(mockManager).reset();
    
    RuntimeEnvironment.setRobolectricPackageManager(mockManager);
    
    try {
      Robolectric.reset();
      
    } finally {
      // Make sure we clean up after ourselves
      ReflectionHelpers.setStaticField(Robolectric.class, "providers", oldProviders);
    }
    assertThat(order).as("reset order").containsExactly("shadowProvider", "packageManager");
    assertThat(RuntimeEnvironment.application).as("app after reset").isNull();
    assertThat(RuntimeEnvironment.getPackageManager()).as("packageManager after reset").isNull();
    assertThat(RuntimeEnvironment.getActivityThread()).as("activityThread after reset").isNull();
  }
  
  @Implements(View.class)
  public static class TestShadowView {
    @Implementation
    public Context getContext() {
      return null;
    }
  }

  private static class LifeCycleActivity extends Activity {
    private boolean created;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      created = true;
    }

    @Override
    protected void onStart() {
      super.onStart();
      started = true;
    }

    public boolean isStarted() {
      return started;
    }

    public boolean isCreated() {
      return created;
    }

    public boolean isVisible() {
      return getWindow().getDecorView().getWindowToken() != null;
    }
  }
}
