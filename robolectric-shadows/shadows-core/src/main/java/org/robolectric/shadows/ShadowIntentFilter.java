package org.robolectric.shadows;

import android.content.IntentFilter;
import android.net.Uri;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Shadow for {@link android.content.IntentFilter}.
 *
 * @deprecated This shadow is no longer needed and will be removed in the next release of Robolectric.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(IntentFilter.class)
@Deprecated
public class ShadowIntentFilter {
}
