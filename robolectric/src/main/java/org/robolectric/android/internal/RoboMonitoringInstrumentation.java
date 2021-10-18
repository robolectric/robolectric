package org.robolectric.android.internal;

import static org.robolectric.shadow.api.Shadow.extract;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import androidx.test.internal.runner.intent.IntentMonitorImpl;
import androidx.test.internal.runner.lifecycle.ActivityLifecycleMonitorImpl;
import androidx.test.internal.runner.lifecycle.ApplicationLifecycleMonitorImpl;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.intent.IntentMonitorRegistry;
import androidx.test.runner.intent.IntentStubberRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.ApplicationLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.ApplicationStage;
import androidx.test.runner.lifecycle.Stage;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

/**
 * A Robolectric instrumentation that acts like a slimmed down
 * {@link androidx.test.runner.MonitoringInstrumentation} with only the parts needed for
 * Robolectric.
 */
@SuppressWarnings("RestrictTo")
public class RoboMonitoringInstrumentation extends Instrumentation {

  private static final String TAG = "RoboInstrumentation";

  private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
  private final ActivityLifecycleMonitorImpl lifecycleMonitor = new ActivityLifecycleMonitorImpl();
  private final ApplicationLifecycleMonitorImpl applicationMonitor =
      new ApplicationLifecycleMonitorImpl();
  private final IntentMonitorImpl intentMonitor = new IntentMonitorImpl();

  /**
   * Sets up lifecycle monitoring, and argument registry.
   *
   * <p>Subclasses must call up to onCreate(). This onCreate method does not call start() it is the
   * subclasses responsibility to call start if it desires.
   */
  @Override
  public void onCreate(Bundle arguments) {
    InstrumentationRegistry.registerInstance(this, arguments);
    ActivityLifecycleMonitorRegistry.registerInstance(lifecycleMonitor);
    ApplicationLifecycleMonitorRegistry.registerInstance(applicationMonitor);
    IntentMonitorRegistry.registerInstance(intentMonitor);

    super.onCreate(arguments);
  }

  @Override
  public void setInTouchMode(boolean inTouch) {
    // ignore
  }

  @Override
  public void waitForIdleSync() {
    shadowMainLooper().idle();
  }

  @Override
  public Activity startActivitySync(final Intent intent) {
    return startActivitySyncInternal(intent).get();
  }

  public ActivityController<? extends Activity> startActivitySyncInternal(Intent intent) {
    return startActivitySyncInternal(intent, /* activityOptions= */ null);
  }

  public ActivityController<? extends Activity> startActivitySyncInternal(
      Intent intent, @Nullable Bundle activityOptions) {
    ActivityInfo ai = intent.resolveActivityInfo(getTargetContext().getPackageManager(), 0);
    if (ai == null) {
      throw new RuntimeException("Unable to resolve activity for " + intent
          + " -- see https://github.com/robolectric/robolectric/pull/4736 for details");
    }

    Class<? extends Activity> activityClass;
    String activityClassName = ai.targetActivity != null ? ai.targetActivity : ai.name;
    try {
      activityClass = Class.forName(activityClassName).asSubclass(Activity.class);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load activity " + ai.name, e);
    }

    ActivityController<? extends Activity> controller = Robolectric
        .buildActivity(activityClass, intent, activityOptions)
        .create();
    if (controller.get().isFinishing()) {
      controller.destroy();
    } else {
      controller.start()
          .postCreate(null)
          .resume()
          .visible()
          .windowFocusChanged(true);
    }
    return controller;
  }

  @Override
  public void callApplicationOnCreate(Application app) {
    applicationMonitor.signalLifecycleChange(app, ApplicationStage.PRE_ON_CREATE);
    super.callApplicationOnCreate(app);
    applicationMonitor.signalLifecycleChange(app, ApplicationStage.CREATED);
  }

  @Override
  public void runOnMainSync(Runnable runner) {
    shadowMainLooper().idle();
    runner.run();
  }

