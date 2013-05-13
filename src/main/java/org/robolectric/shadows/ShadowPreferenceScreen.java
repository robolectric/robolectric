package org.robolectric.shadows;

import android.app.Dialog;
import android.preference.PreferenceScreen;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(PreferenceScreen.class)
public class ShadowPreferenceScreen extends ShadowPreferenceGroup {

  private Dialog dialog;

  @Implementation
  public Dialog getDialog() {
    return dialog;
  }

  public void setDialog(Dialog dialog) {
    this.dialog = dialog;
  }
}
