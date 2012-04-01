package com.xtremelabs.robolectric.shadows;

import android.widget.Spinner;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Spinner.class)
public class ShadowSpinner extends ShadowAbsSpinner {

    private CharSequence prompt;

    @Implementation
    public void setPrompt(CharSequence prompt) {
        this.prompt = prompt;
    }

    @Implementation
    public CharSequence getPrompt() {
        return prompt;
    }
}
