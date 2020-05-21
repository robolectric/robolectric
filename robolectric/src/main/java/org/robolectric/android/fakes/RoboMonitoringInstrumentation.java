package org.robolectric.android.fakes;

import static org.robolectric.shadow.api.Shadow.extract;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import androidx.test.runner.MonitoringInstrumentation;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

public class RoboMonitoringInstrumentation extends MonitoringInstrumentation {

  @Override
  protected void specifyDexMakerCacheProperty() {
    // ignore, unnecessary for robolectric
  }

  @Override
  protected void installMultidex() {
    // ignore, unnecessary for robolectric
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
        .buildActivity(activityClass, intent)
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
  public void runOnMainSync(Runnable runner) {
    shadowMainLooper().idle();
    runner.run();
  }

  @Override
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode) {

    ActivityResult ar =
        super.execStartActivity(who, contextThread, token, target, intent, requestCode);
    if (ar != null) {
      ShadowActivity shadowActivity = extract(target);
      new Handler(Looper.getMainLooper())
          .post(
              new Runnable() {
                @Override
                public void run() {
                  shadowActivity.callOnActivityResult(
                      requestCode, ar.getResultCode(), ar.getResultData());
                }
              });
    }
    return ar;
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
    ActivityResult ar =
        super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
    if (ar != null) {
      ShadowActivity shadowActivity = extract(target);
      new Handler(Looper.getMainLooper())
          .post(
              new Runnable() {
                @Override
                public void run() {
                  shadowActivity.callOnActivityResult(
                      requestCode, ar.getResultCode(), ar.getResultData());
                }
              });
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

    ActivityResult ar =
        super.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
    if (ar != null) {
      ShadowActivity shadowActivity = extract(target);
      new Handler(Looper.getMainLooper())
          .post(
              new Runnable() {
                @Override
                public void run() {
                  shadowActivity.callOnActivityResult(
                      requestCode, ar.getResultCode(), ar.getResultData());
                }
              });
    }
    return ar;
  }

  /** This API was added in Android API 17 (JELLY_BEAN_MR1) */
  @Override
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode,
      Bundle options,
      UserHandle user) {
    ActivityResult ar =
        super.execStartActivity(
            who, contextThread, token, target, intent, requestCode, options, user);
    if (ar != null) {
      ShadowActivity shadowActivity = extract(target);
      new Handler(Looper.getMainLooper())
          .post(
              new Runnable() {
                @Override
                public void run() {
                  shadowActivity.callOnActivityResult(
                      requestCode, ar.getResultCode(), ar.getResultData());
                }
              });
    }
    return ar;
  }

  @Override
  public void finish(int resultCode, Bundle bundle) {
    // intentionally don't call through to super here, to circumvent all the activity
    // waiting/cleanup
    // logic that is unnecessary on Robolectric
    super.restoreUncaughtExceptionHandler();
  }
}
