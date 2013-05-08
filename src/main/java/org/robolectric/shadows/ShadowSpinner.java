package org.robolectric.shadows;

import android.widget.Spinner;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Spinner.class)
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
