package org.robolectric.shadows;

import android.widget.TabWidget;
import org.robolectric.internal.HiddenApi;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(value = TabWidget.class, callThroughByDefault = true)
public class ShadowTabWidget extends ShadowLinearLayout {
    @HiddenApi @Implementation
    public void initTabWidget() {
    }
}
