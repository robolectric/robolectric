package org.robolectric.annotation.processing.objects;

public class OuterDummy2 {
  protected class InnerProtected {
  }
  
  class InnerPackage {
  }

  @SuppressWarnings("unused")
  private class InnerPrivate {
    
  }
}
