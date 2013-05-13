package org.robolectric.annotation;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class WithConstantStringTest {

  private static final String NEW_VALUE = "HTC";

  @Test
  @WithConstantString(classWithField = android.os.Build.class, fieldName = "MANUFACTURER", newValue = NEW_VALUE)
  public void testWithConstantString() {
    assertThat(Build.MANUFACTURER).isEqualTo(NEW_VALUE);
  }

  @Test
  public void testWithoutConstantString() {
    assertThat(Build.MANUFACTURER).isEqualTo(Build.UNKNOWN);
  }

}
