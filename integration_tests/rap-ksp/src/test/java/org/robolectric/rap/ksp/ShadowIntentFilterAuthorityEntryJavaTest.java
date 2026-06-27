package org.robolectric.rap.ksp;

import static com.google.common.truth.Truth.assertThat;

import android.content.IntentFilter;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadow.api.Shadow;

/**
 * Tests that a KSP-processed shadow of a nested framework class declared via {@code className} is
 * registered under its canonical (dot) name and applies at runtime.
 */
@RunWith(RobolectricTestRunner.class)
public final class ShadowIntentFilterAuthorityEntryJavaTest {

  @Test
  public void nestedClassNameShadow_appliesAtRuntime() {
    IntentFilter.AuthorityEntry entry = new IntentFilter.AuthorityEntry("example.com", "80");
    Object shadow = Shadow.extract(entry);
    assertThat(shadow).isInstanceOf(ShadowIntentFilterAuthorityEntryJava.class);
  }

  @Test
  public void nestedClassNameShadow_registeredUnderCanonicalName() {
    // The KSP-generated Shadows provider in this package, not org.robolectric.Shadows.
    Shadows provider = new Shadows();
    boolean found =
        StreamSupport.stream(provider.getShadows().spliterator(), false)
            .map(Map.Entry::getKey)
            .anyMatch("android.content.IntentFilter.AuthorityEntry"::equals);
    assertThat(found).isTrue();
  }
}
