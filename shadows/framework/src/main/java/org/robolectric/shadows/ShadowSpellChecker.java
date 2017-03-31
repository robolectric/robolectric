package org.robolectric.shadows;

import android.widget.SpellChecker;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.widget.SpellChecker}.
 */
@Implements(value = SpellChecker.class, callThroughByDefault = false, isInAndroidSdk = false)
public class ShadowSpellChecker {
}
