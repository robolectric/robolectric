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
 * Verifies that a KSP-processed shadow of a nested framework class declared via {@code className}
 * (here {@code IntentFilter$AuthorityEntry}) is registered under its canonical name and actually
 * applies at runtime. This only works because the generated {@code SHADOWS} entry is keyed by
 * {@code Class.getCanonicalName()} (dots) rather than the {@code $} binary name.
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
    Shadows provider = new Shadows();
    boolean found =
        StreamSupport.stream(provider.getShadows().spliterator(), false)
            .map(Map.Entry::getKey)
            .anyMatch("android.content.IntentFilter.AuthorityEntry"::equals);
    assertThat(found).isTrue();
  }
}
