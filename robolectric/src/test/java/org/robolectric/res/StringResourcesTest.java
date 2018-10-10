package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
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

    assertThat(StringResources.escape("This is a \\\"good string\\\".")).isEqualTo("This is a \"good string\".");
    assertThat(StringResources.escape("This is a \"bad string with unescaped double quotes\"."))
        .isEqualTo("This is a bad string with unescaped double quotes.");

    assertThat(StringResources.escape("Text with escaped backslash followed by an \\\\\"unescaped double quote."))
        .isEqualTo("Text with escaped backslash followed by an \\unescaped double quote.");
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

  @Test
  public void shouldTrimWhitespace() {
    assertThat(StringResources.processStringResources("    ")).isEmpty();
    assertThat(StringResources.processStringResources("Trailingwhitespace    ")).isEqualTo("Trailingwhitespace");
    assertThat(StringResources.processStringResources("Leadingwhitespace    ")).isEqualTo("Leadingwhitespace");
  }

  @Test
  public void shouldCollapseInternalWhiteSpaces() {
    assertThat(StringResources.processStringResources("Whitespace     in     the          middle")).isEqualTo("Whitespace in the middle");
    assertThat(StringResources.processStringResources("Some\n\n\n\nNewlines")).isEqualTo("Some Newlines");
  }

  @Test
  public void escape_shouldRemoveUnescapedDoubleQuotes() {
    Map<String, String> tests = new HashMap<>();
    tests.put("a\\\"b", "a\"b");
    tests.put("a\\\\\"b", "a\\b");
    tests.put("a\\\\\\\"b", "a\\\"b");
    tests.put("a\\\\\\\\\"b", "a\\\\b");

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
    List<String> tests = new ArrayList<>();
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
