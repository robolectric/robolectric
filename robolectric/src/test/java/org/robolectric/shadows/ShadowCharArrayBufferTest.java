package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.apache.http.util.CharArrayBuffer;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowCharArrayBufferTest {

  @Test
  public void append_shouldBeInstrumented() {
    // CharArrayBuffer.expand uses a modified version of java.lang.System.arraycopy.
    // The class needs to be instrumented and re-written in order to work correctly.
    final CharArrayBuffer buffer = new CharArrayBuffer(1);
    buffer.append("Foo");
    assertThat(buffer.toString()).isEqualTo("Foo");
  }
}
