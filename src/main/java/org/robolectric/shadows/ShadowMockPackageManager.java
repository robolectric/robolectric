package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;

@Implements(value = Robolectric.Anything.class, className = "android.test.mock.MockPackageManager")
public class ShadowMockPackageManager {
}
