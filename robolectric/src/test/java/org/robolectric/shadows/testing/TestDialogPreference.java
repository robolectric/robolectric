package org.robolectric.shadows.testing;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class TestDialogPreference extends DialogPreference {
  public TestDialogPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}
