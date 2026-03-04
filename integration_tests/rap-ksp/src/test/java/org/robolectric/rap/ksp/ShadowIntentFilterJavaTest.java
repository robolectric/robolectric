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
 * Integration tests verifying that a Java shadow class is correctly processed by the KSP processor
 * alongside Kotlin shadow classes in the same module (mixed Kotlin/Java sources).
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
    Shadows provider = new Shadows();
    boolean found =
        StreamSupport.stream(provider.getShadows().spliterator(), false)
            .map(Map.Entry::getKey)
            .anyMatch("android.content.IntentFilter"::equals);
    assertThat(found).isTrue();
  }
}
