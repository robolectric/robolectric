package com.xtremelabs.robolectric.annotation;

/**
 * Annotation to run test with setFinalStaticField() defined in a synchronized 
 * block with automatic reversion to the original value.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE,java.lang.annotation.ElementType.METHOD})
public @interface WithConstantInt {
	@SuppressWarnings("rawtypes")
	Class classWithField();
	String fieldName();
	int newValue();
}
