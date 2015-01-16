package org.robolectric.bytecode;

import java.lang.annotation.Annotation;

public interface ClassInfo {
  String getName();

  boolean isInterface();

  boolean isAnnotation();

  boolean hasAnnotation(Class<? extends Annotation> annotationClass);
}
