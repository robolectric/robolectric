package org.robolectric;

import android.database.CursorWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowCursorWrapper;

import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Parameterized tests using custom shadow classes.
 *
 * @author John Ferlisi
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public final class ParameterizedRobolectricTestRunnerConfigTest {

  private final int expectedType;

  public ParameterizedRobolectricTestRunnerConfigTest(int expectedType) {
    this.expectedType = expectedType;
  }

  @Test
  @Config(manifest = Config.NONE, shadows = ShadowCursorWrapper1.class)
  public void getType1() {
    assertThat(new CursorWrapper(null).getType(expectedType)).isEqualTo(1);
  }

  @Test
  @Config(manifest = Config.NONE, shadows = ShadowCursorWrapperEcho.class)
  public void getTypeEcho() {
    assertThat(new CursorWrapper(null).getType(expectedType)).isEqualTo(expectedType);
  }

  @ParameterizedRobolectricTestRunner.Parameters(name = "ConfigTest: {0}")
  public static Collection getTestData() {
    Object[][] data = {
        { 1 },
        { 2 },
        { 3 },
        { 4 }
    };
    return Arrays.asList(data);
  }

  @Implements(CursorWrapper.class)
  public static class ShadowCursorWrapper1 extends ShadowCursorWrapper {

    @Implementation
    @Override
    public int getType(int columnIndex) {
      return 1;
    }
  }

  @Implements(CursorWrapper.class)
  public static class ShadowCursorWrapperEcho extends ShadowCursorWrapper {

    @Implementation
    @Override
    public int getType(int columnIndex) {
      return columnIndex;
    }
  }
}
