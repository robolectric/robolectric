package com.xtremelabs.robolectric.shadows;

import android.preference.PreferenceCategory;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(PreferenceCategory.class)
public class ShadowPreferenceCategory extends ShadowPreferenceGroup {

}
