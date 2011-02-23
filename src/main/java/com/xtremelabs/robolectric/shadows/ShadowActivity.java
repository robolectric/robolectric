package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.*;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.tester.android.view.TestWindow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import javassist.bytecode.Mnemonic;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;


@Implements(Activity.class)
public class ShadowActivity extends ShadowContextWrapper {
    @RealObject
    protected Activity realActivity;

    private Intent intent;
    private FrameLayout contentViewContainer;
    private View contentView;
    private int orientation;
    private int resultCode;
    private Intent resultIntent;
    private Activity parent;
    private boolean finishWasCalled;
    private TestWindow window;

    private List<IntentForResult> startedActivitiesForResults = new ArrayList<IntentForResult>();

    private Map<Intent, Integer> intentRequestCodeMap = new HashMap<Intent, Integer>();
    private int requestedOrientation = -1;
    private View currentFocus;
    private Integer lastShownDialogId = null;
    private int pendingTransitionEnterAnimResId = -1;
    private int pendingTransitionExitAnimResId = -1;
    private Object lastNonConfigurationInstance;
    private Map<Integer, Dialog> dialogForId = new HashMap<Integer, Dialog>();
    private CharSequence title;
    private boolean onKeyUpWasCalled;
    private ArrayList<Cursor> managedCusors = new ArrayList<Cursor>();

    private int mDefaultKeyMode = Activity.DEFAULT_KEYS_DISABLE;
    private SpannableStringBuilder mDefaultKeySsb = null;

    public void callOnCreate(Bundle bundle) {
        invokeReflectively("onCreate", Bundle.class, bundle);
    }

    public void callOnRestoreInstanceState(Bundle savedInstanceState) {
        invokeReflectively("onRestoreInstanceState", Bundle.class, savedInstanceState);
    }

    public void callOnPostCreate(android.os.Bundle savedInstanceState) {
        invokeReflectively("onPostCreate", Bundle.class, savedInstanceState);
    }

    public void callOnStart() {
        invokeReflectively("onStart");
    }

    public void callOnRestart() {
        invokeReflectively("onRestart");
    }

    public void callOnResume() {
        invokeReflectively("onResume");
    }

    public void callOnPostResume() {
        invokeReflectively("onPostResume");
    }

    public void callOnNewIntent(android.content.Intent intent) {
        invokeReflectively("onNewIntent", Intent.class, intent);
    }

    public void callOnSaveInstanceState(android.os.Bundle outState) {
        invokeReflectively("onSaveInstanceState", Bundle.class, outState);
    }

    public void callOnPause() {
        invokeReflectively("onPause");
    }

    public void callOnUserLeaveHint() {
        invokeReflectively("onUserLeaveHint");
    }

    public void callOnStop() {
        invokeReflectively("onStop");
    }

    public void callOnDestroy() {
        invokeReflectively("onDestroy");
    }

