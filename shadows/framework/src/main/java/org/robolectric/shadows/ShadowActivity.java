package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.AnimRes;
import android.annotation.ColorInt;
import android.annotation.RequiresApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Dialog;
import android.app.DirectAction;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.LocusId;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Binder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.android.internal.app.IVoiceInteractor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.internal.WindowConfigurations;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.fakes.RoboIntentSender;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.fakes.RoboSplashScreen;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowContextImpl.ContextImplReflector;
import org.robolectric.shadows.ShadowInstrumentation.TargetAndRequestCode;
import org.robolectric.shadows.ShadowLoadedApk.LoadedApkReflector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

@SuppressWarnings("NewApi")
@Implements(Activity.class)
public class ShadowActivity extends ShadowContextThemeWrapper {

  @RealObject protected Activity realActivity;

  private int resultCode;
  private Intent resultIntent;
  private Activity parent;
  private int requestedOrientation = -1;
  private View currentFocus;
  private Integer lastShownDialogId = null;
  private int pendingTransitionEnterAnimResId = -1;
  private int pendingTransitionExitAnimResId = -1;
  private final SparseArray<OverriddenActivityTransition> overriddenActivityTransitions =
      new SparseArray<>();
  private Object lastNonConfigurationInstance;
  private final Map<Integer, Dialog> dialogForId = new HashMap<>();
  private final ArrayList<Cursor> managedCursors = new ArrayList<>();
  private int mDefaultKeyMode = Activity.DEFAULT_KEYS_DISABLE;
  private SpannableStringBuilder mDefaultKeySsb = null;
  private int streamType = -1;
  private boolean mIsTaskRoot = true;
  private Menu optionsMenu;
  private ComponentName callingActivity;
  private PermissionsRequest lastRequestedPermission;
  private ActivityController controller;
  private boolean inMultiWindowMode = false;
  private IntentSenderRequest lastIntentSenderRequest;
  private boolean throwIntentSenderException;
  private boolean hasReportedFullyDrawn = false;
  private boolean isInPictureInPictureMode = false;
  private Object splashScreen = null;
  private boolean showWhenLocked = false;
  private boolean turnScreenOn = false;
  private boolean isTaskMovedToBack = false;
  private LocusId lastLocusContextId;
  private Bundle lastLocusContextExtras;

  public void setApplication(Application application) {
    reflector(ActivityReflector.class, realActivity).setApplication(application);
  }

  public void callAttach(Intent intent) {
    callAttach(intent, /* activityOptions= */ null, /* lastNonConfigurationInstances= */ null);
  }

  public void callAttach(Intent intent, @Nullable Bundle activityOptions) {
    callAttach(
        intent, /* activityOptions= */ activityOptions, /* lastNonConfigurationInstances= */ null);
  }

  public void callAttach(
      Intent intent,
      @Nullable Bundle activityOptions,
      @Nullable @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances) {
    callAttach(
        intent,
        /* activityOptions= */ activityOptions,
        /* lastNonConfigurationInstances= */ null,
        /* overrideConfig= */ null);
  }

