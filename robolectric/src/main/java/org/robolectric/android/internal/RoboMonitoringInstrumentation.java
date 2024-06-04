package org.robolectric.android.internal;

import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.extract;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.util.DisplayMetrics;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowInstrumentation;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPausedLooper;

/**
 * A Robolectric instrumentation that acts like a slimmed down {@link
 * androidx.test.runner.MonitoringInstrumentation} with only the parts needed for Robolectric.
 */
@SuppressWarnings("RestrictTo")
public class RoboMonitoringInstrumentation extends Instrumentation {

  private static final String TAG = "RoboInstrumentation";

  private final ActivityLifecycleMonitorImpl lifecycleMonitor = new ActivityLifecycleMonitorImpl();
  private final ApplicationLifecycleMonitorImpl applicationMonitor =
      new ApplicationLifecycleMonitorImpl();
  private final IntentMonitorImpl intentMonitor = new IntentMonitorImpl();
  private final List<ActivityController<?>> createdActivities = new ArrayList<>();

  private final AtomicBoolean attachedConfigListener = new AtomicBoolean();

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
  public void waitForIdleSync() {
    shadowOf(Looper.getMainLooper()).idle();
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
      throw new RuntimeException(
          "Unable to resolve activity for "
              + intent
              + " -- see https://github.com/robolectric/robolectric/pull/4736 for details");
    }

    Class<? extends Activity> activityClass;
    String activityClassName = ai.targetActivity != null ? ai.targetActivity : ai.name;
    try {
      activityClass = Class.forName(activityClassName).asSubclass(Activity.class);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load activity " + ai.name, e);
    }

    if (attachedConfigListener.compareAndSet(false, true) && !willCreateActivityContexts()) {
      // To avoid infinite recursion listen to the system resources, this will be updated before
      // the application resources but because activities use the application resources they will
      // get updated by the first activity (via updateConfiguration).
      shadowOf(Resources.getSystem()).addConfigurationChangeListener(this::updateConfiguration);
    }

    AtomicReference<ActivityController<? extends Activity>> activityControllerReference =
        new AtomicReference<>();
    ShadowInstrumentation.runOnMainSyncNoIdle(
        () -> {
          ActivityController<? extends Activity> controller =
              Robolectric.buildActivity(activityClass, intent, activityOptions);
          activityControllerReference.set(controller);
          controller.create();
          if (controller.get().isFinishing()) {
            controller.destroy();
          } else {
            createdActivities.add(controller);
            controller
                .start()
                .postCreate(null)
                .resume()
                .visible()
                .windowFocusChanged(true)
                .topActivityResumed(true);
          }
        });
    return activityControllerReference.get();
  }

  @Override
  public void callApplicationOnCreate(Application app) {
    if (willCreateActivityContexts()) {
      shadowOf(app.getResources()).addConfigurationChangeListener(this::updateConfiguration);
    }
    applicationMonitor.signalLifecycleChange(app, ApplicationStage.PRE_ON_CREATE);
    super.callApplicationOnCreate(app);
    applicationMonitor.signalLifecycleChange(app, ApplicationStage.CREATED);
  }

  /**
   * Executes a runnable on the main thread, blocking until it is complete.
   *
   * <p>When in INSTUMENTATION_TEST Looper mode, the runnable is posted to the main handler and the
   * caller's thread blocks until that runnable has finished. When a Throwable is thrown in the
   * runnable, the exception is propagated back to the caller's thread. If it is an unchecked
   * throwable, it will be rethrown as is. If it is a checked exception, it will be rethrown as a
   * {@link RuntimeException}.
   *
   * <p>For other Looper modes, the main looper is idled and then the runnable is executed in the
   * caller's thread.
   *
   * @param runnable a runnable to be executed on the main thread
   */
  @Override
  public void runOnMainSync(Runnable runnable) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.INSTRUMENTATION_TEST) {
      FutureTask<Void> wrapped = new FutureTask<>(runnable, null);
      Shadow.<ShadowPausedLooper>extract(Looper.getMainLooper()).postSync(wrapped);
      try {
        wrapped.get();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        } else if (cause instanceof Error) {
          throw (Error) cause;
        }
        throw new RuntimeException(cause);
      }
    } else {
      // TODO: Use ShadowPausedLooper#postSync for PAUSED looper mode which provides more realistic
      //  behavior (i.e. it only runs to the runnable, it doesn't completely idle).
      waitForIdleSync();
      runnable.run();
    }
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
    return null;
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
    return null;
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
    return null;
  }

  private void postDispatchActivityResult(
      ShadowActivity shadowActivity, String target, int requestCode, ActivityResult ar) {
    new Handler(Looper.getMainLooper())
        .post(
            new Runnable() {
              @Override
              public void run() {
                shadowActivity.internalCallDispatchActivityResult(
                    target, requestCode, ar.getResultCode(), ar.getResultData());
              }
            });
  }

  private ActivityResult stubResultFor(Intent intent) {
    if (!IntentStubberRegistry.isLoaded()) {
      return null;
    }

    FutureTask<ActivityResult> task =
        new FutureTask<ActivityResult>(
            new Callable<ActivityResult>() {
              @Override
              public ActivityResult call() throws Exception {
                return IntentStubberRegistry.getInstance().getActivityResultForIntent(intent);
              }
            });
    ShadowInstrumentation.runOnMainSyncNoIdle(task);

    try {
      return task.get();
    } catch (ExecutionException e) {
      String msg = String.format("Could not retrieve stub result for intent %s", intent);
      // Preserve original exception
      if (e.getCause() instanceof RuntimeException) {
        Log.w(TAG, msg, e);
        throw (RuntimeException) e.getCause();
      } else if (e.getCause() != null) {
        throw new RuntimeException(msg, e.getCause());
      } else {
        throw new RuntimeException(msg, e);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
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
    if (activity.isFinishing()) {
      createdActivities.removeIf(controller -> controller.get() == activity);
    }
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
  public void finish(int resultCode, Bundle bundle) {}

  @Override
  public Context getTargetContext() {
    return RuntimeEnvironment.getApplication();
  }

  @Override
  public Context getContext() {
    return RuntimeEnvironment.getApplication();
  }

  private void updateConfiguration(
      Configuration oldConfig, Configuration newConfig, DisplayMetrics newMetrics) {
    int changedConfig = oldConfig.diff(newConfig);
    List<ActivityController<?>> controllers = new ArrayList<>(createdActivities);
    for (ActivityController<?> controller : controllers) {
      if (createdActivities.contains(controller)) {
        Activity activity = controller.get();
        controller.configurationChange(newConfig, newMetrics, changedConfig);
        // If the activity is recreated then make the new activity visible, this should be done by
        // configurationChange but there's a pre-existing TODO to address this and it will require
        // more work to make it function correctly.
        if (controller.get() != activity) {
          controller.visible();
        }
      }
    }
  }

  private static boolean willCreateActivityContexts() {
    return RuntimeEnvironment.getApiLevel() >= O
        && Boolean.getBoolean("robolectric.createActivityContexts");
  }
}
