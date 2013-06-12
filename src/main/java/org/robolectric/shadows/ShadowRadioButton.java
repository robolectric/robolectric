package org.robolectric.shadows;

import android.view.ViewParent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadows the {@code android.widget.RadioButton} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = RadioButton.class)
public class ShadowRadioButton extends ShadowCompoundButton {

  @Implementation
  @Override public void setChecked(boolean checked) {
    super.setChecked(checked);
    ViewParent viewParent = realView.getParent();

     /* This simulates the listener a parent RadioGroup would have, listening to the
      checked state it's child RadioButtons. Feel free to implement properly.
     */
    if (viewParent instanceof RadioGroup) {
      ((RadioGroup) viewParent).check(realView.getId());
    }
  }
}