  public void callAttach(
      Intent intent,
      @Nullable Bundle activityOptions,
      @Nullable @WithType("android.app.Activity$NonConfigurationInstances")
          Object lastNonConfigurationInstances,
      @Nullable Configuration overrideConfig) {
    Application application = RuntimeEnvironment.getApplication();
    Context baseContext = application.getBaseContext();

    ComponentName componentName =
        new ComponentName(application.getPackageName(), realActivity.getClass().getName());
    ActivityInfo activityInfo;
    PackageManager packageManager = application.getPackageManager();
    shadowOf(packageManager).addActivityIfNotPresent(componentName);
    try {
      activityInfo = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      throw new RuntimeException("Activity is not resolved even if we made sure it exists", e);
    }
    Binder token = new Binder();

    CharSequence activityTitle = activityInfo.loadLabel(baseContext.getPackageManager());

    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    Instrumentation instrumentation = activityThread.getInstrumentation();

    if (RuntimeEnvironment.getApiLevel() >= O_MR1) {
      // ActivityInfo.FLAG_SHOW_WHEN_LOCKED
      showWhenLocked = (activityInfo.flags & 0x800000) != 0;
    }

    Context activityContext;
    int displayId =
        activityOptions != null
            ? ActivityOptions.fromBundle(activityOptions).getLaunchDisplayId()
            : Display.DEFAULT_DISPLAY;
    // There's no particular reason to only do this above O, however the createActivityContext
    // method signature changed between versions so just for convenience only the latest version is
    // plumbed through, older versions will use the previous robolectric behavior of sharing
    // activity and application ContextImpl objects.
    // TODO(paulsowden): This should be enabled always but many service shadows are storing instance
    //  state that should be represented globally, we'll have to update these one by one to use
    //  static (i.e. global) state instead of instance state. For now enable only when the display
    //  is requested to a non-default display which requires a separate context to function
    //  properly.
    int launchDisplayId =
        displayId == Display.INVALID_DISPLAY ? Display.DEFAULT_DISPLAY : displayId;
    // An activity that can't be in multi-window mode fills its display, whatever bounds it is
    // launched with.
    Rect launchBounds =
        activityOptions != null
                && RuntimeEnvironment.getApiLevel() >= P
                && WindowConfigurations.supportsMultiWindow(activityInfo, launchDisplayId)
            ? ActivityOptions.fromBundle(activityOptions).getLaunchBounds()
            : null;
    // An activity started adjacent to another launches in the other half of split screen.
    Configuration adjacentLaunchOverrideConfig =
        overrideConfig == null
            ? Shadow.<ShadowInstrumentation>extract(instrumentation)
                .takeAdjacentLaunchOverrideConfiguration(intent, activityInfo, launchDisplayId)
            : null;
    // An activity that fills its display launches in the orientation it declares: its display
    // rotates, or it is letterboxed on a display that ignores orientation requests.
    requestedOrientation = activityInfo.screenOrientation;
    Configuration letterboxOverrideConfig = null;
    if (overrideConfig == null && launchBounds == null && adjacentLaunchOverrideConfig == null) {
      letterboxOverrideConfig =
          WindowConfigurations.getLetterboxOverrideConfiguration(
              application.getApplicationInfo(), requestedOrientation, launchDisplayId);
      if (letterboxOverrideConfig == null) {
        rotateDisplayToRequestedOrientation(launchDisplayId);
      }
    }
    if ((Boolean.getBoolean("robolectric.createActivityContexts")
            || (displayId != Display.DEFAULT_DISPLAY && displayId != Display.INVALID_DISPLAY)
            || launchBounds != null
            || adjacentLaunchOverrideConfig != null
            || letterboxOverrideConfig != null)
        && RuntimeEnvironment.getApiLevel() >= O) {
      LoadedApk loadedApk =
          activityThread.getPackageInfo(
              baseContext.getApplicationInfo(), null, Context.CONTEXT_INCLUDE_CODE);
      LoadedApkReflector loadedApkReflector = reflector(LoadedApkReflector.class, loadedApk);
      loadedApkReflector.setResources(application.getResources());
      loadedApkReflector.setApplication(application);
      // The window manager gives an activity the configuration of its window: a window of its own,
      // such as a freeform window, or one filling the display it is on.
      Configuration activityOverrideConfig =
          overrideConfig != null
              ? overrideConfig
              : adjacentLaunchOverrideConfig != null
                  ? adjacentLaunchOverrideConfig
                  : launchBounds != null
                      ? WindowConfigurations.getFreeformOverrideConfiguration(
                          launchDisplayId, launchBounds, activityInfo)
                      : letterboxOverrideConfig != null
                          ? letterboxOverrideConfig
                          : WindowConfigurations.getDisplayOverrideConfiguration(displayId);
      activityContext =
          reflector(ContextImplReflector.class)
              .createActivityContext(
                  activityThread,
                  loadedApk,
                  activityInfo,
                  token,
                  displayId,
                  activityOverrideConfig);
      reflector(ContextImplReflector.class, activityContext).setOuterContext(realActivity);
      // This is not what the SDK does but for backwards compatibility with previous versions of
      // robolectric, which did not use a separate activity context, move the theme from the
      // application context. (Previously tests would configure the theme on the application context
      // with the expectation that it modify the activity.)
      if (baseContext.getThemeResId() != 0) {
        activityContext.setTheme(baseContext.getThemeResId());
      }
    } else {
      activityContext = baseContext;
    }

    reflector(ActivityReflector.class, realActivity)
        .callAttach(
            realActivity,
            activityContext,
            activityThread,
            instrumentation,
            application,
            intent,
            activityInfo,
            token,
            activityTitle,
            lastNonConfigurationInstances);
    if (activityContext != baseContext) {
      // Activity#attach records the global configuration, but the activity's own context can have
      // another one, such as the configuration of the display it was launched on.
      Configuration activityConfig = activityContext.getResources().getConfiguration();
      reflector(ActivityReflector.class, realActivity).getCurrentConfig().setTo(activityConfig);
      if (WindowConfigurations.isInMultiWindowMode(activityConfig)) {
        inMultiWindowMode = true;
      }
      if (WindowConfigurations.isInPictureInPictureMode(activityConfig)) {
        isInPictureInPictureMode = true;
      }
    }

    int theme = activityInfo.getThemeResource();
    if (theme != 0) {
      realActivity.setTheme(theme);
    }
  }

  /**
   * Sets the calling activity that will be reflected in {@link Activity#getCallingActivity} and
   * {@link Activity#getCallingPackage}.
   */
  public void setCallingActivity(@Nullable ComponentName activityName) {
    callingActivity = activityName;
  }

  @Implementation
  protected ComponentName getCallingActivity() {
    return callingActivity;
  }

  /**
   * Sets the calling package that will be reflected in {@link Activity#getCallingActivity} and
   * {@link Activity#getCallingPackage}.
   *
   * <p>Activity name defaults to some default value.
   */
  public void setCallingPackage(@Nullable String packageName) {
    if (callingActivity != null && callingActivity.getPackageName().equals(packageName)) {
      // preserve the calling activity as it was, so non-conflicting setCallingActivity followed by
      // setCallingPackage will not erase previously set information.
      return;
    }
    callingActivity =
        packageName != null ? new ComponentName(packageName, "unknown.Activity") : null;
  }

  @Implementation
  protected String getCallingPackage() {
    return callingActivity != null ? callingActivity.getPackageName() : null;
  }

