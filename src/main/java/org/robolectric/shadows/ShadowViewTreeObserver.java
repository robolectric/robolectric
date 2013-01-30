package com.xtremelabs.robolectric.shadows;

import android.view.ViewTreeObserver;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ViewTreeObserver.class)
public class ShadowViewTreeObserver {

    private ArrayList<ViewTreeObserver.OnGlobalLayoutListener> globalLayoutListeners = new ArrayList<ViewTreeObserver.OnGlobalLayoutListener>();

    @Implementation
    public void addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener listener) {
        this.globalLayoutListeners.add(listener);
    }

    @Implementation
    public void removeGlobalOnLayoutListener(ViewTreeObserver.OnGlobalLayoutListener listener) {
        this.globalLayoutListeners.remove(listener);
    }

    public void fireOnGlobalLayoutListeners() {
        for (ViewTreeObserver.OnGlobalLayoutListener listener : new ArrayList<ViewTreeObserver.OnGlobalLayoutListener>(globalLayoutListeners)) {
            listener.onGlobalLayout();
        }
    }

    public List<ViewTreeObserver.OnGlobalLayoutListener> getOnGlobalLayoutListeners() {
        return globalLayoutListeners;
    }
}
