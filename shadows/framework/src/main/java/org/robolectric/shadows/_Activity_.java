package org.robolectric.shadows;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/** Accessor interface for {@link Activity}'s internals. */
@ForType(Activity.class)
public interface _Activity_ {

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

  // => O
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

  default void callAttach(
      Context baseContext,
      ActivityThread activityThread,
      Instrumentation instrumentation,
      Application application,
      Intent intent,
      ActivityInfo activityInfo,
      CharSequence activityTitle) {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel <= Build.VERSION_CODES.KITKAT) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          null,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          "id",
          null,
          application.getResources().getConfiguration());
    } else if (apiLevel <= Build.VERSION_CODES.LOLLIPOP) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          null,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          "id",
          null,
          application.getResources().getConfiguration(),
          null);
    } else if (apiLevel <= Build.VERSION_CODES.M) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          null,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          "id",
          null,
          application.getResources().getConfiguration(),
          "referrer",
          null);
    } else if (apiLevel <= Build.VERSION_CODES.N_MR1) {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          null,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          "id",
          null,
          application.getResources().getConfiguration(),
          "referrer",
          null,
          null);
    } else {
      attach(
          baseContext,
          activityThread,
          instrumentation,
          null,
          0,
          application,
          intent,
          activityInfo,
          activityTitle,
          null,
          "id",
          null,
          application.getResources().getConfiguration(),
          "referrer",
          null,
          null,
          null);
    }
  }

  void performCreate(Bundle icicle);

  void performDestroy();

  void performPause();

  void performRestoreInstanceState(Bundle savedInstanceState);

  void performResume();

  void performResume(boolean followedByPause, String reason);

  void performSaveInstanceState(Bundle outState);

  void performStart();

  void performStart(String reason);

  void performStop();

  void performStop(boolean preserveWindow);

  void performStop(boolean preserveWindow, String reason);

  void onPostCreate(Bundle savedInstanceState);

  void onPostResume();

  Object retainNonConfigurationInstances();

  @Accessor("mApplication")
  void setApplication(Application application);

  @Accessor("mDecor")
  void setDecor(View decorView);

  @Accessor("mLastNonConfigurationInstances")
  void setLastNonConfigurationInstances(Object nonConfigInstance);

  @Accessor("mWindow")
  void setWindow(Window window);
}
