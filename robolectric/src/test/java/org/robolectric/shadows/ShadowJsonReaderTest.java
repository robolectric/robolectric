package org.robolectric.shadows;

import android.util.JsonReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowJsonReaderTest {
  @Test public void shouldWork() throws Exception {
    JsonReader jsonReader = new JsonReader(new StringReader("{\"abc\": \"def\"}"));
    jsonReader.beginObject();
    assertThat(jsonReader.nextName()).isEqualTo("abc");
    assertThat(jsonReader.nextString()).isEqualTo("def");
    jsonReader.endObject();
  }
}
