package org.robolectric.integrationtests.junitplatform;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.robolectric.junit.jupiter.RobolectricExtension;

/** Third-party Jupiter extensions work alongside {@link RobolectricExtension}. */
@ExtendWith({RobolectricExtension.class, MockitoExtension.class})
class MockitoExtensionTest {
  @Mock private SharedPreferences preferences;

  @Test
  void injectsMocksOfAndroidClasses() {
    when(preferences.getString("name", "world")).thenReturn("Robolectric");

    assertThat(new Greeter(preferences).greeting()).isEqualTo("Hello, Robolectric");
    verify(preferences).getString("name", "world");
  }

  /** The class under test. */
  private static final class Greeter {
    private final SharedPreferences preferences;

    Greeter(SharedPreferences preferences) {
      this.preferences = preferences;
    }

    String greeting() {
      return "Hello, " + preferences.getString("name", "world");
    }
  }
}
