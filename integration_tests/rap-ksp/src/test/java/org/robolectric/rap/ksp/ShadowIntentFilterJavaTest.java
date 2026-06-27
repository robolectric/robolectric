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
 * Tests that the KSP processor handles a Java shadow class alongside Kotlin shadows in a mixed
 * Kotlin/Java module.
 */
@RunWith(RobolectricTestRunner.class)
public final class ShadowIntentFilterJavaTest {

  @Test
  public void javaShadow_isExtracted() {
    IntentFilter filter = new IntentFilter();
    ShadowIntentFilterJava shadow = Shadow.extract(filter);
    assertThat(shadow).isNotNull();
  }

  @Test
  public void javaShadow_isCorrectType() {
    IntentFilter filter = new IntentFilter();
    Object shadow = Shadow.extract(filter);
    assertThat(shadow).isInstanceOf(ShadowIntentFilterJava.class);
  }

  @Test
  public void javaShadow_registeredInShadowsProvider() {
    // The KSP-generated Shadows provider in this package, not org.robolectric.Shadows.
    Shadows provider = new Shadows();
    boolean found =
        StreamSupport.stream(provider.getShadows().spliterator(), false)
            .map(Map.Entry::getKey)
            .anyMatch("android.content.IntentFilter"::equals);
    assertThat(found).isTrue();
  }
}
