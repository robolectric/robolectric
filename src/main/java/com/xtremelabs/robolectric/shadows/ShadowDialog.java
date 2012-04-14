package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.tester.android.view.TestWindow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Dialog.class)
public class ShadowDialog {

    @RealObject private Dialog realDialog;

    private boolean isShowing;
    Context context;
    private int layoutId;
    private int themeId;
    private View inflatedView;
    private boolean hasBeenDismissed;
    private DialogInterface.OnDismissListener onDismissListener;
    protected CharSequence title;
    private DialogInterface.OnCancelListener onCancelListener;
    private Window window;
    private Activity ownerActivity;
    private boolean isCancelable = true;
    private boolean hasShownBefore;
    private static final ArrayList<Dialog> shownDialogs = new ArrayList<Dialog>();
    private boolean isCancelableOnTouchOutside;

    public static void reset() {
        setLatestDialog(null);
        shownDialogs.clear();
    }

    public static Dialog getLatestDialog() {
        ShadowDialog dialog = Robolectric.getShadowApplication().getLatestDialog();
        return dialog == null ? null : dialog.realDialog;
    }

    public static void setLatestDialog(ShadowDialog latestDialog) {
        Robolectric.getShadowApplication().setLatestDialog(latestDialog);
    }

    public void __constructor__(Context context) {
        __constructor__(context, -1);
    }

    public void __constructor__(Context context, int themeId) {
        this.context = context;
        this.themeId = themeId;
    }

    @Implementation
    public void setContentView(int layoutResID) {
        layoutId = layoutResID;
    }

    @Implementation
    public void setContentView(View view) {
        inflatedView = view;
    }

    @Implementation
    public void setTitle(int stringResourceId) {
        this.title = context.getResources().getText(stringResourceId);
    }

    @Implementation(i18nSafe = false)
    public void setTitle(CharSequence title) {
        this.title = title;
    }

    @Implementation
    public void setOwnerActivity(Activity activity) {
        this.ownerActivity = activity;
    }

    @Implementation
    public Activity getOwnerActivity() {
        return this.ownerActivity;
    }

    @Implementation
    public Context getContext() {
        return context;
    }

    @Implementation
    public void onBackPressed() {
        cancel();
    }

    @Implementation
    public void show() {
        setLatestDialog(this);
        shownDialogs.add(realDialog);
        isShowing = true;
        try {
            if (!hasShownBefore) {
                Method onCreateMethod = Dialog.class.getDeclaredMethod("onCreate", Bundle.class);
                onCreateMethod.setAccessible(true);
                onCreateMethod.invoke(realDialog, (Bundle) null);
            }

            Method onStartMethod = Dialog.class.getDeclaredMethod("onStart");
            onStartMethod.setAccessible(true);
            onStartMethod.invoke(realDialog);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        hasShownBefore = true;
    }

    @Implementation
    public void hide() {
        isShowing = false;
    }

    @Implementation
    public boolean isShowing() {
        return isShowing;
    }

    @Implementation
    public void dismiss() {
        isShowing = false;
        hasBeenDismissed = true;

        if (onDismissListener != null) {
            onDismissListener.onDismiss(realDialog);
        }
    }

    @Implementation
    public View findViewById(int viewId) {
        if (inflatedView != null) {
            return inflatedView.findViewById(viewId);
        }
        if (layoutId > 0 && context != null) {
            inflatedView = ShadowLayoutInflater.from(context).inflate(layoutId, null);
            return inflatedView.findViewById(viewId);
        }
        return null;
    }

    public void clickOn(int viewId) {
        findViewById(viewId).performClick();
    }

    @Implementation
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Implementation
    public void setCancelable(boolean flag) {
        isCancelable = flag;
    }

    @Implementation
    public void setCanceledOnTouchOutside(boolean flag) {
        isCancelableOnTouchOutside = flag;
    }

    public boolean isCancelable() {
        return isCancelable;
    }

    public boolean isCancelableOnTouchOutside() {
        return isCancelableOnTouchOutside;
    }

    @Implementation
    public void cancel() {
        if (onCancelListener != null) {
            onCancelListener.onCancel(realDialog);
        }
        realDialog.dismiss();
    }

    @Implementation
    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        this.onCancelListener = listener;
    }

    public DialogInterface.OnCancelListener getOnCancelListener() {
        return onCancelListener;
    }

    @Implementation
    public Window getWindow() {
        if (window == null) {
            window = new TestWindow(realDialog.getContext());
        }
        return window;
    }


    @Implementation
    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(realDialog.getContext());
    }

    public int getLayoutId() {
        return layoutId;
    }

    public int getThemeId() {
        return themeId;
    }

    public boolean hasBeenDismissed() {
        return hasBeenDismissed;
    }

    public CharSequence getTitle() {
        return title;
    }

    public void clickOnText(int textId) {
        if (inflatedView == null) {
            inflatedView = ShadowLayoutInflater.from(context).inflate(layoutId, null);
        }
        String text = getContext().getResources().getString(textId);
        if (!clickOnText(inflatedView, text)) {
            throw new IllegalArgumentException("Text not found: " + text);
        }
    }

    private boolean clickOnText(View view, String text) {
        if (text.equals(shadowOf(view).innerText())) {
            view.performClick();
            return true;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (clickOnText(child, text)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Dialog> getShownDialogs() {
        return shownDialogs;
    }
}
