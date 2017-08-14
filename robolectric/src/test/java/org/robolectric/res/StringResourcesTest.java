package org.robolectric.res;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StringResourcesTest {
  @Test
  public void escape_shouldEscapeStrings() {
    assertThat(StringResources.escape("\"This'll work\"")).isEqualTo("This'll work");
    assertThat(StringResources.escape("This\\'ll also work")).isEqualTo("This'll also work");
  }

  @Test
  public void escape_shouldEscapeCodePoints() {
    Map<String, String> tests = new HashMap<>();
    tests.put("\\u0031", "1");
    tests.put("1\\u0032", "12");
    tests.put("\\u00312", "12");
    tests.put("1\\u00323", "123");
    tests.put("\\u005A", "Z");
    tests.put("\\u005a", "Z");

    for (Map.Entry<String, String> t : tests.entrySet()) {
      assertThat(StringResources.processStringResources(t.getKey())).isEqualTo(t.getValue());
    }
  }

  // Unsupported escape codes should be ignored.
  @Test
  public void escape_shouldIgnoreUnsupportedEscapeCodes() {
    assertThat(StringResources.processStringResources("\\ \\a\\b\\c\\d\\e\\ ")).isEqualTo("");
  }

  @Test
  public void escape_shouldSupport() {
    Map<String, String> tests = new HashMap<>();
    tests.put("\\\\", "\\");
    tests.put("domain\\\\username", "domain\\username");
    for (Map.Entry<String, String> t : tests.entrySet()) {
      assertThat(StringResources.processStringResources(t.getKey())).isEqualTo(t.getValue());
    }
  }

  @Test
  public void testInvalidCodePoints() {
    List<String> tests = new LinkedList<>();
    tests.add("\\u");
    tests.add("\\u0");
    tests.add("\\u00");
    tests.add("\\u004");
    tests.add("\\uzzzz");
    tests.add("\\u0zzz");
    tests.add("\\u00zz");
    tests.add("\\u000z");
    for (String t : tests) {
      try {
        StringResources.processStringResources(t);
        fail("expected IllegalArgumentException with test '" + t + "'");
      } catch (IllegalArgumentException expected) {
        // cool
      }
    }
  }
}
