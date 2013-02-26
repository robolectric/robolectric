package org.robolectric.shadows;

import android.preference.PreferenceCategory;

import org.robolectric.internal.Implements;

@Implements(PreferenceCategory.class)
public class ShadowPreferenceCategory extends ShadowPreferenceGroup {

}
