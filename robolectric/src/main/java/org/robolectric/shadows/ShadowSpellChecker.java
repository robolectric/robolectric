package org.robolectric.shadows;

import org.robolectric.annotation.Implements;

@Implements(className = "android.widget.SpellChecker", callThroughByDefault = false)
public class ShadowSpellChecker {
}
