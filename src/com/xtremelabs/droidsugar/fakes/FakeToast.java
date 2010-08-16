package com.xtremelabs.droidsugar.fakes;

import android.content.Context;
import android.widget.Toast;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Toast.class)
public class FakeToast {
    public static List<CharSequence> toastMessages = new ArrayList<CharSequence>();
    public static boolean wasShown;

    public static Toast makeText(Context context, int resId, int duration) {
        toastMessages.add(context.getResources().getString(resId));
        return new Toast(null);
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        toastMessages.add(text);
        return new Toast(null);
    }

    public static boolean madeToast(CharSequence message) {
        return toastMessages.contains(message);
    }

    public void show() {
        wasShown = true;
    }

    public static void reset() {
        toastMessages.clear();
        wasShown = false;
    }
}
