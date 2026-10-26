package org.robolectric.junit.platform.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;

class JupiterDelegateTest {
  private static final UniqueId SANDBOX =
      UniqueId.forEngine("robolectric").append("sandbox", "sdk=34");
  private static final UniqueId JUPITER_TEST =
      UniqueId.forEngine("junit-jupiter")
          .append("class", "com.example.MyTest")
          .append("method", "test()");

  @Test
  void toOuter_reparentsUnderSandbox() {
    assertThat(JupiterDelegate.toOuter(JUPITER_TEST, SANDBOX))
        .isEqualTo(SANDBOX.append("class", "com.example.MyTest").append("method", "test()"));
  }

  @Test
  void toJupiter_isInverseOfToOuter() {
    UniqueId outer = JupiterDelegate.toOuter(JUPITER_TEST, SANDBOX);

    assertThat(JupiterDelegate.toJupiter(outer, SANDBOX)).isEqualTo(JUPITER_TEST);
  }
}
