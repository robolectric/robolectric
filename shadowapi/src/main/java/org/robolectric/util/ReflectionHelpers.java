package org.robolectric.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.robolectric.annotation.ClassName;

/** Collection of helper methods for calling methods and accessing fields reflectively. */
@SuppressWarnings(value = {"unchecked", "TypeParameterUnusedInFormals", "NewApi"})
public class ReflectionHelpers {

  private static final Map<String, Object> PRIMITIVE_RETURN_VALUES;
  private static final PerfStatsCollector perfStatsCollector = PerfStatsCollector.getInstance();

  static {
    HashMap<String, Object> map = new HashMap<>();
    map.put("boolean", Boolean.FALSE);
    map.put("int", 0);
    map.put("long", (long) 0);
    map.put("float", (float) 0);
    map.put("double", (double) 0);
    map.put("short", (short) 0);
    map.put("byte", (byte) 0);
    PRIMITIVE_RETURN_VALUES = Collections.unmodifiableMap(map);
  }

  /**
   * Create a proxy for the given class which returns default values for every method call.
   *
   * <p>0 will be returned for any primitive return types, otherwise null will be returned.
   *
   * @param clazz the class to provide a proxy instance of.
   * @return a new "Null Proxy" instance of the given class.
   */
  public static <T> T createNullProxy(Class<T> clazz) {
    return (T)
        Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class<?>[] {clazz},
            (proxy, method, args) -> PRIMITIVE_RETURN_VALUES.get(method.getReturnType().getName()));
  }

  /**
   * Create a proxy for the given class which returns other deep proxies from all it's methods.
   *
   * <p>The returned object will be an instance of the given class, but all methods will return
   * either the "default" value for primitives, or another deep proxy for non-primitive types.
   *
   * <p>This should be used rarely, for cases where we need to create deep proxies in order not to
   * crash. The inner proxies are impossible to configure, so there is no way to create meaningful
   * behavior from a deep proxy. It serves mainly to prevent Null Pointer Exceptions.
   *
   * @param clazz the class to provide a proxy instance of.
   * @return a new "Deep Proxy" instance of the given class.
   */
  public static <T> T createDeepProxy(Class<T> clazz) {
    return (T)
        Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class[] {clazz},
            (proxy, method, args) -> {
              if (PRIMITIVE_RETURN_VALUES.containsKey(method.getReturnType().getName())) {
                return PRIMITIVE_RETURN_VALUES.get(method.getReturnType().getName());
              } else if (method.getReturnType().isInterface()) {
                return createDeepProxy(method.getReturnType());
              } else {
                return null;
              }
            });
  }

  /**
   * Create a proxy for the given class which can delegate method calls to another object.
   *
   * <p>If the delegate has no methods whose signature matches, a null (or 0 for primitive types)
   * return value will be returned.
   *
   * @param clazz the class to provide a proxy instance of.
   * @param delegate the object to delegate matching method calls to. A 'matching method' must have
   *     exactlu the same method name and parameter class names as the desired method.
   *     The @ClassName annotation can be applied to provide a custom class name.
   * @return a new "Delegating Proxy" instance of the given class.
   */
  public static <T> T createDelegatingProxy(Class<T> clazz, final Object delegate) {
    final Class delegateClass = delegate.getClass();
    return (T)
        Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class[] {clazz},
            (proxy, method, args) -> {
              try {
                Method delegateMethod =
                    findDelegateMethod(delegateClass, method.getName(), method.getParameterTypes());
                delegateMethod.setAccessible(true);
                return delegateMethod.invoke(delegate, args);
              } catch (NoSuchMethodException e) {
                return PRIMITIVE_RETURN_VALUES.get(method.getReturnType().getName());
              } catch (InvocationTargetException e) {
                // Required to propagate the correct throwable.
                throw e.getTargetException();
              }
            });
  }

  private static Method findDelegateMethod(
      Class<?> delegateClass, String methodName, Class<?>[] parameterTypes)
      throws NoSuchMethodException {
    for (Method delegateMethod : delegateClass.getMethods()) {
      if (delegateMethod.getName().equals(methodName)
          && Modifier.isPublic(delegateMethod.getModifiers())
          && parametersMatch(
              parameterTypes,
              delegateMethod.getParameterTypes(),
              delegateMethod.getParameterAnnotations())) {
        return delegateMethod;
      }
    }
    throw new NoSuchMethodException();
  }

  private static boolean parametersMatch(
      Class<?>[] parameterTypes,
      Class<?>[] delegateParameterTypes,
      Annotation[][] parameterAnnotations) {
    if (parameterTypes.length != delegateParameterTypes.length) {
      return false;
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      if (!parameterTypes[i].getName().equals(delegateParameterTypes[i].getName())
          && !parameterTypes[i]
              .getName()
              .equals(getClassNameFromAnnotation(parameterAnnotations[i]))) {
        return false;
      }
    }
    return true;
  }

  @Nullable
  private static String getClassNameFromAnnotation(Annotation[] parameterAnnotations) {
    for (Annotation annotation : parameterAnnotations) {
      if (annotation.annotationType().equals(ClassName.class)) {
        return ((ClassName) annotation).value();
      }
    }
    return null;
  }

  public static <A extends Annotation> A defaultsFor(Class<A> annotation) {
    return annotation.cast(
        Proxy.newProxyInstance(
            annotation.getClassLoader(),
            new Class[] {annotation},
            (proxy, method, args) -> method.getDefaultValue()));
  }

  /**
   * Reflectively get the value of a field.
   *
   * @param object Target object.
   * @param fieldName The field name.
   * @param <R> The return type.
   * @return Value of the field on the object.
   */
  @SuppressWarnings("unchecked")
  public static <R> R getField(final Object object, final String fieldName) {
    try {
      return traverseClassHierarchy(
          object.getClass(),
          NoSuchFieldException.class,
          traversalClass -> {
            Field field = traversalClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (R) field.get(object);
          });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively set the value of a field.
   *
   * @param object Target object.
   * @param fieldName The field name.
   * @param fieldNewValue New value.
   */
  public static void setField(
      final Object object, final String fieldName, final Object fieldNewValue) {
    try {
      traverseClassHierarchy(
          object.getClass(),
          NoSuchFieldException.class,
          (InsideTraversal<Void>)
              traversalClass -> {
                Field field = traversalClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, fieldNewValue);
                return null;
              });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively set the value of a field.
   *
   * @param type Target type.
   * @param object Target object.
   * @param fieldName The field name.
   * @param fieldNewValue New value.
   */
  public static void setField(
      Class<?> type, final Object object, final String fieldName, final Object fieldNewValue) {
    try {
      Field field = type.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(object, fieldNewValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively check if a class has a given field (static or non static).
   *
   * @param clazz Target class.
   * @param fieldName The field name.
   * @return boolean to indicate whether the field exists or not in clazz.
   */
  public static boolean hasField(Class<?> clazz, String fieldName) {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      return (field != null);
    } catch (NoSuchFieldException e) {
      return false;
    }
  }

  /**
   * Reflectively get the value of a static field.
   *
   * @param field Field object.
   * @param <R> The return type.
   * @return Value of the field.
   */
  @SuppressWarnings("unchecked")
  public static <R> R getStaticField(Field field) {
    try {
      field.setAccessible(true);
      return (R) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively get the value of a static field.
   *
   * @param clazz Target class.
   * @param fieldName The field name.
   * @param <R> The return type.
   * @return Value of the field.
   */
  public static <R> R getStaticField(Class<?> clazz, String fieldName) {
    try {
      return getStaticField(clazz.getDeclaredField(fieldName));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively set the value of a static field.
   *
   * @param field Field object.
   * @param fieldNewValue The new value.
   */
  public static void setStaticField(Field field, Object fieldNewValue) {
    try {
      if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
        throw new IllegalArgumentException("Cannot set the value of final field " + field);
      }
      field.setAccessible(true);
      field.set(null, fieldNewValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively set the value of a static field.
   *
   * @param clazz Target class.
   * @param fieldName The field name.
   * @param fieldNewValue The new value.
   */
  public static void setStaticField(Class<?> clazz, String fieldName, Object fieldNewValue) {
    try {
      setStaticField(clazz.getDeclaredField(fieldName), fieldNewValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively call an instance method on an object.
   *
   * @param instance Target object.
   * @param methodName The method name to call.
   * @param classParameters Array of parameter types and values.
   * @param <R> The return type.
   * @return The return value of the method.
   */
  public static <R> R callInstanceMethod(
      final Object instance, final String methodName, ClassParameter<?>... classParameters) {
    perfStatsCollector.incrementCount(
        String.format(
            "ReflectionHelpers.callInstanceMethod-%s_%s",
            instance.getClass().getName(), methodName));
    try {
      final Class<?>[] classes = ClassParameter.getClasses(classParameters);
      final Object[] values = ClassParameter.getValues(classParameters);

      return traverseClassHierarchy(
          instance.getClass(),
          NoSuchMethodException.class,
          traversalClass -> {
            Method declaredMethod = traversalClass.getDeclaredMethod(methodName, classes);
            declaredMethod.setAccessible(true);
            return (R) declaredMethod.invoke(instance, values);
          });
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) e.getTargetException();
      }
      if (e.getTargetException() instanceof Error) {
        throw (Error) e.getTargetException();
      }
      throw new RuntimeException(e.getTargetException());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively call an instance method on an object on a specific class.
   *
   * @param cl The class.
   * @param instance Target object.
   * @param methodName The method name to call.
   * @param classParameters Array of parameter types and values.
   * @param <R> The return type.
   * @return The return value of the method.
   */
  public static <R> R callInstanceMethod(
      Class<?> cl,
      final Object instance,
      final String methodName,
      ClassParameter<?>... classParameters) {
    perfStatsCollector.incrementCount(
        String.format("ReflectionHelpers.callInstanceMethod-%s_%s", cl.getName(), methodName));
    try {
      final Class<?>[] classes = ClassParameter.getClasses(classParameters);
      final Object[] values = ClassParameter.getValues(classParameters);

      Method method = cl.getDeclaredMethod(methodName, classes);
      method.setAccessible(true);
      if (Modifier.isStatic(method.getModifiers())) {
        throw new IllegalArgumentException(method + " is static");
      }
      return (R) method.invoke(instance, values);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) e.getTargetException();
      }
      if (e.getTargetException() instanceof Error) {
        throw (Error) e.getTargetException();
      }
      throw new RuntimeException(e.getTargetException());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method for calling a static method using a class from a custom class loader
   *
   * @param classLoader The ClassLoader used to load class
   * @param fullyQualifiedClassName The full qualified class name with package name of the
   *     ClassLoader will load
   * @param methodName The method name will be called
   * @param classParameters The input parameters will be used for method calling
   * @param <R> Return type of the method
   * @return Return the value of the method
   */
  public static <R> R callStaticMethod(
      ClassLoader classLoader,
      String fullyQualifiedClassName,
      String methodName,
      ClassParameter<?>... classParameters) {
    Class<?> clazz = loadClass(classLoader, fullyQualifiedClassName);
    return callStaticMethod(clazz, methodName, classParameters);
  }

  /**
   * Reflectively call a static method on a class.
   *
   * @param clazz Target class.
   * @param methodName The method name to call.
   * @param classParameters Array of parameter types and values.
   * @param <R> The return type.
   * @return The return value of the method.
   */
  @SuppressWarnings("unchecked")
  public static <R> R callStaticMethod(
      Class<?> clazz, String methodName, ClassParameter<?>... classParameters) {
    perfStatsCollector.incrementCount(
        String.format("ReflectionHelpers.callStaticMethod-%s_%s", clazz.getName(), methodName));
    try {
      Class<?>[] classes = ClassParameter.getClasses(classParameters);
      Object[] values = ClassParameter.getValues(classParameters);

      Method method = clazz.getDeclaredMethod(methodName, classes);
      method.setAccessible(true);
      if (!Modifier.isStatic(method.getModifiers())) {
        throw new IllegalArgumentException(method + " is not static");
      }
      return (R) method.invoke(null, values);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) e.getTargetException();
      }
      if (e.getTargetException() instanceof Error) {
        throw (Error) e.getTargetException();
      }
      throw new RuntimeException(e.getTargetException());
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("no such method " + clazz + "." + methodName, e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Load a class.
   *
   * @param classLoader The class loader.
   * @param fullyQualifiedClassName The fully qualified class name.
   * @return The class object.
   */
  public static Class<?> loadClass(ClassLoader classLoader, String fullyQualifiedClassName) {
    try {
      return classLoader.loadClass(fullyQualifiedClassName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Attempt to load a class.
   *
   * @param classLoader The class loader.
   * @param fullyQualifiedClassName The fully qualified class name.
   * @return The class object, or null if class is not found.
   */
  public static Optional<Class<?>> attemptLoadClass(
      ClassLoader classLoader, String fullyQualifiedClassName) {
    try {
      return Optional.of(classLoader.loadClass(fullyQualifiedClassName));
    } catch (ClassNotFoundException e) {
      return Optional.empty();
    }
  }

  /**
   * Create a new instance of a class
   *
   * @param cl The class object.
   * @param <T> The class type.
   * @return New class instance.
   */
  public static <T> T newInstance(Class<T> cl) {
    try {
      return cl.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reflectively call the constructor of an object.
   *
   * @param clazz Target class.
   * @param classParameters Array of parameter types and values.
   * @param <R> The return type.
   * @return The return value of the method.
   */
  public static <R> R callConstructor(
      Class<? extends R> clazz, ClassParameter<?>... classParameters) {
    perfStatsCollector.incrementCount("ReflectionHelpers.callConstructor-" + clazz.getName());
    try {
      final Class<?>[] classes = ClassParameter.getClasses(classParameters);
      final Object[] values = ClassParameter.getValues(classParameters);

      Constructor<? extends R> constructor = clazz.getDeclaredConstructor(classes);
      constructor.setAccessible(true);
      return constructor.newInstance(values);
    } catch (InstantiationException e) {
      throw new RuntimeException("error instantiating " + clazz.getName(), e);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) e.getTargetException();
      }
      if (e.getTargetException() instanceof Error) {
        throw (Error) e.getTargetException();
      }
      throw new RuntimeException(e.getTargetException());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static <R, E extends Exception> R traverseClassHierarchy(
      Class<?> targetClass, Class<? extends E> exceptionClass, InsideTraversal<R> insideTraversal)
      throws Exception {
    Class<?> hierarchyTraversalClass = targetClass;
    while (true) {
      try {
        return insideTraversal.run(hierarchyTraversalClass);
      } catch (Exception e) {
        if (!exceptionClass.isInstance(e)) {
          throw e;
        }
        hierarchyTraversalClass = hierarchyTraversalClass.getSuperclass();
        if (hierarchyTraversalClass == null) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static Object defaultValueForType(String returnType) {
    return PRIMITIVE_RETURN_VALUES.get(returnType);
  }

  private interface InsideTraversal<R> {
    R run(Class<?> traversalClass) throws Exception;
  }

  /**
   * Typed parameter used with reflective method calls.
   *
   * @param <V> The value of the method parameter.
   */
  public static class ClassParameter<V> {
    public final Class<? extends V> clazz;
    public final V value;

    public ClassParameter(Class<? extends V> clazz, V value) {
      this.clazz = clazz;
      this.value = value;
    }

    public static <V> ClassParameter<V> from(Class<? extends V> clazz, V value) {
      return new ClassParameter<>(clazz, value);
    }

    public static ClassParameter<?>[] fromComponentLists(Class<?>[] classes, Object[] values) {
      ClassParameter<?>[] classParameters = new ClassParameter[classes.length];
      for (int i = 0; i < classes.length; i++) {
        classParameters[i] = ClassParameter.from(classes[i], values[i]);
      }
      return classParameters;
    }

    public static Class<?>[] getClasses(ClassParameter<?>... classParameters) {
      Class<?>[] classes = new Class[classParameters.length];
      for (int i = 0; i < classParameters.length; i++) {
        Class<?> paramClass = classParameters[i].clazz;
        classes[i] = paramClass;
      }
      return classes;
    }

    public static Object[] getValues(ClassParameter<?>... classParameters) {
      Object[] values = new Object[classParameters.length];
      for (int i = 0; i < classParameters.length; i++) {
        Object paramValue = classParameters[i].value;
        values[i] = paramValue;
      }
      return values;
    }
  }
}
