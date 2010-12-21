package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.res.ResourceLoader;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Calls through to the {@code resourceLoader} to actually load resources.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Context.class)
public class ShadowContext {
    @RealObject private Context realContext;

    @Implementation
    public String getString(int resId) {
        return realContext.getResources().getString(resId);
    }

    @Implementation
    public CharSequence getText(int resId) {
        return realContext.getResources().getText(resId);
    }

    @Implementation
    public String getString(int resId, Object... formatArgs) {
        return realContext.getResources().getString(resId, formatArgs);
    }

    /**
     * Non-Android accessor.
     *
     * @return the {@code ResourceLoader} associated with this {@code Context}
     */
    public ResourceLoader getResourceLoader() {
        return shadowOf((Application) realContext.getApplicationContext()).getResourceLoader();
    }
}
