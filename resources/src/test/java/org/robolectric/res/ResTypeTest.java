package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link ResType}
 */
@RunWith(JUnit4.class)
public class ResTypeTest {

  @Test
  public void testInferFromValue() {

    assertThat(ResType.inferFromValue("#802C76AD")).isEqualTo(ResType.COLOR);

    assertThat(ResType.inferFromValue("true")).isEqualTo(ResType.BOOLEAN);
    assertThat(ResType.inferFromValue("false")).isEqualTo(ResType.BOOLEAN);

    assertThat(ResType.inferFromValue("10dp")).isEqualTo(ResType.DIMEN);
    assertThat(ResType.inferFromValue("10sp")).isEqualTo(ResType.DIMEN);
    assertThat(ResType.inferFromValue("10pt")).isEqualTo(ResType.DIMEN);
    assertThat(ResType.inferFromValue("10px")).isEqualTo(ResType.DIMEN);
    assertThat(ResType.inferFromValue("10in")).isEqualTo(ResType.DIMEN);

    assertThat(ResType.inferFromValue("10")).isEqualTo(ResType.INTEGER);

    assertThat(ResType.inferFromValue("10.9")).isEqualTo(ResType.FRACTION);
  }
}
