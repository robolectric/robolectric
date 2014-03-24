package org.robolectric.shadows;

import android.preference.EditTextPreference;
import org.robolectric.annotation.Implements;

@Implements(EditTextPreference.class)
public class ShadowEditTextPreference extends ShadowDialogPreference {

}
