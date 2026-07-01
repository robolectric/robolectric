package org.robolectric.junit.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A rule that lets you override system properties for tests. All properties are restored after each
 * test.
 */
public class SetSystemPropertyRule implements TestRule {

  private final Map<String, String> originalProperties = new HashMap<>();

  public SetSystemPropertyRule() {}

  public void set(String key, String value) {
    Objects.requireNonNull(key);
    if (!originalProperties.containsKey(key)) {
      originalProperties.put(key, System.getProperty(key));
    }
    System.setProperty(key, value);
  }

  public void clear(String key) {
    Objects.requireNonNull(key);
    if (!originalProperties.containsKey(key)) {
      originalProperties.put(key, System.getProperty(key));
    }
    System.clearProperty(key);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
        } finally {
          restoreProperties();
        }
      }
    };
  }

  private void restoreProperties() {
    for (Map.Entry<String, String> entry : originalProperties.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (value != null) {
        System.setProperty(key, value);
      } else {
        System.clearProperty(key);
      }
    }
    originalProperties.clear();
  }
}
