package org.robolectric.rap.ksp;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Test fixture shadow used only by the rap-ksp integration tests. Shadows a non-public nested
 * framework class by binary name via {@code Implements(className = ...)}, verifying that the KSP
 * processor registers it under the canonical (dot) name while reset() guards use the binary ($)
 * name.
 */
@Implements(className = "android.content.IntentFilter$AuthorityEntry", isInAndroidSdk = false)
public class ShadowIntentFilterAuthorityEntryJava {

  @Resetter
  public static void reset() {}
}
