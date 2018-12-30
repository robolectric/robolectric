package org.robolectric.internal;

import org.robolectric.Bridge;

public class AndroidBridge implements Bridge {

  interface FactoryI {

    Bridge build();
  }

  static class Factory implements FactoryI {

    public Bridge build() {
      return new AndroidBridge();
    }
  }
}