    private void invokeReflectively(String methodName, Class<?> argClass, Object arg) {
        try {
            Method method = Activity.class.getDeclaredMethod(methodName, argClass);
            method.setAccessible(true);
            method.invoke(realActivity, arg);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeReflectively(String methodName) {
        try {
            Method method = Activity.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(realActivity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Implementation
    public final Application getApplication() {
        return Robolectric.application;
    }

    @Override
    @Implementation
    public final Application getApplicationContext() {
        return getApplication();
    }

    @Implementation
    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    @Implementation
    public Intent getIntent() {
        return intent;
    }
    
    @Implementation
    public void setDefaultKeyMode(int keyMode) {
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

    @Implementation(i18nSafe = false)
    public void setTitle(CharSequence title) {
        this.title = title;
    }

    @Implementation
    public void setTitle(int titleId) {
        this.title = this.getResources().getString(titleId);
    }

    @Implementation
    public CharSequence getTitle() {
        return title;
    }

    /**
     * Sets the {@code contentView} for this {@code Activity} by invoking the
     * {@link android.view.LayoutInflater}
     *
     * @param layoutResID ID of the layout to inflate
     * @see #getContentView()
     */
    @Implementation
    public void setContentView(int layoutResID) {
        contentView = getLayoutInflater().inflate(layoutResID, new FrameLayout(realActivity));
        realActivity.onContentChanged();
    }

    @Implementation
    public void setContentView(View view) {
        contentView = view;
        realActivity.onContentChanged();
    }

    @Implementation
    public final void setResult(int resultCode) {
        this.resultCode = resultCode;
    }

    @Implementation
    public final void setResult(int resultCode, Intent data) {
        this.resultCode = resultCode;
        this.resultIntent = data;
    }

    @Implementation
    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(realActivity);
    }

    @Implementation
    public MenuInflater getMenuInflater() {
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
    public View findViewById(int id) {
        if (id == android.R.id.content) {
            return getContentViewContainer();
        }
        if (contentView != null) {
            return contentView.findViewById(id);
        } else {
            System.out.println("WARNING: you probably should have called setContentView() first");
            Thread.dumpStack();
            return null;
        }
    }

    private View getContentViewContainer() {
        if (contentViewContainer == null) {
            contentViewContainer = new FrameLayout(realActivity);
        }
        contentViewContainer.addView(contentView, 0);
        return contentViewContainer;
    }

    @Implementation
    public final Activity getParent() {
        return parent;
    }

    @Implementation
    public void onBackPressed() {
        finish();
    }

    @Implementation
    public void finish() {
        finishWasCalled = true;
    }

    public void resetIsFinishing() {
        finishWasCalled = false;
    }

    /**
     * @return whether {@link #finish()} was called
     */
    @Implementation
    public boolean isFinishing() {
        return finishWasCalled;
    }

    /**
     * Constructs a new Window (a {@link com.xtremelabs.robolectric.tester.android.view.TestWindow}) if no window has previously been
     * set.
     *
     * @return the window associated with this Activity
     */
    @Implementation
    public Window getWindow() {
        if (window == null) {
            window = new TestWindow(realActivity);
        }
        return window;
    }

    public void setWindow(TestWindow wind){
    	window = wind;
    }
    
    @Implementation
    public void runOnUiThread(Runnable action) {
        Robolectric.getUiThreadScheduler().post(action);
    }

    @Implementation
    public void onCreate(Bundle bundle) {

    }

    /**
     * Checks to see if {@code BroadcastListener}s are still registered.
     *
     * @throws RuntimeException if any listeners are still registered
     * @see #assertNoBroadcastListenersRegistered()
     */
    @Implementation
    public void onDestroy() {
        assertNoBroadcastListenersRegistered();
    }

    @Implementation
    public WindowManager getWindowManager() {
        return (WindowManager) Robolectric.application.getSystemService(Context.WINDOW_SERVICE);
    }

    @Implementation
    public void setRequestedOrientation(int requestedOrientation) {
        this.requestedOrientation = requestedOrientation;
    }

    @Implementation
    public int getRequestedOrientation() {
        return requestedOrientation;
    }

    @Implementation
    public SharedPreferences getPreferences(int mode) {
    	return ShadowPreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    /**
     * Checks the {@code ApplicationContext} to see if {@code BroadcastListener}s are still registered.
     *
     * @throws RuntimeException if any listeners are still registered
     * @see ShadowApplication#assertNoBroadcastListenersRegistered(android.content.Context, String)
     */
    public void assertNoBroadcastListenersRegistered() {
        shadowOf(getApplicationContext()).assertNoBroadcastListenersRegistered(realActivity, "Activity");
    }

    /**
     * Non-Android accessor.
     *
     * @return the {@code contentView} set by one of the {@code setContentView()} methods
     */
    public View getContentView() {
        return contentView;
    }

    /**
     * Non-Android accessor.
     *
     * @return the {@code resultCode} set by one of the {@code setResult()} methods
     */
    public int getResultCode() {
        return resultCode;
    }

    /**
     * Non-Android accessor.
     *
     * @return the {@code Intent} set by {@link #setResult(int, android.content.Intent)}
     */
    public Intent getResultIntent() {
        return resultIntent;
    }

    /**
     * Non-Android accessor consumes and returns the next {@code Intent} on the
     * started activities for results stack.
     *
     * @return the next started {@code Intent} for an activity, wrapped in
     *         an {@link ShadowActivity.IntentForResult} object
     */
    public IntentForResult getNextStartedActivityForResult() {
        if (startedActivitiesForResults.isEmpty()) {
            return null;
        } else {
            return startedActivitiesForResults.remove(0);
        }
    }

    /**
     * Non-Android accessor returns the most recent {@code Intent} started by
     * {@link #startActivityForResult(android.content.Intent, int)} without
     * consuming it.
     *
     * @return the most recently started {@code Intent}, wrapped in
     *         an {@link ShadowActivity.IntentForResult} object
     */
    public IntentForResult peekNextStartedActivityForResult() {
        if (startedActivitiesForResults.isEmpty()) {
            return null;
        } else {
            return startedActivitiesForResults.get(0);
        }
    }

    @Implementation
    public Object getLastNonConfigurationInstance() {
        return lastNonConfigurationInstance;
    }

    public void setLastNonConfigurationInstance(Object lastNonConfigurationInstance) {
        this.lastNonConfigurationInstance = lastNonConfigurationInstance;
    }

    /**
     * Non-Android accessor Sets the {@code View} for this {@code Activity}
     *
     * @param view
     */
    public void setCurrentFocus(View view) {
        currentFocus = view;
    }

    @Implementation
    public View getCurrentFocus() {
        return currentFocus;
    }

    @Implementation
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        onKeyUpWasCalled = true;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return false;
    }

    public boolean onKeyUpWasCalled() {
        return onKeyUpWasCalled;
    }

    public void resetKeyUpWasCalled() {
        onKeyUpWasCalled = false;
    }

    /**
     * Container object to hold an Intent, together with the requestCode used
     * in a call to {@code Activity#startActivityForResult(Intent, int)}
     */
    public class IntentForResult {
        public Intent intent;
        public int requestCode;

        public IntentForResult(Intent intent, int requestCode) {
            this.intent = intent;
            this.requestCode = requestCode;
        }
    }

    @Implementation
    public void startActivityForResult(Intent intent, int requestCode) {
        intentRequestCodeMap.put(intent, requestCode);
        startedActivitiesForResults.add(new IntentForResult(intent, requestCode));
        getApplicationContext().startActivity(intent);
    }

    public void receiveResult(Intent requestIntent, int resultCode, Intent resultIntent) {
        Integer requestCode = intentRequestCodeMap.get(requestIntent);
        if (requestCode == null) {
            throw new RuntimeException("No intent matches " + requestIntent + " among " + intentRequestCodeMap.keySet());
        }

        final ActivityInvoker invoker = new ActivityInvoker();
        invoker.call("onActivityResult", Integer.TYPE, Integer.TYPE, Intent.class)
            .with(requestCode, resultCode, resultIntent);
    }

    @Implementation
    public final void showDialog(int id) {
        showDialog(id, null);
    }

    @Implementation
    public final void dismissDialog(int id) {
        final Dialog dialog = dialogForId.get(id);
        if (dialog == null) {
            throw new IllegalArgumentException();
        }

        dialog.dismiss();
    }

    @Implementation
    public final void removeDialog(int id) {
        dialogForId.remove(id);
    }

    @Implementation
    public final boolean showDialog(int id, Bundle bundle) {
        Dialog dialog = null;
        this.lastShownDialogId = id;

        dialog = dialogForId.get(id);

        if (dialog == null) {
            final ActivityInvoker invoker = new ActivityInvoker();
            dialog = (Dialog) invoker.call("onCreateDialog", Integer.TYPE).with(id);

            if (bundle == null) {
                invoker.call("onPrepareDialog", Integer.TYPE, Dialog.class)
                    .with(id, dialog);
            } else {
                invoker.call("onPrepareDialog", Integer.TYPE, Dialog.class, Bundle.class)
                    .with(id, dialog, bundle);
            }

            dialogForId.put(id, dialog);
        }

        dialog.show();

        return true;
    }

    /**
     * Non-Android accessor
     *
     * @return the dialog resource id passed into
     *         {@code Activity#showDialog(int, Bundle)} or {@code Activity#showDialog(int)}
     */
    public Integer getLastShownDialogId() {
        return lastShownDialogId;
    }

    public boolean hasCancelledPendingTransitions() {
        return pendingTransitionEnterAnimResId == 0 && pendingTransitionExitAnimResId == 0;
    }

    @Implementation
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        pendingTransitionEnterAnimResId = enterAnim;
        pendingTransitionExitAnimResId = exitAnim;
    }

    public Dialog getDialogById(int dialogId) {
        return dialogForId.get(dialogId);
    }

    public void create() {
        final ActivityInvoker invoker = new ActivityInvoker();

        final Bundle noInstanceState = null;
        invoker.call("onCreate", Bundle.class).with(noInstanceState);
        invoker.call("onStart").withNothing();
        invoker.call("onPostCreate", Bundle.class).with(noInstanceState);
        invoker.call("onResume").withNothing();
    }

    @Implementation
    public void recreate() {
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

    public void pauseAndThenResume() {
        final ActivityInvoker invoker = new ActivityInvoker();

        invoker.call("onPause").withNothing();
        invoker.call("onStop").withNothing();
        invoker.call("onRestart").withNothing();
        invoker.call("onStart").withNothing();
        invoker.call("onResume").withNothing();
    }
    
    @Implementation
    public void startManagingCursor(Cursor c) {
    	managedCusors.add(c);
    }    

    @Implementation
    public void stopManagingCursor(Cursor c) {
    	managedCusors.remove(c);
    }
    
    public List<Cursor> getManagedCursors() {
    	return managedCusors;
    }
    
    private final class ActivityInvoker {
        private Method method;

        public ActivityInvoker call(final String methodName, final Class ...argumentClasses) {
            try {
                method = Activity.class.getDeclaredMethod(methodName, argumentClasses);
                method.setAccessible(true);
                return this;
            } catch(NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public Object withNothing() {
            return with();
        }

        public Object with(final Object ...parameters) {
            try {
                return method.invoke(realActivity, parameters);
            } catch(IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch(IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch(InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
