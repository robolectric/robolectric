package org.robolectric.shadows;


import android.preference.DialogPreference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.ResourceLoader;

@Implements(DialogPreference.class)
public class ShadowDialogPreference extends ShadowPreference {
  @Implementation
  public CharSequence getDialogMessage() {
    return attrs.getAttributeValue(ResourceLoader.ANDROID_NS, "dialogMessage");
  }
}
