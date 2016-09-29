package org.robolectric.internal.bytecode;

import org.junit.Test;
import org.robolectric.annotation.Implementation;

import java.lang.reflect.Method;

import static android.os.Build.VERSION_CODES;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;

public class ShadowConfigTest {
  @Test
  public void supportsSdk() throws Exception {
    ShadowConfig shadowConfig = new ShadowConfig("some-class", false, false, false, -1, -1);
//    shadowConfig.methodSupportsSdk(getMethod("methodWithoutFromOrTo"))
//    System.out.println(implementation);
  }

  private Method getMethod(String methodWithoutFromOrTo) throws NoSuchMethodException {
    return ShadowConfigTest.class
          .getMethod(methodWithoutFromOrTo);
  }

  @Implementation
  void methodWithoutFromOrTo() {
  }

  @Implementation(minSdk = KITKAT_WATCH)
  void methodWithFrom() {
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  void methodWithTo() {
  }

  @Implementation(minSdk = KITKAT)
  void methodWithFromAndTo() {
  }

  @Implementation(minSdk = KITKAT_WATCH)
  void methodWithSameFromAndTo() {
  }
}