  @Implementation
  protected void setDefaultKeyMode(int keyMode) {
    mDefaultKeyMode = keyMode;

    // Some modes use a SpannableStringBuilder to track & dispatch input events
    // This list must remain in sync with the switch in onKeyDown()
    switch (mDefaultKeyMode) {
      case Activity.DEFAULT_KEYS_DISABLE:
      case Activity.DEFAULT_KEYS_SHORTCUT:
        mDefaultKeySsb = null; // not used in these modes
        break;
      case Activity.DEFAULT_KEYS_DIALER:
      case Activity.DEFAULT_KEYS_SEARCH_LOCAL:
      case Activity.DEFAULT_KEYS_SEARCH_GLOBAL:
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  public int getDefaultKeymode() {
    return mDefaultKeyMode;
  }

  @Implementation(minSdk = O_MR1)
  protected void setShowWhenLocked(boolean showWhenLocked) {
    this.showWhenLocked = showWhenLocked;
  }

  @RequiresApi(api = O_MR1)
  public boolean getShowWhenLocked() {
    return showWhenLocked;
  }

  @Implementation(minSdk = O_MR1)
  protected void setTurnScreenOn(boolean turnScreenOn) {
    this.turnScreenOn = turnScreenOn;
  }

  @RequiresApi(api = O_MR1)
  public boolean getTurnScreenOn() {
    return turnScreenOn;
  }

  @Implementation
  protected void setResult(int resultCode) {
    this.resultCode = resultCode;
  }

  @Implementation
  protected void setResult(int resultCode, Intent data) {
    this.resultCode = resultCode;
    this.resultIntent = data;
  }

  @Implementation
  protected LayoutInflater getLayoutInflater() {
    return LayoutInflater.from(realActivity);
  }

  @Implementation
  protected MenuInflater getMenuInflater() {
    return new MenuInflater(realActivity);
  }

  /**
   * Checks to ensure that the{@code contentView} has been set
   *
   * @param id ID of the view to find
   * @return the view
   * @throws RuntimeException if the {@code contentView} has not been called first
   */
  @Implementation
  protected View findViewById(int id) {
    return getWindow().findViewById(id);
  }

  @Implementation
  protected Activity getParent() {
    return parent;
  }

  /**
   * Allow setting of Parent fragmentActivity (for unit testing purposes only)
   *
   * @param parent Parent fragmentActivity to set on this fragmentActivity
   */
  @HiddenApi
  @Implementation
  public void setParent(Activity parent) {
    this.parent = parent;
  }

  @Implementation
  protected void onBackPressed() {
    finish();
  }

  @Implementation
  protected void finish() {
    // Sets the mFinished field in the real activity so NoDisplay activities can be tested.
    reflector(ActivityReflector.class, realActivity).setFinished(true);
  }

  @Implementation
  protected void finishAndRemoveTask() {
    // Sets the mFinished field in the real activity so NoDisplay activities can be tested.
    reflector(ActivityReflector.class, realActivity).setFinished(true);
  }

  @Implementation
  protected void finishAffinity() {
    // Sets the mFinished field in the real activity so NoDisplay activities can be tested.
    reflector(ActivityReflector.class, realActivity).setFinished(true);
  }

  public void resetIsFinishing() {
    reflector(ActivityReflector.class, realActivity).setFinished(false);
  }

  /**
   * Returns whether {@link #finish()} was called.
   *
   * <p>Note: this method seems redundant, but removing it will cause problems for Mockito spies of
   * Activities that call {@link Activity#finish()} followed by {@link Activity#isFinishing()}. This
   * is because `finish` modifies the members of {@link ShadowActivity#realActivity}, so
   * `isFinishing` should refer to those same members.
   */
  @Implementation
  protected boolean isFinishing() {
    return reflector(DirectActivityReflector.class, realActivity).isFinishing();
  }

  /**
   * Constructs a new Window (a {@link com.android.internal.policy.PhoneWindow}) if no window has
   * previously been set.
   *
   * @return the window associated with this Activity
   */
  @Implementation
  protected Window getWindow() {
    Window window = reflector(DirectActivityReflector.class, realActivity).getWindow();

    if (window == null) {
      try {
        window = ShadowWindow.create(realActivity);
        setWindow(window);
      } catch (Exception e) {
        throw new RuntimeException("Window creation failed!", e);
      }
    }

    return window;
  }

  /**
   * @return fake SplashScreen
   */
  @Implementation(minSdk = S)
  protected synchronized @ClassName("android.window.SplashScreen") Object getSplashScreen() {
    if (splashScreen == null) {
      splashScreen = new RoboSplashScreen();
    }
    return splashScreen;
  }

  public void setWindow(Window window) {
    reflector(ActivityReflector.class, realActivity).setWindow(window);
  }

  @Implementation
  protected void runOnUiThread(Runnable action) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.LEGACY) {
      RuntimeEnvironment.getMasterScheduler().post(action);
    } else {
      reflector(DirectActivityReflector.class, realActivity).runOnUiThread(action);
    }
  }

  @Implementation
  protected void setRequestedOrientation(int requestedOrientation) {
    if (getParent() != null) {
      getParent().setRequestedOrientation(requestedOrientation);
    } else {
      this.requestedOrientation = requestedOrientation;
      // As the window manager does, apply the request once the activity's current work is done.
      if (controller != null && ShadowLooper.looperMode() != LooperMode.Mode.LEGACY) {
        new Handler(Looper.getMainLooper()).post(this::applyRequestedOrientation);
      }
    }
  }

  /**
   * Applies the orientation the activity requests, as the window manager does: an activity that
   * fills its display rotates it, or is letterboxed if the display ignores orientation requests.
   * The request is ignored in multi-window mode, and on a large screen for an app that is
   * universally resizeable.
   */
  void applyRequestedOrientation() {
    if (controller == null
        || controller.get() != realActivity
        || realActivity.isFinishing()
        || realActivity.isDestroyed()
        || WindowConfigurations.isInMultiWindowMode(
            realActivity.getResources().getConfiguration())) {
      return;
    }
    int displayId = getDisplayId();
    if (WindowConfigurations.isIgnoringOrientationRequest(displayId)
        || WindowConfigurations.isUniversalResizeable(
            realActivity.getApplicationInfo(), displayId)) {
      // The activity's letterbox, if it has one, changes instead of the display.
      DisplayChanges.changeConfigurationIfNeeded(realActivity);
    } else if (rotateDisplayToRequestedOrientation(displayId) && controller.get() == realActivity) {
      DisplayChanges.changeConfigurationIfNeeded(realActivity);
    }
  }

