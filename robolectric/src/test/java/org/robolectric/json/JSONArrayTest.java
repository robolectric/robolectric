package org.robolectric.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class JSONArrayTest {
  @Test
  public void testEquality() throws Exception {
    JSONArray array = new JSONArray(Arrays.asList("a", "b"));
    assertThat(array).isEqualTo(new JSONArray(array.toString()));
  }
}
