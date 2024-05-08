package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DataTypeTest {

  @Test
  public void fromCode_shouldThrowExceptionForInvalidCode() {
    assertThrows(IllegalArgumentException.class, () -> DataType.fromCode(99));
  }

  @Test
  public void fromCode_shouldReturnCorrectDataTypeForValidCode() {
    assertThat(DataType.fromCode(0)).isEqualTo(DataType.NULL);
    assertThat(DataType.fromCode(3)).isEqualTo(DataType.STRING);
  }
}
