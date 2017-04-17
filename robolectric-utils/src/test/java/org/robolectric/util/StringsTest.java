package org.robolectric.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;

@RunWith(JUnit4.class)
public class StringsTest {
  @Test
  public void shouldGetStringFromStream() throws Exception {
    InputStream stream = new ByteArrayInputStream("some random string".getBytes());
    assertEquals("some random string", Strings.fromStream(stream));
  }
}
