package org.robolectric.rap.ksp;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * A custom shadow of a non-public, nested Android framework class declared by its binary name via
 * {@code @Implements(className = ...)} — the pattern downstream projects use to shadow hidden
 * framework classes (for example {@code android.text.AndroidBidi} or, here, {@code
 * IntentFilter$AuthorityEntry}).
 *
 * <p>Exercises the KSP processor's handling of a nested {@code className} target: the shadow must
 * be registered under the canonical (dot) name so {@code ShadowMap.getShadowInfo} resolves it via
 * {@code Class.getCanonicalName()} at runtime, while the {@code @Resetter} guard uses the binary
 * ({@code $}) name to match {@code Class.getName()}.
 */
@Implements(className = "android.content.IntentFilter$AuthorityEntry", isInAndroidSdk = false)
public class ShadowIntentFilterAuthorityEntryJava {

  @Resetter
  public static void reset() {}
}
