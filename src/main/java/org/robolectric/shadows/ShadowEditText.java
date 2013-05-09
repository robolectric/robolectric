package org.robolectric.shadows;

import android.widget.EditText;
import org.robolectric.annotation.Implements;

/**
 * A shadow for EditText that provides support for listeners
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = EditText.class)
public class ShadowEditText extends ShadowTextView {
}