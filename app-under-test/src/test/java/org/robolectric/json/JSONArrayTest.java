package org.robolectric.json;

import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class JSONArrayTest {
  @Test
  public void testEquality() throws Exception {
    JSONArray array = new JSONArray(Arrays.asList("a", "b"));
    assertThat(array).isEqualTo(new JSONArray(array.toString()));
  }
}
