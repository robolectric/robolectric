package org.robolectric.versioning;

import static com.google.common.truth.Truth.assertThat;

import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.versioning.AndroidVersions.AndroidRelease;

/**
 * Check versions information aligns with runtime information. Primarily, selected SDK with
 * internally detected version number.
 */
@RunWith(JUnit4.class)
public final class AndroidVersionsTest {

  @Test
  public void testUnreleased() {
    assertThat(
            AndroidVersions.getReleases().stream()
                .map(AndroidRelease::getSdkInt)
                .collect(Collectors.toList()))
        .containsExactly(
            16, 17, 18, 19, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36);
    assertThat(
            AndroidVersions.getUnreleased().stream()
                .map(AndroidRelease::getSdkInt)
                .collect(Collectors.toList()))
        .containsExactly(10000);
  }
}
