package org.robolectric.shadows;


import android.preference.DialogPreference;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(DialogPreference.class)
public class ShadowDialogPreference extends ShadowPreference {
    @Implementation
    public CharSequence getDialogMessage() {
        return attrs.getAttributeValue("android", "dialogMessage");
    }
}
