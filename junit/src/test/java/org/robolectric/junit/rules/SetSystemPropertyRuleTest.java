package org.robolectric.junit.rules;

import static com.google.common.truth.Truth.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

/** Tests for {@link SetSystemPropertyRule}. */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4.class)
public final class SetSystemPropertyRuleTest {

  @Rule public final SetSystemPropertyRule rule = new SetSystemPropertyRule();

  /** Creates a system property with an initial state. */
  @BeforeClass
  public static void beforeClass() {
    System.setProperty("robolectric.test.property", "false");
  }

  /** Overrides the system property. */
  @Test
  public void test1_set_overridesSystemProperties() {
    rule.set("robolectric.test.property", "true");
    assertThat(System.getProperty("robolectric.test.property")).isEqualTo("true");
  }

  /** Checks that the rule has restored the original value after the test completed. */
  @Test
  public void test2_systemProperties_areRestored_afterTest() {
    assertThat(System.getProperty("robolectric.test.property")).isEqualTo("false");
  }

  /** Clears the system property. */
  @AfterClass
  public static void afterClass() {
    assertThat(System.getProperty("robolectric.test.property")).isEqualTo("false");
    System.clearProperty("robolectric.test.property");
  }
}
