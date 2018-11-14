package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowView;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class RobolectricTest {

  private PrintStream originalSystemOut;
  private ByteArrayOutputStream buff;
  private String defaultLineSeparator;
  private Application context;

  @Before
  public void setUp() {
    originalSystemOut = System.out;
    defaultLineSeparator = System.getProperty("line.separator");

    System.setProperty("line.separator", "\n");
    buff = new ByteArrayOutputStream();
    PrintStream testOut = new PrintStream(buff);
    System.setOut(testOut);
    context = ApplicationProvider.getApplicationContext();
  }

  @After
  public void tearDown() throws Exception {
    System.setProperty("line.separator", defaultLineSeparator);
    System.setOut(originalSystemOut);
  }

  @Test(expected = RuntimeException.class)
  public void clickOn_shouldThrowIfViewIsDisabled() throws Exception {
    View view = new View(context);
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
  public void clickOn_shouldCallClickListener() throws Exception {
    View view = new View(context);
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class));
    OnClickListener testOnClickListener = mock(OnClickListener.class);
    view.setOnClickListener(testOnClickListener);
    ShadowView.clickOn(view);

    verify(testOnClickListener).onClick(view);
  }

  @Test(expected = ActivityNotFoundException.class)
  public void checkActivities_shouldSetValueOnShadowApplication() throws Exception {
    ShadowApplication.getInstance().checkActivities(true);
    context.startActivity(
        new Intent("i.dont.exist.activity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  }

  @Test @Config(sdk = 16)
  public void setupActivity_returnsAVisibleActivity() throws Exception {
    LifeCycleActivity activity = Robolectric.setupActivity(LifeCycleActivity.class);

    assertThat(activity.isCreated()).isTrue();
    assertThat(activity.isStarted()).isTrue();
    assertThat(activity.isResumed()).isTrue();
    assertThat(activity.isVisible()).isTrue();
  }

  @Implements(View.class)
  public static class TestShadowView {
    @Implementation
    protected Context getContext() {
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
