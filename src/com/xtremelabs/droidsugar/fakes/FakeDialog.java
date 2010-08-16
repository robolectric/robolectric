package com.xtremelabs.droidsugar.fakes;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import com.xtremelabs.droidsugar.util.Implements;

import java.lang.reflect.Method;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Dialog.class)
public class FakeDialog {
    public static FakeDialog latestDialog;

    private Dialog realDialog;
    private boolean isShowing;
    public Context context;
    public int layoutId;
    public int themeId;
    private View inflatedView;
    public boolean hasBeenDismissed;
    private DialogInterface.OnDismissListener onDismissListener;
    public CharSequence title;

    public static void reset() {
        latestDialog = null;
    }

    public FakeDialog(Dialog dialog) {
        realDialog = dialog;
    }

    public void __constructor__(Context context, int themeId) {
        this.context = context;
        this.themeId = themeId;

        latestDialog = this;
    }

    public void setContentView(int layoutResID) {
        layoutId = layoutResID;
    }

    public void setTitle(int stringResourceId) {
        this.title = context.getResources().getText(stringResourceId);
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public Context getContext() {
        return context;
    }

    public void show() {
        isShowing = true;
        try {
            Method onCreateMethod = Dialog.class.getDeclaredMethod("onCreate", Bundle.class);
            onCreateMethod.setAccessible(true);
            onCreateMethod.invoke(realDialog, (Bundle) null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void hide() {
        isShowing = false;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void dismiss() {
        isShowing = false;
        hasBeenDismissed = true;

        if (onDismissListener != null) {
            onDismissListener.onDismiss(realDialog);
        }
    }

    public View findViewById(int viewId) {
        if (layoutId > 0 && context != null) {
            if (inflatedView == null) {
                inflatedView = FakeContextWrapper.resourceLoader.viewLoader.inflateView(context, layoutId);
            }
            return inflatedView.findViewById(viewId);
        }
        return null;
    }

    public void clickOn(int viewId) {
        findViewById(viewId).performClick();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }
}
