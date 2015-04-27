package org.robolectric.internal;

public class ShadowExtractor {
  public static Object extract(Object instance) {
    return ((ShadowedObject) instance).$$robo$getData();
  }
}
