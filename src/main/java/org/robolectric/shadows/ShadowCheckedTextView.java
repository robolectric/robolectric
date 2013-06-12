package org.robolectric.shadows;

import android.widget.CheckedTextView;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = CheckedTextView.class)
public class ShadowCheckedTextView extends ShadowTextView {
}
