package org.robolectric.json;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class JSONArrayTest {
  @Test
  public void testEquality() throws Exception {
    JSONArray array = new JSONArray(Arrays.asList("a", "b"));
    assertThat(array).isEqualTo(new JSONArray(array.toString()));
  }
}
