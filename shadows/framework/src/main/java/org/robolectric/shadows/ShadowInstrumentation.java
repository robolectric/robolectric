package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;

import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityResult;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity.IntentForResult;

@Implements(value = Instrumentation.class, looseSignatures = true)
public class ShadowInstrumentation {

  private List<Intent> startedActivities = new ArrayList<>();
  private List<IntentForResult> startedActivitiesForResults = new ArrayList<>();
  private Map<FilterComparison, Integer> intentRequestCodeMap = new HashMap<>();

  private boolean checkActivities;

  @Implementation
  public Activity startActivitySync(Intent intent, Bundle options) {
    throw new UnsupportedOperationException("Implement me!!");
  }

  @Implementation
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode,
      Bundle options) {
    verifyActivityInManifest(intent);
    return logStartedActivity(intent, requestCode, options);
  }

  @Implementation(maxSdk = LOLLIPOP_MR1)
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Fragment target,
      Intent intent,
      int requestCode,
      Bundle options) {
    verifyActivityInManifest(intent);
    return logStartedActivity(intent, requestCode, options);
  }

  private ActivityResult logStartedActivity(Intent intent, int requestCode, Bundle options) {
    startedActivities.add(intent);
    intentRequestCodeMap.put(new FilterComparison(intent), requestCode);
    startedActivitiesForResults.add(new IntentForResult(intent, requestCode, options));
    return null;
  }

  private void verifyActivityInManifest(Intent intent) {
    if (checkActivities
        && RuntimeEnvironment.application.getPackageManager().resolveActivity(intent, -1) == null) {
      throw new ActivityNotFoundException(intent.getAction());
    }
  }

  @Implementation
  public void execStartActivities(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent[] intents,
      Bundle options) {
    for (Intent intent : intents) {
      execStartActivity(who, contextThread, token, target, intent, -1, options);
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  public void execStartActivityFromAppTask(
      Context who, IBinder contextThread, Object appTask, Intent intent, Bundle options) {
    throw new UnsupportedOperationException("Implement me!!");
  }

  @Implementation(minSdk = M)
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      String target,
      Intent intent,
      int requestCode,
      Bundle options) {
    verifyActivityInManifest(intent);
    return logStartedActivity(intent, requestCode, options);
  }

  @Implementation
  public ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      String resultWho,
      Intent intent,
      int requestCode,
      Bundle options,
      UserHandle user) {
    throw new UnsupportedOperationException("Implement me!!");
  }

  @Implementation
  public ActivityResult execStartActivityAsCaller(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode,
      Bundle options,
      boolean ignoreTargetSecurity,
      int userId) {
    throw new UnsupportedOperationException("Implement me!!");
  }

  Intent getNextStartedActivity() {
    if (startedActivities.isEmpty()) {
      return null;
    } else {
      return startedActivities.remove(startedActivities.size() - 1);
    }
  }

  Intent peekNextStartedActivity() {
    if (startedActivities.isEmpty()) {
      return null;
    } else {
      return startedActivities.get(startedActivities.size() - 1);
    }
  }

  IntentForResult getNextStartedActivityForResult() {
    if (startedActivitiesForResults.isEmpty()) {
      return null;
    } else {
      return startedActivitiesForResults.remove(startedActivitiesForResults.size() - 1);
    }
  }

  IntentForResult peekNextStartedActivityForResult() {
    if (startedActivitiesForResults.isEmpty()) {
      return null;
    } else {
      return startedActivitiesForResults.get(startedActivitiesForResults.size() - 1);
    }
  }

  void checkActivities(boolean checkActivities) {
    this.checkActivities = checkActivities;
  }

  int getRequestCodeForIntent(Intent requestIntent) {
    Integer requestCode = intentRequestCodeMap.get(new Intent.FilterComparison(requestIntent));
    if (requestCode == null) {
      throw new RuntimeException(
          "No intent matches " + requestIntent + " among " + intentRequestCodeMap.keySet());
    }
    return requestCode;
  }
}
