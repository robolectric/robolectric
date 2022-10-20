package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Binder;
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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.RequiresApi;
import com.android.internal.app.IVoiceInteractor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.fakes.RoboIntentSender;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.fakes.RoboSplashScreen;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowContextImpl._ContextImpl_;
import org.robolectric.shadows.ShadowInstrumentation.TargetAndRequestCode;
import org.robolectric.shadows.ShadowLoadedApk._LoadedApk_;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

@SuppressWarnings("NewApi")
@Implements(value = Activity.class, looseSignatures = true)
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
  private Object lastNonConfigurationInstance;
  private Map<Integer, Dialog> dialogForId = new HashMap<>();
  private ArrayList<Cursor> managedCursors = new ArrayList<>();
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

  public void setApplication(Application application) {
    reflector(_Activity_.class, realActivity).setApplication(application);
  }

  public void callAttach(Intent intent) {
    callAttach(intent, /*activityOptions=*/ null, /*lastNonConfigurationInstances=*/ null);
  }

  public void callAttach(Intent intent, @Nullable Bundle activityOptions) {
    callAttach(
        intent, /*activityOptions=*/ activityOptions, /*lastNonConfigurationInstances=*/ null);
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
    if ((Boolean.getBoolean("robolectric.createActivityContexts")
            || (displayId != Display.DEFAULT_DISPLAY && displayId != Display.INVALID_DISPLAY))
        && RuntimeEnvironment.getApiLevel() >= O) {
      LoadedApk loadedApk =
          activityThread.getPackageInfo(
              ShadowActivityThread.getApplicationInfo(), null, Context.CONTEXT_INCLUDE_CODE);
      _LoadedApk_ loadedApkReflector = reflector(_LoadedApk_.class, loadedApk);
      loadedApkReflector.setResources(application.getResources());
      loadedApkReflector.setApplication(application);
      activityContext =
          reflector(_ContextImpl_.class)
              .createActivityContext(
                  activityThread, loadedApk, activityInfo, token, displayId, overrideConfig);
      reflector(_ContextImpl_.class, activityContext).setOuterContext(realActivity);
      // This is not what the SDK does but for backwards compatibility with previous versions of
      // robolectric, which did not use a separate activity context, move the theme from the
      // application context (previously tests would configure the theme on the application context
      // with the expectation that it modify the activity).
      if (baseContext.getThemeResId() != 0) {
        activityContext.setTheme(baseContext.getThemeResId());
      }
    } else {
      activityContext = baseContext;
    }

    reflector(_Activity_.class, realActivity)
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
  protected final void setResult(int resultCode) {
    this.resultCode = resultCode;
  }

  @Implementation
  protected final void setResult(int resultCode, Intent data) {
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
  protected final Activity getParent() {
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
    reflector(_Activity_.class, realActivity).setFinished(true);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void finishAndRemoveTask() {
    // Sets the mFinished field in the real activity so NoDisplay activities can be tested.
    reflector(_Activity_.class, realActivity).setFinished(true);
  }

  @Implementation
  protected void finishAffinity() {
    // Sets the mFinished field in the real activity so NoDisplay activities can be tested.
    reflector(_Activity_.class, realActivity).setFinished(true);
  }

  public void resetIsFinishing() {
    reflector(_Activity_.class, realActivity).setFinished(false);
  }

  /**
   * Returns whether {@link #finish()} was called.
   *
   * <p>Note: this method seems redundant, but removing it will cause problems for Mockito spies of
   * Activities that call {@link Activity#finish()} followed by {@link Activity#isFinishing()}. This
   * is because `finish` modifies the members of {@link ShadowActivity#realActivity}, so
   * `isFinishing` should refer to those same members.
   */
  @Implementation(minSdk = JELLY_BEAN)
  protected boolean isFinishing() {
    return reflector(DirectActivityReflector.class, realActivity).isFinishing();
  }

  /**
   * Constructs a new Window (a {@link com.android.internal.policy.impl.PhoneWindow}) if no window
   * has previously been set.
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
  protected synchronized Object getSplashScreen() {
    if (splashScreen == null) {
      splashScreen = new RoboSplashScreen();
    }
    return splashScreen;
  }

  public void setWindow(Window window) {
    reflector(_Activity_.class, realActivity).setWindow(window);
  }

  @Implementation
  protected void runOnUiThread(Runnable action) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      reflector(DirectActivityReflector.class, realActivity).runOnUiThread(action);
    } else {
      ShadowApplication.getInstance().getForegroundThreadScheduler().post(action);
    }
  }

  @Implementation
  protected void setRequestedOrientation(int requestedOrientation) {
    if (getParent() != null) {
      getParent().setRequestedOrientation(requestedOrientation);
    } else {
      this.requestedOrientation = requestedOrientation;
    }
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

  @Implementation(minSdk = KITKAT)
  protected void reportFullyDrawn() {
    hasReportedFullyDrawn = true;
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
    reflector(_Activity_.class, realActivity).onActivityResult(requestCode, resultCode, resultData);
  }

  /** For internal use only. Not for public use. */
  public void internalCallDispatchActivityResult(
      String who, int requestCode, int resultCode, Intent data) {
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      reflector(_Activity_.class, realActivity)
          .dispatchActivityResult(who, requestCode, resultCode, data, "ACTIVITY_RESULT");
    } else {
      reflector(_Activity_.class, realActivity)
          .dispatchActivityResult(who, requestCode, resultCode, data);
    }
  }

  /** For internal use only. Not for public use. */
  public <T extends Activity> void attachController(ActivityController controller) {
    this.controller = controller;
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
  protected final void showDialog(int id) {
    showDialog(id, null);
  }

  @Implementation
  protected final void dismissDialog(int id) {
    final Dialog dialog = dialogForId.get(id);
    if (dialog == null) {
      throw new IllegalArgumentException();
    }

    dialog.dismiss();
  }

  @Implementation
  protected final void removeDialog(int id) {
    dialogForId.remove(id);
  }

  @Implementation
  protected final boolean showDialog(int id, Bundle bundle) {
    this.lastShownDialogId = id;
    Dialog dialog = dialogForId.get(id);

    if (dialog == null) {
      dialog = reflector(_Activity_.class, realActivity).onCreateDialog(id);
      if (dialog == null) {
        return false;
      }
      if (bundle == null) {
        reflector(_Activity_.class, realActivity).onPrepareDialog(id, dialog);
      } else {
        reflector(_Activity_.class, realActivity).onPrepareDialog(id, dialog, bundle);
      }

      dialogForId.put(id, dialog);
    }

    dialog.show();
    return true;
  }

  public void setIsTaskRoot(boolean isRoot) {
    mIsTaskRoot = isRoot;
  }

  @Implementation
  protected final boolean isTaskRoot() {
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

  public Dialog getDialogById(int dialogId) {
    return dialogForId.get(dialogId);
  }

  // TODO(hoisie): consider moving this to ActivityController#makeActivityEligibleForGc
  @Implementation
  protected void onDestroy() {
    reflector(DirectActivityReflector.class, realActivity).onDestroy();
    ShadowActivityThread activityThread = Shadow.extract(RuntimeEnvironment.getActivityThread());
    IBinder token = reflector(_Activity_.class, realActivity).getToken();
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
  protected final void setVolumeControlStream(int streamType) {
    this.streamType = streamType;
  }

  @Implementation
  protected final int getVolumeControlStream() {
    return streamType;
  }

  @Implementation(minSdk = M)
  protected final void requestPermissions(String[] permissions, int requestCode) {
    lastRequestedPermission = new PermissionsRequest(permissions, requestCode);
    reflector(DirectActivityReflector.class, realActivity)
        .requestPermissions(permissions, requestCode);
  }

  /**
   * Starts a lock task.
   *
   * <p>The status of the lock task can be verified using {@link #isLockTask} method. Otherwise this
   * implementation has no effect.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected void startLockTask() {
    Shadow.<ShadowActivityManager>extract(getActivityManager())
        .setLockTaskModeState(ActivityManager.LOCK_TASK_MODE_LOCKED);
  }

  /**
   * Stops a lock task.
   *
   * <p>The status of the lock task can be verified using {@link #isLockTask} method. Otherwise this
   * implementation has no effect.
   */
  @Implementation(minSdk = LOLLIPOP)
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
    isInPictureInPictureMode = true;
  }

  @Implementation(minSdk = O)
  protected boolean enterPictureInPictureMode(PictureInPictureParams params) {
    isInPictureInPictureMode = true;
    return true;
  }

  @Implementation
  protected boolean moveTaskToBack(boolean nonRoot) {
    isInPictureInPictureMode = false;
    return true;
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
    reflector(_Activity_.class, realActivity)
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
          callback.accept(output);
        });
  }

  /** Class to hold a permissions request, including its request code. */
  public static class PermissionsRequest {
    public final int requestCode;
    public final String[] requestedPermissions;

    public PermissionsRequest(String[] requestedPermissions, int requestCode) {
      this.requestedPermissions = requestedPermissions;
      this.requestCode = requestCode;
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

  private ShadowPackageManager shadowOf(PackageManager packageManager) {
    return Shadow.extract(packageManager);
  }

  @ForType(value = Activity.class, direct = true)
  interface DirectActivityReflector {

    void runOnUiThread(Runnable action);

    void onDestroy();

    boolean isFinishing();

    Window getWindow();

    Object getLastNonConfigurationInstance();

    boolean onCreateOptionsMenu(Menu menu);

    void requestPermissions(String[] permissions, int requestCode);
  }
}