  /** {@inheritDoc} */
  @Override
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode,
      Bundle options) {
    intentMonitor.signalIntent(intent);
    ActivityResult ar = stubResultFor(intent);
    if (ar != null) {
      Log.i(TAG, String.format("Stubbing intent %s", intent));
    } else {
      ar = super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
    }
    if (ar != null && target != null) {
      ShadowActivity shadowActivity = extract(target);
      postDispatchActivityResult(shadowActivity, null, requestCode, ar);
    }
    return ar;
  }

  /** This API was added in Android API 23 (M) */
  @Override
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      String target,
      Intent intent,
      int requestCode,
      Bundle options) {
    intentMonitor.signalIntent(intent);
    ActivityResult ar = stubResultFor(intent);
    if (ar != null) {
      Log.i(TAG, String.format("Stubbing intent %s", intent));
    } else {
      ar = super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
    }
    if (ar != null && who instanceof Activity) {
      ShadowActivity shadowActivity = extract(who);
      postDispatchActivityResult(shadowActivity, target, requestCode, ar);
    }
    return ar;
  }

  /** This API was added in Android API 17 (JELLY_BEAN_MR1) */
  @Override
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      String target,
      Intent intent,
      int requestCode,
      Bundle options,
      UserHandle user) {
    ActivityResult ar = stubResultFor(intent);
    if (ar != null) {
      Log.i(TAG, String.format("Stubbing intent %s", intent));
    } else {
      ar =
          super.execStartActivity(
              who, contextThread, token, target, intent, requestCode, options, user);
    }
    if (ar != null && target != null) {
      ShadowActivity shadowActivity = extract(target);
      postDispatchActivityResult(shadowActivity, null, requestCode, ar);
    }
    return ar;
  }

  private void postDispatchActivityResult(
      ShadowActivity shadowActivity, String target, int requestCode, ActivityResult ar) {
    mainThreadHandler.post(
        new Runnable() {
          @Override
          public void run() {
            shadowActivity.internalCallDispatchActivityResult(
                target, requestCode, ar.getResultCode(), ar.getResultData());
          }
        });
  }

  private ActivityResult stubResultFor(Intent intent) {
    if (IntentStubberRegistry.isLoaded()) {
      return IntentStubberRegistry.getInstance().getActivityResultForIntent(intent);
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void execStartActivities(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent[] intents,
      Bundle options) {
    Log.d(TAG, "execStartActivities(context, ibinder, ibinder, activity, intent[], bundle)");
    // For requestCode < 0, the caller doesn't expect any result and
    // in this case we are not expecting any result so selecting
    // a value < 0.
    int requestCode = -1;
    for (Intent intent : intents) {
      execStartActivity(who, contextThread, token, target, intent, requestCode, options);
    }
  }

  @Override
  public boolean onException(Object obj, Throwable e) {
    String error =
        String.format(
            "Exception encountered by: %s. Dumping thread state to "
                + "outputs and pining for the fjords.",
            obj);
    Log.e(TAG, error, e);
    Log.e("THREAD_STATE", getThreadState());
    Log.e(TAG, "Dying now...");
    return super.onException(obj, e);
  }

  protected String getThreadState() {
    Set<Map.Entry<Thread, StackTraceElement[]>> threads = Thread.getAllStackTraces().entrySet();
    StringBuilder threadState = new StringBuilder();
    for (Map.Entry<Thread, StackTraceElement[]> threadAndStack : threads) {
      StringBuilder threadMessage = new StringBuilder("  ").append(threadAndStack.getKey());
      threadMessage.append("\n");
      for (StackTraceElement ste : threadAndStack.getValue()) {
        threadMessage.append(String.format("    %s%n", ste));
      }
      threadMessage.append("\n");
      threadState.append(threadMessage);
    }
    return threadState.toString();
  }

  @Override
  public void callActivityOnDestroy(Activity activity) {
    super.callActivityOnDestroy(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.DESTROYED, activity);
  }

  @Override
  public void callActivityOnRestart(Activity activity) {
    super.callActivityOnRestart(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.RESTARTED, activity);
  }

  @Override
  public void callActivityOnCreate(Activity activity, Bundle bundle) {
    lifecycleMonitor.signalLifecycleChange(Stage.PRE_ON_CREATE, activity);
    super.callActivityOnCreate(activity, bundle);
    lifecycleMonitor.signalLifecycleChange(Stage.CREATED, activity);
  }

  @Override
  public void callActivityOnStart(Activity activity) {
    super.callActivityOnStart(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.STARTED, activity);
  }

  @Override
  public void callActivityOnStop(Activity activity) {
    super.callActivityOnStop(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.STOPPED, activity);
  }

  @Override
  public void callActivityOnResume(Activity activity) {
    super.callActivityOnResume(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.RESUMED, activity);
  }

  @Override
  public void callActivityOnPause(Activity activity) {
    super.callActivityOnPause(activity);
    lifecycleMonitor.signalLifecycleChange(Stage.PAUSED, activity);
  }

  @Override
  public void finish(int resultCode, Bundle bundle) { }

  @Override
  public Context getTargetContext() {
    return RuntimeEnvironment.getApplication();
  }

  @Override
  public Context getContext() {
    return RuntimeEnvironment.getApplication();
  }
}
