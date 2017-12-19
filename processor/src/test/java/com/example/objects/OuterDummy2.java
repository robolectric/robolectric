package com.example.objects;

public class OuterDummy2 {
  @SuppressWarnings("ClassCanBeStatic")
  protected class InnerProtected {
  }

  @SuppressWarnings("ClassCanBeStatic")
  class InnerPackage {
  }

  @SuppressWarnings(value = {"unused", "ClassCanBeStatic"})
  private class InnerPrivate {
    
  }
}
