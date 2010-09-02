package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Activity.class)
public class FakeActivity extends FakeContextWrapper {
    private Intent intent;
    public View contentView;

    public boolean finishWasCalled;
    public Intent startActivityIntent;
    public int resultCode;
    public Intent resultIntent;
    public Activity parent;
    public static Application application;
    private Activity realActivity;
    private FakeActivity.TestWindow window;

    public FakeActivity(Activity realActivity) {
        this.realActivity = realActivity;
    }

    public final Application getApplication() {
        return application;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setContentView(int layoutResID) {
        contentView = resourceLoader.viewLoader.inflateView(realActivity, layoutResID);
    }

    public void setContentView(View view) {
        contentView = view;
    }

    public final void setResult(int resultCode) {
        this.resultCode = resultCode;
    }

    public final void setResult(int resultCode, Intent data) {
        this.resultCode = resultCode;
        resultIntent = data;
    }

    public LayoutInflater getLayoutInflater() {
        return new FakeLayoutInflater(resourceLoader.viewLoader);
    }

    public View findViewById(int id) {
        if (contentView != null) {
            return contentView.findViewById(id);
        } else {
            throw new RuntimeException("you should have called setContentView() first");
        }
    }

    public final Activity getParent() {
        return parent;
    }

    public void startActivity(Intent intent) {
        startActivityIntent = intent;
    }

    public void finish() {
        finishWasCalled = true;
    }

    public Window getWindow() {
        if(window == null) {
            window = new TestWindow(realActivity);
        }
        return window;
    }

    public class TestWindow extends Window {
        public int flags;

        private TestWindow(Context context) {
            super(context);
        }

        @Override public void setFlags(int flags, int mask) {
            this.flags = (flags&~mask) | (flags&mask);
        }

        @Override public void addFlags(int flags) {
            setFlags(flags, flags);
        }

        @Override public boolean isFloating() {
            return false;
        }

        @Override public void setContentView(int layoutResID) {
        }

        @Override public void setContentView(View view) {
        }

        @Override public void setContentView(View view, ViewGroup.LayoutParams params) {
        }

        @Override public void addContentView(View view, ViewGroup.LayoutParams params) {
        }

        @Override public View getCurrentFocus() {
            return null;
        }

        @Override public LayoutInflater getLayoutInflater() {
            return null;
        }

        @Override public void setTitle(CharSequence title) {
        }

        @Override public void setTitleColor(int textColor) {
        }

        @Override public void openPanel(int featureId, KeyEvent event) {
        }

        @Override public void closePanel(int featureId) {
        }

        @Override public void togglePanel(int featureId, KeyEvent event) {
        }

        @Override public boolean performPanelShortcut(int featureId, int keyCode, KeyEvent event, int flags) {
            return false;
        }

        @Override public boolean performPanelIdentifierAction(int featureId, int id, int flags) {
            return false;
        }

        @Override public void closeAllPanels() {
        }

        @Override public boolean performContextMenuIdentifierAction(int id, int flags) {
            return false;
        }

        @Override public void onConfigurationChanged(Configuration newConfig) {
        }

        @Override public void setBackgroundDrawable(Drawable drawable) {
        }

        @Override public void setFeatureDrawableResource(int featureId, int resId) {
        }

        @Override public void setFeatureDrawableUri(int featureId, Uri uri) {
        }

        @Override public void setFeatureDrawable(int featureId, Drawable drawable) {
        }

        @Override public void setFeatureDrawableAlpha(int featureId, int alpha) {
        }

        @Override public void setFeatureInt(int featureId, int value) {
        }

        @Override public void takeKeyEvents(boolean get) {
        }

        @Override public boolean superDispatchKeyEvent(KeyEvent event) {
            return false;
        }

        @Override public boolean superDispatchTouchEvent(MotionEvent event) {
            return false;
        }

        @Override public boolean superDispatchTrackballEvent(MotionEvent event) {
            return false;
        }

        @Override public View getDecorView() {
            return null;
        }

        @Override public View peekDecorView() {
            return null;
        }

        @Override public Bundle saveHierarchyState() {
            return null;
        }

        @Override public void restoreHierarchyState(Bundle savedInstanceState) {
        }

        @Override protected void onActive() {
        }

        @Override public void setChildDrawable(int featureId, Drawable drawable) {
        }

        @Override public void setChildInt(int featureId, int value) {
        }

        @Override public boolean isShortcutKey(int keyCode, KeyEvent event) {
            return false;
        }

        @Override public void setVolumeControlStream(int streamType) {
        }

        @Override public int getVolumeControlStream() {
            return 0;
        }
    }
}
