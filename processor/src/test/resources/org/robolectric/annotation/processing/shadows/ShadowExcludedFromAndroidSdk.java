package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import com.example.objects.Dummy;

@Implements(value = Dummy.class, isInAndroidSdk = false)
public class ShadowExcludedFromAndroidSdk {
}
