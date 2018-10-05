package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.Window;
import com.android.internal.app.IVoiceInteractor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(Activity.class)
public class ShadowActivity extends ShadowContextThemeWrapper {

  @RealObject
  protected Activity realActivity;

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
  private boolean isLockTask;

  public void setApplication(Application application) {
    ReflectionHelpers.setField(realActivity, "mApplication", application);
  }

  public void callAttach(Intent intent) {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    Application application = RuntimeEnvironment.application;
    Context baseContext = RuntimeEnvironment.application.getBaseContext();
    Class<?> nonConfigurationInstancesClass = getNonConfigurationClass();

    ActivityInfo activityInfo;
    try {
      activityInfo = application.getPackageManager().getActivityInfo(new ComponentName(application.getPackageName(), realActivity.getClass().getName()), PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      throw new RuntimeException();
    }

    CharSequence activityTitle = activityInfo.loadLabel(baseContext.getPackageManager());

    Instrumentation instrumentation =
        ((ActivityThread) RuntimeEnvironment.getActivityThread()).getInstrumentation();
    if (apiLevel <= Build.VERSION_CODES.KITKAT) {
      ReflectionHelpers.callInstanceMethod(
          Activity.class,
          realActivity,
          "attach",
          ReflectionHelpers.ClassParameter.from(Context.class, baseContext),
          ReflectionHelpers.ClassParameter.from(
              ActivityThread.class, RuntimeEnvironment.getActivityThread()),
          ReflectionHelpers.ClassParameter.from(Instrumentation.class, instrumentation),
          ReflectionHelpers.ClassParameter.from(IBinder.class, null),
          ReflectionHelpers.ClassParameter.from(int.class, 0),
          ReflectionHelpers.ClassParameter.from(Application.class, application),
          ReflectionHelpers.ClassParameter.from(Intent.class, intent),
          ReflectionHelpers.ClassParameter.from(ActivityInfo.class, activityInfo),
          ReflectionHelpers.ClassParameter.from(CharSequence.class, activityTitle),
          ReflectionHelpers.ClassParameter.from(Activity.class, null),
          ReflectionHelpers.ClassParameter.from(String.class, "id"),
          ReflectionHelpers.ClassParameter.from(nonConfigurationInstancesClass, null),
          ReflectionHelpers.ClassParameter.from(
              Configuration.class, application.getResources().getConfiguration()));
    } else if (apiLevel <= Build.VERSION_CODES.LOLLIPOP) {
      ReflectionHelpers.callInstanceMethod(
          Activity.class,
          realActivity,
          "attach",
          ReflectionHelpers.ClassParameter.from(Context.class, baseContext),
          ReflectionHelpers.ClassParameter.from(
              ActivityThread.class, RuntimeEnvironment.getActivityThread()),
          ReflectionHelpers.ClassParameter.from(Instrumentation.class, instrumentation),
          ReflectionHelpers.ClassParameter.from(IBinder.class, null),
          ReflectionHelpers.ClassParameter.from(int.class, 0),
          ReflectionHelpers.ClassParameter.from(Application.class, application),
          ReflectionHelpers.ClassParameter.from(Intent.class, intent),
          ReflectionHelpers.ClassParameter.from(ActivityInfo.class, activityInfo),
          ReflectionHelpers.ClassParameter.from(CharSequence.class, activityTitle),
          ReflectionHelpers.ClassParameter.from(Activity.class, null),
          ReflectionHelpers.ClassParameter.from(String.class, "id"),
          ReflectionHelpers.ClassParameter.from(nonConfigurationInstancesClass, null),
          ReflectionHelpers.ClassParameter.from(
              Configuration.class, application.getResources().getConfiguration()),
          ReflectionHelpers.ClassParameter.from(IVoiceInteractor.class, null)); // ADDED
    } else if (apiLevel <= Build.VERSION_CODES.M) {
      ReflectionHelpers.callInstanceMethod(
          Activity.class,
          realActivity,
          "attach",
          ReflectionHelpers.ClassParameter.from(Context.class, baseContext),
          ReflectionHelpers.ClassParameter.from(
              ActivityThread.class, RuntimeEnvironment.getActivityThread()),
          ReflectionHelpers.ClassParameter.from(Instrumentation.class, instrumentation),
          ReflectionHelpers.ClassParameter.from(IBinder.class, null),
          ReflectionHelpers.ClassParameter.from(int.class, 0),
          ReflectionHelpers.ClassParameter.from(Application.class, application),
          ReflectionHelpers.ClassParameter.from(Intent.class, intent),
          ReflectionHelpers.ClassParameter.from(ActivityInfo.class, activityInfo),
          ReflectionHelpers.ClassParameter.from(CharSequence.class, activityTitle),
          ReflectionHelpers.ClassParameter.from(Activity.class, null),
          ReflectionHelpers.ClassParameter.from(String.class, "id"),
          ReflectionHelpers.ClassParameter.from(nonConfigurationInstancesClass, null),
          ReflectionHelpers.ClassParameter.from(
              Configuration.class, application.getResources().getConfiguration()),
          ReflectionHelpers.ClassParameter.from(String.class, "referrer"),
          ReflectionHelpers.ClassParameter.from(
              IVoiceInteractor.class, null)); // SAME AS LOLLIPOP ---------------------------
    } else if (apiLevel <= Build.VERSION_CODES.N_MR1) {
      ReflectionHelpers.callInstanceMethod(
          Activity.class,
          realActivity,
          "attach",
          ReflectionHelpers.ClassParameter.from(Context.class, baseContext),
          ReflectionHelpers.ClassParameter.from(
              ActivityThread.class, RuntimeEnvironment.getActivityThread()),
          ReflectionHelpers.ClassParameter.from(Instrumentation.class, instrumentation),
          ReflectionHelpers.ClassParameter.from(IBinder.class, null),
          ReflectionHelpers.ClassParameter.from(int.class, 0),
          ReflectionHelpers.ClassParameter.from(Application.class, application),
          ReflectionHelpers.ClassParameter.from(Intent.class, intent),
          ReflectionHelpers.ClassParameter.from(ActivityInfo.class, activityInfo),
          ReflectionHelpers.ClassParameter.from(CharSequence.class, activityTitle),
          ReflectionHelpers.ClassParameter.from(Activity.class, null),
          ReflectionHelpers.ClassParameter.from(String.class, "id"),
          ReflectionHelpers.ClassParameter.from(nonConfigurationInstancesClass, null),
          ReflectionHelpers.ClassParameter.from(
              Configuration.class, application.getResources().getConfiguration()),
          ReflectionHelpers.ClassParameter.from(String.class, "referrer"),
          ReflectionHelpers.ClassParameter.from(IVoiceInteractor.class, null),
          ReflectionHelpers.ClassParameter.from(Window.class, null) // ADDED
          );
    } else if (apiLevel >= Build.VERSION_CODES.O) {
      ReflectionHelpers.callInstanceMethod(
          Activity.class,
          realActivity,
          "attach",
          ReflectionHelpers.ClassParameter.from(Context.class, baseContext),
          ReflectionHelpers.ClassParameter.from(
              ActivityThread.class, RuntimeEnvironment.getActivityThread()),
          ReflectionHelpers.ClassParameter.from(Instrumentation.class, instrumentation),
          ReflectionHelpers.ClassParameter.from(IBinder.class, null),
          ReflectionHelpers.ClassParameter.from(int.class, 0),
          ReflectionHelpers.ClassParameter.from(Application.class, application),
          ReflectionHelpers.ClassParameter.from(Intent.class, intent),
          ReflectionHelpers.ClassParameter.from(ActivityInfo.class, activityInfo),
          ReflectionHelpers.ClassParameter.from(CharSequence.class, activityTitle),
          ReflectionHelpers.ClassParameter.from(Activity.class, null),
          ReflectionHelpers.ClassParameter.from(String.class, "id"),
          ReflectionHelpers.ClassParameter.from(nonConfigurationInstancesClass, null),
          ReflectionHelpers.ClassParameter.from(
              Configuration.class, application.getResources().getConfiguration()),
          ReflectionHelpers.ClassParameter.from(String.class, "referrer"),
          ReflectionHelpers.ClassParameter.from(IVoiceInteractor.class, null),
          ReflectionHelpers.ClassParameter.from(Window.class, null),
          ReflectionHelpers.ClassParameter.from(
              ViewRootImpl.ActivityConfigCallback.class, null) // ADDED
          );
    } else {
      throw new RuntimeException("Could not find AndroidRuntimeAdapter for API level: " + apiLevel);
    }

    int theme = activityInfo.getThemeResource();
    if (theme != 0) {
      realActivity.setTheme(theme);
    }
  }

  private Class<?> getNonConfigurationClass() {
    try {
      return getClass().getClassLoader().loadClass("android.app.Activity$NonConfigurationInstances");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public void setCallingActivity(ComponentName activityName) {
    callingActivity = activityName;
  }

  @Implementation
  protected ComponentName getCallingActivity() {
    return callingActivity;
  }

  @Implementation
  protected void setDefaultKeyMode(int keyMode) {
    mDefaultKeyMode = keyMode;

    // Some modes use a SpannableStringBuilder to track & dispatch input events
    // This list must remain in sync with the switch in onKeyDown()
    switch (mDefaultKeyMode) {
      case Activity.DEFAULT_KEYS_DISABLE:
      case Activity.DEFAULT_KEYS_SHORTCUT:
        mDefaultKeySsb = null;      // not used in these modes
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
  @HiddenApi @Implementation
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
    ReflectionHelpers.setField(Activity.class, realActivity, "mFinished", true);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void finishAndRemoveTask() {
    // Sets the mFinished field in the real activity so NoDisplay activities can be tested.
    ReflectionHelpers.setField(Activity.class, realActivity, "mFinished", true);
  }

  @Implementation(minSdk = JELLY_BEAN)
  protected void finishAffinity() {
    // Sets the mFinished field in the real activity so NoDisplay activities can be tested.
    ReflectionHelpers.setField(Activity.class, realActivity, "mFinished", true);
  }

  public void resetIsFinishing() {
    ReflectionHelpers.setField(Activity.class, realActivity, "mFinished", false);
  }

  /**
   * Constructs a new Window (a {@link com.android.internal.policy.impl.PhoneWindow}) if no window
   * has previously been set.
   *
   * @return the window associated with this Activity
   */
  @Implementation
  protected Window getWindow() {
    Window window = directlyOn(realActivity, Activity.class).getWindow();

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

  public void setWindow(Window window) {
    ReflectionHelpers.setField(realActivity, "mWindow", window);
  }

  @Implementation
  protected void runOnUiThread(Runnable action) {
    ShadowApplication.getInstance().getForegroundThreadScheduler().post(action);
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

  /**
   * @return the {@code contentView} set by one of the {@code setContentView()} methods
   */
  public View getContentView() {
    return ((ViewGroup) getWindow().findViewById(R.id.content)).getChildAt(0);
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
   * Consumes and returns the next {@code Intent} on the
   * started activities for results stack.
   *
   * @return the next started {@code Intent} for an activity, wrapped in
   *         an {@link ShadowActivity.IntentForResult} object
   */
  public IntentForResult getNextStartedActivityForResult() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation = Shadow.extract(activityThread.getInstrumentation());
    return shadowInstrumentation.getNextStartedActivityForResult();
  }

  /**
   * Returns the most recent {@code Intent} started by
   * {@link Activity#startActivityForResult(Intent, int)} without consuming it.
   *
   * @return the most recently started {@code Intent}, wrapped in
   *         an {@link ShadowActivity.IntentForResult} object
   */
  public IntentForResult peekNextStartedActivityForResult() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation = Shadow.extract(activityThread.getInstrumentation());
    return shadowInstrumentation.peekNextStartedActivityForResult();
  }

  @Implementation
  protected Object getLastNonConfigurationInstance() {
    return lastNonConfigurationInstance;
  }

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
    return directlyOn(realActivity, Activity.class).onCreateOptionsMenu(menu);
  }

  /**
   * Return the options menu.
   *
   * @return  Options menu.
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

  /**
   * Container object to hold an Intent, together with the requestCode used
   * in a call to {@code Activity.startActivityForResult(Intent, int)}
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
  }

  public void receiveResult(Intent requestIntent, int resultCode, Intent resultIntent) {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation = Shadow.extract(activityThread.getInstrumentation());
    int requestCode = shadowInstrumentation.getRequestCodeForIntent(requestIntent);

    final ActivityInvoker invoker = new ActivityInvoker();
    invoker.call("onActivityResult", Integer.TYPE, Integer.TYPE, Intent.class)
        .with(requestCode, resultCode, resultIntent);
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
      final ActivityInvoker invoker = new ActivityInvoker();
      dialog = (Dialog) invoker.call("onCreateDialog", Integer.TYPE).with(id);
      if (dialog == null) {
        return false;
      }
      if (bundle == null) {
        invoker.call("onPrepareDialog", Integer.TYPE, Dialog.class).with(id, dialog);
      } else {
        invoker.call("onPrepareDialog", Integer.TYPE, Dialog.class, Bundle.class).with(id, dialog, bundle);
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
   * @return the dialog resource id passed into
   *         {@code Activity.showDialog(int, Bundle)} or {@code Activity.showDialog(int)}
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

  @Implementation
  protected void recreate() {
    Bundle outState = new Bundle();
    final ActivityInvoker invoker = new ActivityInvoker();

    invoker.call("onSaveInstanceState", Bundle.class).with(outState);
    invoker.call("onPause").withNothing();
    invoker.call("onStop").withNothing();

    Object nonConfigInstance = invoker.call("onRetainNonConfigurationInstance").withNothing();
    setLastNonConfigurationInstance(nonConfigInstance);

    invoker.call("onDestroy").withNothing();
    invoker.call("onCreate", Bundle.class).with(outState);
    invoker.call("onStart").withNothing();
    invoker.call("onRestoreInstanceState", Bundle.class).with(outState);
    invoker.call("onResume").withNothing();
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
  protected final void requestPermissions(String[] permissions, int requestCode) {}

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

  private final class ActivityInvoker {
    private Method method;

    public ActivityInvoker call(final String methodName, final Class... argumentClasses) {
      try {
        method = Activity.class.getDeclaredMethod(methodName, argumentClasses);
        method.setAccessible(true);
        return this;
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    public Object withNothing() {
      return with();
    }

    public Object with(final Object... parameters) {
      try {
        return method.invoke(realActivity, parameters);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
