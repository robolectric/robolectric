package org.robolectric.shadows;

import android.widget.CheckedTextView;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = CheckedTextView.class)
public class ShadowCheckedTextView extends ShadowTextView {
}