  /** Rotates the display to the orientation the activity requests, and returns whether it did. */
  private boolean rotateDisplayToRequestedOrientation(int displayId) {
    int orientation = WindowConfigurations.getFixedOrientation(requestedOrientation);
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (orientation == Configuration.ORIENTATION_UNDEFINED
        || displayInfo == null
        || (orientation == Configuration.ORIENTATION_PORTRAIT)
            == (displayInfo.logicalHeight >= displayInfo.logicalWidth)
        || WindowConfigurations.isIgnoringOrientationRequest(displayId)
        || WindowConfigurations.isUniversalResizeable(
            RuntimeEnvironment.getApplication().getApplicationInfo(), displayId)) {
      return false;
    }
    String qualifiers = orientation == Configuration.ORIENTATION_PORTRAIT ? "+port" : "+land";
    if (displayId == Display.DEFAULT_DISPLAY) {
      RuntimeEnvironment.setQualifiers(qualifiers);
    } else {
      // The activities on the display receive the change as on a device.
      ShadowDisplayManager.changeDisplay(displayId, qualifiers);
    }
    return true;
  }

  @Implementation
  protected int getRequestedOrientation() {
    if (getParent() != null) {
      return getParent().getRequestedOrientation();
    } else {
      return this.requestedOrientation;
    }
  }

  @Implementation
  protected int getTaskId() {
    return 0;
  }

  @Implementation
  public void startIntentSenderForResult(
      IntentSender intentSender,
      int requestCode,
      @Nullable Intent fillInIntent,
      int flagsMask,
      int flagsValues,
      int extraFlags,
      Bundle options)
      throws IntentSender.SendIntentException {
    if (throwIntentSenderException) {
      throw new IntentSender.SendIntentException("PendingIntent was canceled");
    }
    lastIntentSenderRequest =
        new IntentSenderRequest(
            intentSender, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    lastIntentSenderRequest.send();
  }

  @Implementation
  protected void reportFullyDrawn() {
    hasReportedFullyDrawn = true;
  }

  @Implementation(minSdk = R)
  protected void setLocusContext(LocusId locusId, @Nullable Bundle extras) {
    this.lastLocusContextId = locusId;
    this.lastLocusContextExtras = extras;
    reflector(DirectActivityReflector.class, realActivity).setLocusContext(locusId, extras);
  }

  /**
   * @return the most recently populated locus context {@link LocusId}.
   */
  @Nullable
  public LocusId getLastLocusContextId() {
    return lastLocusContextId;
  }

  /**
   * @return the most recently populated locus context {@link Bundle} extras.
   */
  @Nullable
  public Bundle getLastLocusContextExtras() {
    return lastLocusContextExtras;
  }

  /**
   * @return whether {@code ReportFullyDrawn()} methods has been called.
   */
  public boolean getReportFullyDrawn() {
    return hasReportedFullyDrawn;
  }

  /**
   * @return the {@code contentView} set by one of the {@code setContentView()} methods
   */
  public View getContentView() {
    return ((ViewGroup) getWindow().findViewById(android.R.id.content)).getChildAt(0);
  }

  /**
   * @return the {@code resultCode} set by one of the {@code setResult()} methods
   */
  public int getResultCode() {
    return resultCode;
  }

  /**
   * @return the {@code Intent} set by {@link #setResult(int, android.content.Intent)}
   */
  public Intent getResultIntent() {
    return resultIntent;
  }

  /**
   * Consumes and returns the next {@code Intent} on the started activities for results stack.
   *
   * @return the next started {@code Intent} for an activity, wrapped in an {@link
   *     ShadowActivity.IntentForResult} object
   */
  @Override
  public IntentForResult getNextStartedActivityForResult() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation =
        Shadow.extract(activityThread.getInstrumentation());
    return shadowInstrumentation.getNextStartedActivityForResult();
  }

