package org.robolectric.shadows;

import android.content.Context;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = ContextThemeWrapper.class, inheritImplementationMethods = true)
public class ShadowContextThemeWrapper extends ShadowContextWrapper {
    public void __constructor__(Context baseContext, int themesres) {
        super.__constructor__(baseContext);
    }

    @Implementation
    @Override public Resources.Theme getTheme() {
        return super.getTheme();
    }
}