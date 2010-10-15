package com.xtremelabs.robolectric.fakes;

import android.content.Context;
import android.widget.Toast;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Toast.class)
public class FakeToast {
    private static Map<CharSequence, Toast> toasts = new HashMap<CharSequence, Toast>();

    private boolean wasShown = false;

    @Implementation
    public static Toast makeText(Context context, int resId, int duration) {
        return makeText(context, context.getResources().getString(resId), duration);
    }

    @Implementation
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

    @Implementation
    public void show() {
        wasShown = true;
    }

    public static Set<String> showedToasts() {
        HashSet<String> strings = new HashSet<String>();
        for (CharSequence toastString : toasts.keySet()) {
            if (proxyFor(toasts.get(toastString.toString())).wasShown) {
                strings.add(toastString.toString());
            }
        }
        return strings;
    }
}
