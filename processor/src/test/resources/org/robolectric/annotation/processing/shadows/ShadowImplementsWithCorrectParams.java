package org.robolectric.annotation.processing.shadows;

import com.example.objects.ParameterizedDummy;
import org.robolectric.annotation.Implements;

@Implements(ParameterizedDummy.class)
public class ShadowImplementsWithCorrectParams<T, N extends Number> {}
