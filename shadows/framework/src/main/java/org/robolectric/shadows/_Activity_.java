package org.robolectric.shadows;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/** Accessor interface for {@link Activity}'s internals. */
@ForType(Activity.class)
public interface _Activity_ {

  @Accessor("mToken")
  IBinder getToken();

  // <= KITKAT:
  void attach(
      Context context,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      IBinder token,
      int ident,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      CharSequence title,
      Activity parent,
      String id,
      @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances,
      Configuration configuration);

  // <= LOLLIPOP:
  void attach(
      Context context,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      IBinder token,
      int ident,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      CharSequence title,
      Activity parent,
      String id,
      @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances,
      Configuration configuration,
      @WithType("com.android.internal.app.IVoiceInteractor") Object iVoiceInteractor);

  // <= M
  void attach(
      Context context,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      IBinder token,
      int ident,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      CharSequence title,
      Activity parent,
      String id,
      @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances,
      Configuration configuration,
      String referer,
      @WithType("com.android.internal.app.IVoiceInteractor") Object iVoiceInteractor);

  // <= N_MR1
  void attach(
      Context context,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      IBinder token,
      int ident,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      CharSequence title,
      Activity parent,
      String id,
      @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances,
      Configuration configuration,
      String referer,
      @WithType("com.android.internal.app.IVoiceInteractor") Object iVoiceInteractor,
      Window window);

  // <= P
  void attach(
      Context context,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      IBinder token,
      int ident,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      CharSequence title,
      Activity parent,
      String id,
      @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances,
      Configuration configuration,
      String referer,
      @WithType("com.android.internal.app.IVoiceInteractor") Object iVoiceInteractor,
      Window window,
      @WithType("android.view.ViewRootImpl$ActivityConfigCallback") Object activityConfigCallback);

  // <= R
  void attach(
      Context context,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      IBinder token,
      int ident,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      CharSequence title,
      Activity parent,
      String id,
      @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances,
      Configuration configuration,
      String referer,
      @WithType("com.android.internal.app.IVoiceInteractor") Object iVoiceInteractor,
      Window window,
      @WithType("android.view.ViewRootImpl$ActivityConfigCallback") Object activityConfigCallback,
      IBinder assistToken);

  // >= S
  void attach(
      Context context,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      IBinder token,
      int ident,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      CharSequence title,
      Activity parent,
      String id,
      @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances,
      Configuration configuration,
      String referer,
      @WithType("com.android.internal.app.IVoiceInteractor") Object iVoiceInteractor,
      Window window,
      @WithType("android.view.ViewRootImpl$ActivityConfigCallback") Object activityConfigCallback,
      IBinder assistToken,
      IBinder shareableActivityToken);

  default void callAttach(
      Activity realActivity,
      Context baseContext,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      IBinder token,
      CharSequence activityTitle,
      @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances) {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel <= Build.VERSION_CODES.KITKAT) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          token,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          null,
          lastNonConfigurationInstances,
          application.getResources().getConfiguration());
    } else if (apiLevel <= Build.VERSION_CODES.LOLLIPOP) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          token,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          null,
          lastNonConfigurationInstances,
          application.getResources().getConfiguration(),
          null);
    } else if (apiLevel <= Build.VERSION_CODES.M) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          token,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          null,
          lastNonConfigurationInstances,
          application.getResources().getConfiguration(),
          "referrer",
          null);
    } else if (apiLevel <= Build.VERSION_CODES.N_MR1) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          token,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          null,
          lastNonConfigurationInstances,
          application.getResources().getConfiguration(),
          "referrer",
          null,
          null);
    } else if (apiLevel <= Build.VERSION_CODES.P) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          token,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          null,
          lastNonConfigurationInstances,
          application.getResources().getConfiguration(),
          "referrer",
          null,
          null,
          null);
    } else if (apiLevel <= VERSION_CODES.R) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          token,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          null,
          lastNonConfigurationInstances,
          application.getResources().getConfiguration(),
          "referrer",
          null,
          null,
          null,
          null);
    } else if (apiLevel > Build.VERSION_CODES.R) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          token,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          null,
          lastNonConfigurationInstances,
          application.getResources().getConfiguration(),
          "referrer",
          null,
          null,
          null,
          null,
          null);
    }
    Shadow.<ShadowActivityThread>extract(activityThread)
        .registerActivityLaunch(intent, activityInfo, realActivity, token);
  }

  void performCreate(Bundle icicle);

  void performDestroy();

  void performPause();

  void performRestart();

  void performRestart(boolean start, String reason);

  void performRestoreInstanceState(Bundle savedInstanceState);

  void performResume();

  void performResume(boolean followedByPause, String reason);

  void performTopResumedActivityChanged(boolean isTopResumedActivity, String reason);

  void performSaveInstanceState(Bundle outState);

  void performStart();

  void performStart(String reason);

  void performStop();

  void performStop(boolean preserveWindow);

  void performStop(boolean preserveWindow, String reason);

  void onPostCreate(Bundle savedInstanceState);

  void onPostResume();

  void makeVisible();

  void onNewIntent(Intent intent);

  void onActivityResult(int requestCode, int resultCode, Intent data);

  void dispatchActivityResult(String who, int requestCode, int resultCode, Intent data);

  void dispatchActivityResult(
      String who, int requestCode, int resultCode, Intent data, String type);

  Dialog onCreateDialog(int id);

  void onPrepareDialog(int id, Dialog dialog, Bundle args);

  void onPrepareDialog(int id, Dialog dialog);

  Object retainNonConfigurationInstances();

  @Accessor("mApplication")
  void setApplication(Application application);

  @Accessor("mDecor")
  void setDecor(View decorView);

  @Accessor("mFinished")
  void setFinished(boolean finished);

  @Accessor("mLastNonConfigurationInstances")
  void setLastNonConfigurationInstances(Object nonConfigInstance);

  void setVoiceInteractor(
      @WithType("com.android.internal.app.IVoiceInteractor") Object voiceInteractor);

  @Accessor("mWindowAdded")
  boolean getWindowAdded();

  @Accessor("mWindow")
  void setWindow(Window window);

  @Accessor("mChangingConfigurations")
  void setChangingConfigurations(boolean value);

  @Accessor("mConfigChangeFlags")
  void setConfigChangeFlags(int value);

  @Accessor("mInstrumentation")
  Instrumentation getInstrumentation();
}
