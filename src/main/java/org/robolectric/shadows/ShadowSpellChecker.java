package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;

@Implements(value = Robolectric.Anything.class, className = "android.widget.SpellChecker", callThroughByDefault = false)
public class ShadowSpellChecker {
}
