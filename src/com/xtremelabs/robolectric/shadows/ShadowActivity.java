package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.view.TestWindow;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;


@SuppressWarnings({"UnusedDeclaration"})
@Implements(Activity.class)
public class ShadowActivity extends ShadowContextWrapper {
    @RealObject private Activity realActivity;

    private Intent intent;
    View contentView;

    private int resultCode;
    private Intent resultIntent;
    private Activity parent;
    private boolean finishWasCalled;
    private TestWindow window;

    @Implementation
    public final Application getApplication() {
        return Robolectric.application;
    }

    @Override @Implementation
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

    /**
     * Sets the {@code contentView} for this {@code Activity} by invoking the
     * {@link android.view.LayoutInflater}
     *
     * @param layoutResID ID of the layout to inflate
     * @see #getContentView()
     */
    @Implementation
    public void setContentView(int layoutResID) {
        contentView = getLayoutInflater().inflate(layoutResID, null);
    }

    @Implementation
    public void setContentView(View view) {
        contentView = view;
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
        if (contentView != null) {
            return contentView.findViewById(id);
        } else {
            throw new RuntimeException("you should have called setContentView() first");
        }
    }

    @Implementation
    public final Activity getParent() {
        return parent;
    }

    @Implementation
    public void finish() {
        finishWasCalled = true;
    }

    /**
     * @return whether {@link #finish()} was called
     */
    @Implementation
    public boolean isFinishing() {
        return finishWasCalled;
    }

    /**
     * Constructs a new Window (a {@link com.xtremelabs.robolectric.view.TestWindow}) if no window has previously been
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

    /**
     * Checks the {@code ApplicationContext} to see if {@code BroadcastListener}s are still registered.
     *
     * @throws RuntimeException if any listeners are still registered
     * @see ShadowApplication#assertNoBroadcastListenersRegistered(android.content.Context, String)
     */
    public void assertNoBroadcastListenersRegistered() {
        ((ShadowApplication) shadowOf(getApplicationContext())).assertNoBroadcastListenersRegistered(realActivity, "Activity");
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
}
