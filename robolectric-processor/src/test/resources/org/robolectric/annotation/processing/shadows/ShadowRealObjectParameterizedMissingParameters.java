package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import com.example.objects.ParameterizedDummy;

@Implements(ParameterizedDummy.class)
public class ShadowRealObjectParameterizedMissingParameters<T,S extends Number> {

  @RealObject
  ParameterizedDummy someField;
}
