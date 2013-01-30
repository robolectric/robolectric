package com.xtremelabs.robolectric.shadows;

import android.view.ViewParent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the {@code android.widget.RadioButton} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(RadioButton.class)
public class ShadowRadioButton extends ShadowCompoundButton {

    @Implementation
    @Override public void setChecked(boolean checked) {
        super.setChecked(checked);
        ViewParent viewParent = getParent();

         /* This simulates the listener a parent RadioGroup would have, listening to the
            checked state it's child RadioButtons. Feel free to implement properly.
         */
        if (viewParent instanceof RadioGroup) {
            ((RadioGroup) viewParent).check(getId());
        }
    }
}
