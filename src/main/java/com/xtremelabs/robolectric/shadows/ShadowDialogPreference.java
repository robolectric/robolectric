package com.xtremelabs.robolectric.shadows;


import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import android.preference.DialogPreference;

@Implements(DialogPreference.class)
public class ShadowDialogPreference extends ShadowPreference {
    @Implementation
    public CharSequence getDialogMessage() {
        return attrs.getAttributeValue("android", "dialogMessage");
    }
}
