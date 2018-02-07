package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.util.JsonReader;
import java.io.StringReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowJsonReaderTest {
  @Test public void shouldWork() throws Exception {
    JsonReader jsonReader = new JsonReader(new StringReader("{\"abc\": \"def\"}"));
    jsonReader.beginObject();
    assertThat(jsonReader.nextName()).isEqualTo("abc");
    assertThat(jsonReader.nextString()).isEqualTo("def");
    jsonReader.endObject();
  }
}
