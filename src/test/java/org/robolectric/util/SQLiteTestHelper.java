package org.robolectric.util;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class SQLiteTestHelper {

  static void verifyColumnValues(List<Object> colValues) {
    assertThat(colValues.get(0)).isInstanceOf(Float.class);
    assertThat(colValues.get(1)).isInstanceOf(byte[].class);
    assertThat(colValues.get(2)).isInstanceOf(String.class);
    assertThat(colValues.get(3)).isInstanceOf(Integer.class);
  }

}
