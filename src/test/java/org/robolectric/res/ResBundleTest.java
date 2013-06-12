package org.robolectric.res;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResBundleTest {
  @Test
  public void shouldMatchQualifiersPerAndroidSpec() throws Exception {
    assertEquals("en-port", ResBundle.pick(asValues(
        "",
        "en",
        "fr-rCA",
        "en-port",
        "en-notouch-12key",
        "port-ldpi",
        "port-notouch-12key"), "en-GB-port-hdpi-notouch-12key").value);
  }

  private ResBundle.Values<String> asValues(String... qualifierses) {
    ResBundle.Values<String> values = new ResBundle.Values<String>();
    for (String qualifiers : qualifierses) {
      values.add(new ResBundle.Value<String>(qualifiers, qualifiers, null));
    }
    return values;
  }
}
