package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import dalvik.system.VMRuntime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
public final class ShadowVMRuntimeTest {
  @Test
  public void newNonMovableArray_floatArray() {
    VMRuntime runtime = VMRuntime.getRuntime();
    float[] result = (float[]) runtime.newNonMovableArray(float.class, 10);
    assertThat(result).hasLength(10);
    assertThat(runtime.addressOf(result)).isEqualTo(runtime.addressOf(result));
    ShadowVMRuntime shadow = Shadow.extract(runtime);
    assertThat(shadow.getObjectForAddress(runtime.addressOf(result))).isSameInstanceAs(result);
  }

  @Test
  public void addressOf_notArray() {
    assertThrows(
        IllegalArgumentException.class, () -> VMRuntime.getRuntime().addressOf(new Object()));
  }

  @Test
  public void addressOf_notPrimitiveArray() {
    assertThrows(
        IllegalArgumentException.class, () -> VMRuntime.getRuntime().addressOf(new Object[0]));
  }

  @Test
  public void addressOf_notNonMoveableArray() {
    assertThat(VMRuntime.getRuntime().addressOf(new float[0])).isEqualTo(0);
  }
}
