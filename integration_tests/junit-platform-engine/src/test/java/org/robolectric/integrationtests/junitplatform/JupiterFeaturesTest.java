package org.robolectric.integrationtests.junitplatform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.robolectric.junit.jupiter.RobolectricExtension;
import org.robolectric.shadows.ShadowLooper;

@ExtendWith(RobolectricExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JupiterFeaturesTest {
  private final Context context = ApplicationProvider.getApplicationContext();
  private final List<String> events = new ArrayList<>();

  @BeforeEach
  void setUp() {
    events.add("setUp");
  }

  @AfterEach
  void tearDown() {
    assertThat(events).contains("setUp");
  }

  @Test
  @Order(1)
  void writesPreferences() {
    preferences().edit().putString("key", "value").commit();
    assertThat(preferences().getString("key", null)).isEqualTo("value");
  }

  @Test
  @Order(2)
  void preferencesAreResetBetweenTests() {
    assertThat(preferences().getString("key", null)).isNull();
  }

  @Test
  void mainLooperIsPaused() {
    new Handler(Looper.getMainLooper()).post(() -> events.add("posted"));
    assertThat(events).doesNotContain("posted");

    ShadowLooper.idleMainLooper();

    assertThat(events).contains("posted");
  }

  @ParameterizedTest
  @CsvSource({"https://example.com/path, example.com", "content://authority/item, authority"})
  void parsesUris(String uri, String expectedAuthority) {
    assertThat(Uri.parse(uri).getAuthority()).isEqualTo(expectedAuthority);
  }

  @TestFactory
  Stream<DynamicTest> dynamicTests() {
    return Stream.of("a", "b")
        .map(key -> dynamicTest(key, () -> assertThat(preferences().contains(key)).isFalse()));
  }

  @Nested
  class WhenNested {
    @Test
    void sharesApplicationContext() {
      assertThat(context).isSameInstanceAs(ApplicationProvider.getApplicationContext());
    }
  }

  private SharedPreferences preferences() {
    return context.getSharedPreferences("test", Context.MODE_PRIVATE);
  }
}
