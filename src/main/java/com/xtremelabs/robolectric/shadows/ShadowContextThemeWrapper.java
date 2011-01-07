package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.ContextThemeWrapper;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextThemeWrapper.class)
public class ShadowContextThemeWrapper extends ShadowContextWrapper {
    public void __constructor__(Context baseContext, int themesres) {
        super.__constructor__(baseContext);
    }
}