package org.robolectric.android.fakes;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.extract;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import androidx.test.runner.MonitoringInstrumentation;
import org.robolectric.Robolectric;
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
    shadowOf(Looper.getMainLooper()).idle();
  }

  @Override
  public Activity startActivitySync(final Intent intent) {
    ActivityInfo ai = intent.resolveActivityInfo(getTargetContext().getPackageManager(), 0);
    try {
      Class<? extends Activity> activityClass = Class.forName(ai.name).asSubclass(Activity.class);
      return Robolectric.buildActivity(activityClass, intent)
          .create()
          .postCreate(null)
          .start()
          .resume()
          .postResume()
          .visible()
          .windowFocusChanged(true)
          .get();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not load activity " + ai.name, e);
    }
  }

  @Override
  public void runOnMainSync(Runnable runner) {
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
      shadowActivity.callOnActivityResult(requestCode, ar.getResultCode(), ar.getResultData());
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
      shadowActivity.callOnActivityResult(requestCode, ar.getResultCode(), ar.getResultData());
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
      shadowActivity.callOnActivityResult(requestCode, ar.getResultCode(), ar.getResultData());
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
      shadowActivity.callOnActivityResult(requestCode, ar.getResultCode(), ar.getResultData());
    }
    return ar;
  }
}