  /**
   * Returns the most recent {@code Intent} started by {@link
   * Activity#startActivityForResult(Intent, int)} without consuming it.
   *
   * @return the most recently started {@code Intent}, wrapped in an {@link
   *     ShadowActivity.IntentForResult} object
   */
  @Override
  public IntentForResult peekNextStartedActivityForResult() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation =
        Shadow.extract(activityThread.getInstrumentation());
    return shadowInstrumentation.peekNextStartedActivityForResult();
  }

  @Implementation
  protected Object getLastNonConfigurationInstance() {
    if (lastNonConfigurationInstance != null) {
      return lastNonConfigurationInstance;
    }
    return reflector(DirectActivityReflector.class, realActivity).getLastNonConfigurationInstance();
  }

  /**
   * @deprecated use {@link ActivityController#recreate()}.
   */
  @Deprecated
  public void setLastNonConfigurationInstance(Object lastNonConfigurationInstance) {
    this.lastNonConfigurationInstance = lastNonConfigurationInstance;
  }

  /**
   * @param view View to focus.
   */
  public void setCurrentFocus(View view) {
    currentFocus = view;
  }

  @Implementation
  protected View getCurrentFocus() {
    return currentFocus;
  }

  public int getPendingTransitionEnterAnimationResourceId() {
    return pendingTransitionEnterAnimResId;
  }

  public int getPendingTransitionExitAnimationResourceId() {
    return pendingTransitionExitAnimResId;
  }

  /**
   * Get the overridden {@link Activity} transition, set by {@link
   * Activity#overrideActivityTransition}.
   *
   * @param overrideType Use {@link Activity#OVERRIDE_TRANSITION_OPEN} to get the overridden
   *     activity transition animation details when starting/entering an activity. Use {@link
   *     Activity#OVERRIDE_TRANSITION_CLOSE} to get the overridden activity transition animation
   *     details when finishing/closing an activity.
   * @return overridden activity transition details after calling {@link
   *     Activity#overrideActivityTransition(int, int, int, int)} or null if was not overridden.
   * @see #clearOverrideActivityTransition(int)
   */
  @Nullable
  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public OverriddenActivityTransition getOverriddenActivityTransition(int overrideType) {
    return overriddenActivityTransitions.get(overrideType, null);
  }

  @Implementation
  protected boolean onCreateOptionsMenu(Menu menu) {
    optionsMenu = menu;
    return reflector(DirectActivityReflector.class, realActivity).onCreateOptionsMenu(menu);
  }

  /**
   * Return the options menu.
   *
   * @return Options menu.
   */
  public Menu getOptionsMenu() {
    return optionsMenu;
  }

  /**
   * Perform a click on a menu item.
   *
   * @param menuItemResId Menu item resource ID.
   * @return True if the click was handled, false otherwise.
   */
  public boolean clickMenuItem(int menuItemResId) {
    final RoboMenuItem item = new RoboMenuItem(menuItemResId);
    return realActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
  }

  @Deprecated
  public void callOnActivityResult(int requestCode, int resultCode, Intent resultData) {
    reflector(ActivityReflector.class, realActivity)
        .onActivityResult(requestCode, resultCode, resultData);
  }

  /** For internal use only. Not for public use. */
  public void internalCallDispatchActivityResult(
      String who, int requestCode, int resultCode, Intent data) {
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      reflector(ActivityReflector.class, realActivity)
          .dispatchActivityResult(who, requestCode, resultCode, data, "ACTIVITY_RESULT");
    } else {
      reflector(ActivityReflector.class, realActivity)
          .dispatchActivityResult(who, requestCode, resultCode, data);
    }
  }

  /** For internal use only. Not for public use. */
  public <T extends Activity> void attachController(ActivityController controller) {
    this.controller = controller;
  }

  @Nullable
  ActivityController<?> getController() {
    return controller;
  }

  /** Sets if startIntentSenderForRequestCode will throw an IntentSender.SendIntentException. */
  public void setThrowIntentSenderException(boolean throwIntentSenderException) {
    this.throwIntentSenderException = throwIntentSenderException;
  }

  /**
   * Container object to hold an Intent, together with the requestCode used in a call to {@code
   * Activity.startActivityForResult(Intent, int)}
   */
  public static class IntentForResult {
    public Intent intent;
    public int requestCode;
    public Bundle options;

    public IntentForResult(Intent intent, int requestCode) {
      this.intent = intent;
      this.requestCode = requestCode;
      this.options = null;
    }

    public IntentForResult(Intent intent, int requestCode, Bundle options) {
      this.intent = intent;
      this.requestCode = requestCode;
      this.options = options;
    }

    @Override
    public String toString() {
      return super.toString()
          + "{intent="
          + intent
          + ", requestCode="
          + requestCode
          + ", options="
          + options
          + '}';
    }
  }

  public void receiveResult(Intent requestIntent, int resultCode, Intent resultIntent) {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation =
        Shadow.extract(activityThread.getInstrumentation());
    TargetAndRequestCode targetAndRequestCode =
        shadowInstrumentation.getTargetAndRequestCodeForIntent(requestIntent);

    internalCallDispatchActivityResult(
        targetAndRequestCode.target, targetAndRequestCode.requestCode, resultCode, resultIntent);
  }

  @Implementation
  protected void showDialog(int id) {
    showDialog(id, null);
  }

  @Implementation
  protected boolean showDialog(int id, Bundle bundle) {
    this.lastShownDialogId = id;
    Dialog dialog = dialogForId.get(id);

    if (dialog == null) {
      dialog = reflector(ActivityReflector.class, realActivity).onCreateDialog(id);
      if (dialog == null) {
        return false;
      }
      if (bundle == null) {
        reflector(ActivityReflector.class, realActivity).onPrepareDialog(id, dialog);
      } else {
        reflector(ActivityReflector.class, realActivity).onPrepareDialog(id, dialog, bundle);
      }

      dialogForId.put(id, dialog);
    }

    dialog.show();
    return true;
  }

  @Implementation
  protected void dismissDialog(int id) {
    final Dialog dialog = dialogForId.get(id);
    if (dialog == null) {
      throw new IllegalArgumentException();
    }

    dialog.dismiss();
  }

  @Implementation
  protected void removeDialog(int id) {
    dialogForId.remove(id);
  }

  public void setIsTaskRoot(boolean isRoot) {
    mIsTaskRoot = isRoot;
  }

  @Implementation
  protected boolean isTaskRoot() {
    return mIsTaskRoot;
  }

  /**
   * @return the dialog resource id passed into {@code Activity.showDialog(int, Bundle)} or {@code
   *     Activity.showDialog(int)}
   */
  public Integer getLastShownDialogId() {
    return lastShownDialogId;
  }

  public boolean hasCancelledPendingTransitions() {
    return pendingTransitionEnterAnimResId == 0 && pendingTransitionExitAnimResId == 0;
  }

  @Implementation
  protected void overridePendingTransition(int enterAnim, int exitAnim) {
    pendingTransitionEnterAnimResId = enterAnim;
    pendingTransitionExitAnimResId = exitAnim;
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void overrideActivityTransition(
      int overrideType,
      @AnimRes int enterAnim,
      @AnimRes int exitAnim,
      @ColorInt int backgroundColor) {
    overriddenActivityTransitions.put(
        overrideType, new OverriddenActivityTransition(enterAnim, exitAnim, backgroundColor));

    reflector(DirectActivityReflector.class, realActivity)
        .overrideActivityTransition(overrideType, enterAnim, exitAnim, backgroundColor);
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void clearOverrideActivityTransition(int overrideType) {
    overriddenActivityTransitions.remove(overrideType);

    reflector(DirectActivityReflector.class, realActivity)
        .clearOverrideActivityTransition(overrideType);
  }

  public Dialog getDialogById(int dialogId) {
    return dialogForId.get(dialogId);
  }

  // TODO(hoisie): consider moving this to ActivityController#makeActivityEligibleForGc
  @Implementation
  protected void onDestroy() {
    reflector(DirectActivityReflector.class, realActivity).onDestroy();
    ShadowActivityThread activityThread = Shadow.extract(RuntimeEnvironment.getActivityThread());
    IBinder token = reflector(ActivityReflector.class, realActivity).getToken();
    activityThread.removeActivity(token);
  }

  @Implementation
  protected void recreate() {
    if (controller != null) {
      // Post the call to recreate to simulate ActivityThread behavior.
      new Handler(Looper.getMainLooper()).post(controller::recreate);
    } else {
      throw new IllegalStateException(
          "Cannot use an Activity that is not managed by an ActivityController");
    }
  }

  @Implementation
  protected void startManagingCursor(Cursor c) {
    managedCursors.add(c);
  }

  @Implementation
  protected void stopManagingCursor(Cursor c) {
    managedCursors.remove(c);
  }

  public List<Cursor> getManagedCursors() {
    return managedCursors;
  }

  @Implementation
  protected void setVolumeControlStream(int streamType) {
    this.streamType = streamType;
  }

  @Implementation
  protected int getVolumeControlStream() {
    return streamType;
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected void requestPermissions(String[] permissions, int requestCode) {
    lastRequestedPermission = new PermissionsRequest(permissions, requestCode);
    reflector(DirectActivityReflector.class, realActivity)
        .requestPermissions(permissions, requestCode);
  }

  @Implementation(minSdk = VANILLA_ICE_CREAM)
  protected void requestPermissions(String[] permissions, int requestCode, int deviceId) {
    lastRequestedPermission = new PermissionsRequest(permissions, requestCode, deviceId);
    reflector(DirectActivityReflector.class, realActivity)
        .requestPermissions(permissions, requestCode, deviceId);
  }

  /**
   * Starts a lock task.
   *
   * <p>The status of the lock task can be verified using {@link #isLockTask} method. Otherwise,
   * this implementation has no effect.
   */
  @Implementation
  protected void startLockTask() {
    Shadow.<ShadowActivityManager>extract(getActivityManager())
        .setLockTaskModeState(ActivityManager.LOCK_TASK_MODE_LOCKED);
  }

  /**
   * Stops a lock task.
   *
   * <p>The status of the lock task can be verified using {@link #isLockTask} method. Otherwise,
   * this implementation has no effect.
   */
  @Implementation
  protected void stopLockTask() {
    Shadow.<ShadowActivityManager>extract(getActivityManager())
        .setLockTaskModeState(ActivityManager.LOCK_TASK_MODE_NONE);
  }

  /**
   * Returns if the activity is in the lock task mode.
   *
   * @deprecated Use {@link ActivityManager#getLockTaskModeState} instead.
   */
  @Deprecated
  public boolean isLockTask() {
    return getActivityManager().isInLockTaskMode();
  }

  private ActivityManager getActivityManager() {
    return (ActivityManager) realActivity.getSystemService(Context.ACTIVITY_SERVICE);
  }

  /** Changes state of {@link #isInMultiWindowMode} method. */
  public void setInMultiWindowMode(boolean value) {
    inMultiWindowMode = value;
  }

  /**
   * Puts the activity in a freeform window with the given bounds on its display, as when the user
   * moves or resizes it, or makes its window fill the display if the bounds are null.
   *
   * <p>As on a device, the activity receives {@link Activity#onMultiWindowModeChanged} if it enters
   * or leaves multi-window mode, and the configuration change for its new window, or is recreated
   * if it doesn't handle the change. An activity that shares the application's context, as one
   * launched without {@link ActivityOptions#setLaunchBounds} does, can only enter a window by being
   * recreated. Requires P or later.
   *
   * @throws IllegalStateException if the window manager wouldn't put the activity in a freeform
   *     window: it isn't resizeable, and isn't on a large screen on S or later.
   */
  public void setWindowBounds(@Nullable Rect bounds) {
    if (bounds == null || RuntimeEnvironment.getApiLevel() < P) {
      changeWindow(null);
      return;
    }
    int displayId = getDisplayId();
    ActivityInfo activityInfo = lookUpActivityInfo();
    if (!WindowConfigurations.supportsMultiWindow(activityInfo, displayId)) {
      throw new IllegalStateException(
          "The activity isn't resizeable, so it can't be in a freeform window on this display");
    }
    changeWindow(
        WindowConfigurations.getFreeformOverrideConfiguration(displayId, bounds, activityInfo));
  }

  /**
   * Puts the activity in the system's split screen, as when the user picks it for split screen: in
   * the top or left half of its display, or in the other half if another activity is there. It
   * keeps its half, recomputed when the display changes, until {@link #setWindowBounds} changes its
   * window.
   *
   * <p>As on a device, an activity it starts with {@link Intent#FLAG_ACTIVITY_LAUNCH_ADJACENT}
   * launches in the other half, and {@link ShadowDisplayManager#setSplitScreenDividerPosition}
   * moves the divider between them. The activity receives the change as {@link #setWindowBounds}
   * describes. Requires P or later.
   *
   * @throws IllegalStateException if the window manager wouldn't put the activity in split screen:
   *     it isn't resizeable and isn't on a large screen on S or later, or its minimal size doesn't
   *     fit in its half of a display that respects it.
   */
  public void enterSplitScreen() {
    int displayId = getDisplayId();
    boolean topOrLeftIsTaken = false;
    for (Activity activity : LiveActivities.get()) {
      if (activity != realActivity
          && activity.getWindowManager().getDefaultDisplay().getDisplayId() == displayId
          && WindowConfigurations.isInTopOrLeftOfSplitScreen(
              activity.getResources().getConfiguration())) {
        topOrLeftIsTaken = true;
      }
    }
    if (RuntimeEnvironment.getApiLevel() >= P
        && !WindowConfigurations.supportsSplitScreen(
            lookUpActivityInfo(), displayId, !topOrLeftIsTaken)) {
      throw new IllegalStateException(
          "The activity can't be in split screen on this display: it isn't resizeable, or its"
              + " minimal size doesn't fit");
    }
    changeWindow(
        WindowConfigurations.getSplitScreenOverrideConfiguration(displayId, !topOrLeftIsTaken));
  }

  private ActivityInfo lookUpActivityInfo() {
    try {
      return realActivity
          .getPackageManager()
          .getActivityInfo(realActivity.getComponentName(), /* flags= */ 0);
    } catch (NameNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private void changeWindow(@Nullable Configuration windowOverrideConfig) {
    if (RuntimeEnvironment.getApiLevel() < P) {
      throw new IllegalStateException("Windows of their own require P or later");
    }
    if (controller == null) {
      throw new IllegalStateException("The activity was not started by an ActivityController");
    }
    int displayId = getDisplayId();
    Resources applicationResources = realActivity.getApplicationContext().getResources();
    Configuration configuration =
        new Configuration(
            WindowConfigurations.getDisplayConfiguration(
                displayId, applicationResources.getConfiguration()));
    DisplayMetrics displayMetrics =
        WindowConfigurations.getDisplayMetrics(displayId, applicationResources.getDisplayMetrics());
    if (windowOverrideConfig != null) {
      configuration.updateFrom(windowOverrideConfig);
      displayMetrics =
          WindowConfigurations.getWindowMetrics(
              displayMetrics, windowOverrideConfig.windowConfiguration.getBounds());
    } else {
      configuration.windowConfiguration.setWindowingMode(
          WindowConfiguration.WINDOWING_MODE_FULLSCREEN);
    }
    boolean wasInPictureInPictureMode =
        WindowConfigurations.isInPictureInPictureMode(
            realActivity.getResources().getConfiguration());
    controller.configurationChange(configuration, displayMetrics);
    Activity activity = (Activity) controller.get();
    boolean isInPictureInPictureMode = WindowConfigurations.isInPictureInPictureMode(configuration);
    Shadow.<ShadowActivity>extract(activity).isInPictureInPictureMode = isInPictureInPictureMode;
    // As on a device, an activity is paused in picture-in-picture mode, and resumed when it leaves.
    if (isInPictureInPictureMode && activity.isResumed()) {
      controller.topActivityResumed(false).pause();
    } else if (wasInPictureInPictureMode && !isInPictureInPictureMode && !activity.isResumed()) {
      controller.resume().topActivityResumed(true);
    }
  }

  private int getDisplayId() {
    return realActivity.getWindowManager().getDefaultDisplay().getDisplayId();
  }

  @Implementation(minSdk = N)
  protected boolean isInMultiWindowMode() {
    return inMultiWindowMode;
  }

  @Implementation(minSdk = N)
  protected boolean isInPictureInPictureMode() {
    return isInPictureInPictureMode;
  }

  @Implementation(minSdk = N)
  protected void enterPictureInPictureMode() {
    enterPictureInPictureMode((Rational) null);
  }

  @Implementation(minSdk = O)
  protected boolean enterPictureInPictureMode(PictureInPictureParams params) {
    enterPictureInPictureMode((Rational) ReflectionHelpers.getField(params, "mAspectRatio"));
    return true;
  }

  /**
   * Puts the activity in picture-in-picture mode. As the window manager does, once the activity's
   * current work is done it gets a pinned window, receives {@link
   * Activity#onPictureInPictureModeChanged} and is paused. {@link #setWindowBounds} with null
   * bounds makes its window fill the display again, as when the user expands it.
   */
  private void enterPictureInPictureMode(@Nullable Rational aspectRatio) {
    isInPictureInPictureMode = true;
    if (controller != null
        && RuntimeEnvironment.getApiLevel() >= P
        && ShadowLooper.looperMode() != LooperMode.Mode.LEGACY) {
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                if (isInPictureInPictureMode
                    && controller.get() == realActivity
                    && !realActivity.isFinishing()
                    && !realActivity.isDestroyed()) {
                  changeWindow(
                      WindowConfigurations.getPictureInPictureOverrideConfiguration(
                          getDisplayId(), aspectRatio));
                }
              });
    }
  }

  @Implementation
  protected boolean moveTaskToBack(boolean nonRoot) {
    // If task has already moved to back, return true.
    if (isTaskMovedToBack) {
      return true;
    }
    // If nonRoot is false then #moveTaskToBack only works when activity is the root of the task.
    if (!nonRoot && !mIsTaskRoot) {
      return false;
    }
    isTaskMovedToBack = true;
    isInPictureInPictureMode = false;
    return true;
  }

  /**
   * @return whether the task containing this activity is moved to the back of the activity stack.
   */
  public boolean isTaskMovedToBack() {
    return isTaskMovedToBack;
  }

  /**
   * Gets the last startIntentSenderForResult request made to this activity.
   *
   * @return The IntentSender request details.
   */
  public IntentSenderRequest getLastIntentSenderRequest() {
    return lastIntentSenderRequest;
  }

  /**
   * Gets the last permission request submitted to this activity.
   *
   * @return The permission request details.
   */
  public PermissionsRequest getLastRequestedPermission() {
    return lastRequestedPermission;
  }

  /**
   * Initializes the associated Activity with an {@link android.app.VoiceInteractor} instance.
   * Subsequent {@link android.app.Activity#getVoiceInteractor()} calls on the associated activity
   * will return a {@link android.app.VoiceInteractor} instance
   */
  public void initializeVoiceInteractor() {
    if (RuntimeEnvironment.getApiLevel() < N) {
      throw new IllegalStateException("initializeVoiceInteractor requires API " + N);
    }
    reflector(ActivityReflector.class, realActivity)
        .setVoiceInteractor(ReflectionHelpers.createDeepProxy(IVoiceInteractor.class));
  }

  /**
   * Calls Activity#onGetDirectActions with the given parameters. This method also simulates the
   * Parcel serialization/deserialization which occurs when assistant requests DirectAction.
   */
  public void callOnGetDirectActions(
      CancellationSignal cancellationSignal, Consumer<List<DirectAction>> callback) {
    if (RuntimeEnvironment.getApiLevel() < Q) {
      throw new IllegalStateException("callOnGetDirectActions requires API " + Q);
    }
    realActivity.onGetDirectActions(
        cancellationSignal,
        directActions -> {
          Parcel parcel = Parcel.obtain();
          parcel.writeParcelableList(directActions, 0);
          parcel.setDataPosition(0);
          List<DirectAction> output = new ArrayList<>();
          parcel.readParcelableList(output, DirectAction.class.getClassLoader());
          parcel.recycle();
          callback.accept(output);
        });
  }

  /**
   * Class to hold overridden activity transition details after calling {@link
   * Activity#overrideActivityTransition(int, int, int, int)}
   */
  public static class OverriddenActivityTransition {
    @AnimRes public final int enterAnim;
    @AnimRes public final int exitAnim;
    @ColorInt public final int backgroundColor;

    public OverriddenActivityTransition(int enterAnim, int exitAnim, int backgroundColor) {
      this.enterAnim = enterAnim;
      this.exitAnim = exitAnim;
      this.backgroundColor = backgroundColor;
    }
  }

  /** Class to hold a permissions request, including its request code. */
  public static class PermissionsRequest {
    public final int requestCode;
    public final String[] requestedPermissions;
    public final int deviceId;

    public PermissionsRequest(String[] requestedPermissions, int requestCode) {
      this(requestedPermissions, requestCode, Context.DEVICE_ID_DEFAULT);
    }

    public PermissionsRequest(String[] requestedPermissions, int requestCode, int deviceId) {
      this.requestedPermissions = requestedPermissions;
      this.requestCode = requestCode;
      this.deviceId = deviceId;
    }
  }

  /** Class to holds details of a startIntentSenderForResult request. */
  public static class IntentSenderRequest {
    public final IntentSender intentSender;
    public final int requestCode;
    @Nullable public final Intent fillInIntent;
    public final int flagsMask;
    public final int flagsValues;
    public final int extraFlags;
    public final Bundle options;

    public IntentSenderRequest(
        IntentSender intentSender,
        int requestCode,
        @Nullable Intent fillInIntent,
        int flagsMask,
        int flagsValues,
        int extraFlags,
        Bundle options) {
      this.intentSender = intentSender;
      this.requestCode = requestCode;
      this.fillInIntent = fillInIntent;
      this.flagsMask = flagsMask;
      this.flagsValues = flagsValues;
      this.extraFlags = extraFlags;
      this.options = options;
    }

    public void send() {
      if (intentSender instanceof RoboIntentSender) {
        try {
          Shadow.<ShadowPendingIntent>extract(((RoboIntentSender) intentSender).getPendingIntent())
              .send(
                  RuntimeEnvironment.getApplication(),
                  0,
                  null,
                  null,
                  null,
                  null,
                  null,
                  requestCode);
        } catch (PendingIntent.CanceledException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  @ForType(value = Activity.class, direct = true)
  interface DirectActivityReflector {

    void runOnUiThread(Runnable action);

    void onDestroy();

    boolean isFinishing();

    void overrideActivityTransition(
        int overrideType, int enterAnim, int exitAnim, int backgroundColor);

    void clearOverrideActivityTransition(int overrideType);

    Window getWindow();

    Object getLastNonConfigurationInstance();

    boolean onCreateOptionsMenu(Menu menu);

    void requestPermissions(String[] permissions, int requestCode);

    void requestPermissions(String[] permissions, int requestCode, int deviceId);

    void setLocusContext(LocusId locusId, @Nullable Bundle bundle);
  }
}
