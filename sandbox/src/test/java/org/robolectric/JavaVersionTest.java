package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.JavaVersion;

@RunWith(JUnit4.class)
public class JavaVersionTest {
  @Test
  public void jdk8() {
    check("1.8.1u40", "1.8.5u60");
    check("1.8.0u40", "1.8.0u60");
    check("1.8.0u40", "1.8.0u100");
  }

  @Test
  public void jdk9() {
    check("9.0.1+12", "9.0.2+12");
    check("9.0.2+60", "9.0.2+100");
  }

  @Test
  public void differentJdk() {
    check("1.7.0", "1.8.0u60");
    check("1.8.1u40", "9.0.2+12");
  }

  @Test
  public void longer() {
    check("1.8.0", "1.8.0.1");
  }

  @Test
  public void longerEquality() {
    checkEqual("1.8.0", "1.8.0");
    checkEqual("1.8.0u33", "1.8.0u33");
    checkEqual("5", "5");
  }

  private static void check(String v1, String v2) {
    assertThat(new JavaVersion(v1).compareTo(new JavaVersion(v2))).isLessThan(0);
  }

  private static void checkEqual(String v1, String v2) {
    assertThat(new JavaVersion(v1).compareTo(new JavaVersion(v2))).isEqualTo(0);
  }

}