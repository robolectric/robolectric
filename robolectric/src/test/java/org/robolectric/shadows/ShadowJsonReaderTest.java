package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.util.JsonReader;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.StringReader;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowJsonReaderTest {
  @Test public void shouldWork() throws Exception {
    JsonReader jsonReader = new JsonReader(new StringReader("{\"abc\": \"def\"}"));
    jsonReader.beginObject();
    assertThat(jsonReader.nextName()).isEqualTo("abc");
    assertThat(jsonReader.nextString()).isEqualTo("def");
    jsonReader.endObject();
  }
}
