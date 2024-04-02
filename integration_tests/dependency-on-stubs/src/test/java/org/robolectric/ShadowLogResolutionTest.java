package org.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.shadows.ShadowLog;

/**
 * This test attempts to reference ShadowLow from outside of the context of a Robolectric
 * environment. ShadowLog contained reflector interfaces that referenced `@hide` classes (e.g.
 * TerribleFailure), which cannot be referenced outside of a Robolectric ClassLoader.
 *
 * @see <a href="https://github.com/robolectric/robolectric/issues/8957">Issue 8957 </a> for more
 *     details.
 */
@RunWith(JUnit4.class)
public class ShadowLogResolutionTest {
  @Test
  public void reference_shadowLog_outsideRobolectric() {
    ShadowLog.stream = System.out;
  }
}
