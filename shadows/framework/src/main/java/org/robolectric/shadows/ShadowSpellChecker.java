package org.robolectric.shadows;

import android.widget.SpellChecker;
import org.robolectric.annotation.Implements;

@Implements(value = SpellChecker.class, callThroughByDefault = false, isInAndroidSdk = false)
public class ShadowSpellChecker {
}
