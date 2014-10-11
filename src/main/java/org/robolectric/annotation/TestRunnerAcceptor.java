package org.robolectric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method wants to receive active RobolectricTestRunner instance before running tests.
 * Methods marked with TestRunnerAcceptor annotation will be called before all the methods with BeforeClass annotation,
 * and these methods must accept exactly one argument with type RobolectricTestRunner.
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TestRunnerAcceptor {
}
