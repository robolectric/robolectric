package com.xtremelabs.robolectric.fakes;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

import java.util.ArrayList;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Toast.class)
public class ShadowToast {
    private static ArrayList<Toast> shownToasts = new ArrayList<Toast>();

    private String text;
    private int gravity;
    private View view;

    @RealObject Toast toast;

    @Implementation
    public static Toast makeText(Context context, int resId, int duration) {
        return makeText(context, context.getResources().getString(resId), duration);
    }

    @Implementation
    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast toast = new Toast(null);
        proxyFor(toast).text = text.toString();
        return toast;
    }

    @Implementation
    public void show() {
        shownToasts.add(toast);
    }

    @Implementation
    public void setView(View view) {
        this.view = view;
    }

    @Implementation
    public View getView() {
        return view;
    }

    @Implementation
    public void setGravity(int gravity, int xOffset, int yOffset) {
        this.gravity = gravity;
    }

    @Implementation
    public int getGravity() {
        return gravity;
    }

    private static ShadowToast proxyFor(Toast toast) {
        return (ShadowToast) ProxyDelegatingHandler.getInstance().proxyFor(toast);
    }

    public static void reset() {
        shownToasts.clear();
    }

    public static int shownToastCount() {
        return shownToasts.size();
    }

    public static boolean showedCustomToast(CharSequence message, int layoutResourceIdToCheckForMessage) {
        for (Toast toast : shownToasts) {
            String text = ((TextView) toast.getView().findViewById(layoutResourceIdToCheckForMessage)).getText().toString();
            if (text.equals(message.toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean showedToast(CharSequence message) {
        for (Toast toast : shownToasts) {
            String text = proxyFor(toast).text;
            if (text != null && text.equals(message.toString())) {
                return true;
            }
        }
        return false;
    }

    public static String getTextOfLatestToast() {
        return proxyFor(shownToasts.get(0)).text;
    }

    public static Toast getLatestToast() {
        return shownToasts.get(shownToasts.size() - 1);
    }
}
