package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.processing.objects.ParameterizedDummy;

@Implements(ParameterizedDummy.class)
public class ShadowRealObjectParameterizedMismatch<T,S extends Number> {

  @RealObject
  ParameterizedDummy<S,T> someField;
}
