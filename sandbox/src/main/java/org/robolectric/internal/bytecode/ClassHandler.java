package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * This interface is used by Robolectric when instrumented classes are created and interacted with.
 */
public interface ClassHandler {

  /**
   * Called by Robolectric when an instrumented class is first loaded into a sandbox and is ready
   * to be statically initialized.
   *
   * This happens *in place of* any static initialization that may be performed by the class being
   * loaded. The class will have a method named `__staticInitializer__` which may be invoked to
   * perform its normal initialization from `<clinit>`.
   *
   * @param clazz the class being loaded
   */
  void classInitializing(Class clazz);

  /**
   * Called by Robolectric when a new instance of an instrumented class has been created and is
   * ready to be initialized (but only on JVMs which don't support the `invokedynamic` instruction).
   *
   * This happens before constructor code executes on the new instance.
   *
   * Implementations may return an object which will be associated with the new instance and passed
   * along on future calls to {@link #methodInvoked(String, boolean, Class)}.
   *
   * @param instance the newly-created instance
   * @return a data value to be associated with the new instance
   * @see #getShadowCreator(Class) for newer JVMs
   */
  Object initializing(Object instance);

  /**
   * Called by Robolectric when an instrumented method is invoked.
   *
   * Implementations should return an {@link Plan}, which will be invoked with details about the
   * current instance and parameters.
   *
   * Implementations may also return `null`, in which case the method's original code will be
   * executed.
   *
   * @param signature the JVM internal-format signature of the method being invoked (e.g.
   *                  `android/view/View/measure(II)V`)
   * @param isStatic true if the method is static
   * @param theClass the class on which the method is declared
   * @return an execution plan, or `null` if the original method's code should be executed
   * @see #findShadowMethodHandle(Class, String, MethodType, boolean) for newer JVMs
   */
  Plan methodInvoked(String signature, boolean isStatic, Class<?> theClass);

  /**
   * Called by Robolectric to determine how to create and initialize a shadow object when a new
   * instance of an instrumented class has been instantiated. (but only on JVMs which support the
   * `invokedynamic` instruction).
   *
   * The returned {@link MethodHandle} will be invoked after the new object has been allocated
   * but before its constructor code is executed.
   *
   * Note that this is not directly analogous to {@link #initializing(Object)}; the return value
   * from this method will be cached and used again for other instantiations of instances of the
   * same class.
   *
   * @param theClass the instrumented class
   * @return a data value to be associated with the new instance
   * @see #getShadowCreator(Class) for older JVMs
   * @see ShadowInvalidator for invalidating the returned {@link MethodHandle}
   */
  MethodHandle getShadowCreator(Class<?> theClass);

  /**
   * Called by Robolectric when an instrumented method is invoked.
   *
   * Implementations should return an {@link MethodHandle}, which will be invoked with details about
   * the current instance and parameters.
   *
   * Implementations may also return `null`, in which case the method's original code will be
   * executed.
   *
   * @param theClass the class on which the method is declared
   * @param name the name of the method
   * @param methodType the method type
   * @param isStatic true if the method is static
   * @return a method handle to invoke, or `null` if the original method's code should be executed
   * @see #methodInvoked(String, boolean, Class) for older JVMs
   * @see ShadowInvalidator for invalidating the returned {@link MethodHandle}
   */
  MethodHandle findShadowMethodHandle(Class<?> theClass, String name, MethodType methodType,
      boolean isStatic) throws IllegalAccessException;

  /**
   * Called by Robolectric when an intercepted method is invoked.
   *
   * Unlike instrumented methods, calls to intercepted methods are modified in place by Robolectric
   * in the calling code. This is useful when the method about to be invoked doesn't exist in the
   * current JVM (e.g. because of Android differences).
   *
   * @param signature the JVM internal-format signature of the method being invoked (e.g.
   *                  `android/view/View/measure(II)V`)
   * @param instance the instance on which the method would have been invoked
   * @param params the parameters to the method
   * @param theClass the class on which the method is declared
   * @return the value to be returned
   * @throws Throwable if anything bad happens
   */
  Object intercept(String signature, Object instance, Object[] params, Class<?> theClass)
      throws Throwable;

  /**
   * Removes Robolectric noise from stack traces.
   *
   * @param throwable the exception to be stripped
   * @param <T> the type of exception
   * @return the stripped stack trace
   */
  <T extends Throwable> T stripStackTrace(T throwable);

  /**
   * An execution plan, which can be invoked to satisfy a requested method call.
   */
  interface Plan {

    Object run(Object instance, Object[] params) throws Throwable;

    String describe();
  }
}
