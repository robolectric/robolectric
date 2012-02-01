package com.xtremelabs.robolectric.shadows;

import android.widget.RadioGroup;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(RadioGroup.class)
public class ShadowRadioGroup extends ShadowLinearLayout {

    private int checkedRadioButtonId = -1;

    @Implementation
    public int getCheckedRadioButtonId() {
        return checkedRadioButtonId;
    }

    @Implementation
    public void check(int id) {
        checkedRadioButtonId = id;
    }
}
