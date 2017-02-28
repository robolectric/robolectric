package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import com.example.objects.ParameterizedDummy;

@Implements(ParameterizedDummy.class)
public class ShadowRealObjectParameterizedMismatch<T,S extends Number> {

  @RealObject
  ParameterizedDummy<S,T> someField;
}
