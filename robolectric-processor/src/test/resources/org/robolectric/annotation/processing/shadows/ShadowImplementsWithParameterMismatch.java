package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.processing.objects.ParameterizedDummy;

@Implements(ParameterizedDummy.class)
public class ShadowImplementsWithParameterMismatch<N extends Number,T> {
}
