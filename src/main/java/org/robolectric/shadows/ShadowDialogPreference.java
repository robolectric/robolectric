package org.robolectric.shadows;


import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import android.preference.DialogPreference;

@Implements(DialogPreference.class)
public class ShadowDialogPreference extends ShadowPreference {
    @Implementation
    public CharSequence getDialogMessage() {
        return attrs.getAttributeValue("android", "dialogMessage");
    }
}
