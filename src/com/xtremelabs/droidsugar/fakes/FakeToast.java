package com.xtremelabs.droidsugar.fakes;

import android.content.Context;
import android.widget.Toast;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Toast.class)
public class FakeToast {
    private static Map<CharSequence, Toast> toasts = new HashMap<CharSequence, Toast>();

    private boolean wasShown = false;

    public static Toast makeText(Context context, int resId, int duration) {
        return makeText(context, context.getResources().getString(resId), duration);
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast toast = new Toast(null);
        toasts.put(text, toast);
        return toast;
    }

    public static boolean showedToast(CharSequence message) {
        return toasts.containsKey(message) && proxyFor(toasts.get(message)).wasShown;
    }

    private static FakeToast proxyFor(Toast toast) {
        return (FakeToast) ProxyDelegatingHandler.getInstance().proxyFor(toast);
    }

    public static void reset() {
        toasts.clear();
    }

    public void show() {
        wasShown = true;
    }
}
