package org.robolectric.junit.platform.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClaimRegistryTest {
  @AfterEach
  void tearDown() {
    ClaimRegistry.reset();
  }

  @Test
  void claimedClass_isClaimed() {
    ClaimRegistry.claim(ClaimRegistryTest.class);

    assertThat(ClaimRegistry.isClaimed(ClaimRegistryTest.class.getName())).isTrue();
    assertThat(ClaimRegistry.isClaimed(String.class.getName())).isFalse();
  }

  @Test
  void nestedClassesOfClaimedClass_areClaimed() {
    ClaimRegistry.claim(ClaimRegistryTest.class);

    assertThat(ClaimRegistry.isClaimed(ClaimRegistryTest.class.getName() + "$Nested")).isTrue();
    assertThat(ClaimRegistry.isClaimed(ClaimRegistryTest.class.getName() + "$Nested$Deeper"))
        .isTrue();
  }

  @Test
  void reset_removesClaims() {
    ClaimRegistry.claim(ClaimRegistryTest.class);

    ClaimRegistry.reset();

    assertThat(ClaimRegistry.isClaimed(ClaimRegistryTest.class.getName())).isFalse();
  }
}
