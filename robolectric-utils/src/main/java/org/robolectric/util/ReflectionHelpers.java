package org.robolectric.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionHelpers {

  @SuppressWarnings("unchecked")
  public static <R> R getFieldReflectively(final Object object, final String fieldName) {
    try {
      return traverseClassHierarchy(object.getClass(), NoSuchFieldException.class, new InsideTraversal<R>() {
        @Override
        public R run(Class<?> traversalClass) throws Exception {
          Field field = traversalClass.getDeclaredField(fieldName);
          field.setAccessible(true);
          return (R) field.get(object);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void setFieldReflectively(final Object object, final String fieldName, final Object fieldNewValue) {
    try {
      traverseClassHierarchy(object.getClass(), NoSuchFieldException.class, new InsideTraversal<Void>() {
        @Override
        public Void run(Class<?> traversalClass) throws Exception {
          Field field = traversalClass.getDeclaredField(fieldName);
          field.setAccessible(true);
          field.set(object, fieldNewValue);
          return null;
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <R> R getStaticFieldReflectively(Field field) {
    try {
      makeFieldVeryAccessible(field);
      return (R) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <R> R getStaticFieldReflectively(Class<?> clazz, String fieldName) {
    try {
      return getStaticFieldReflectively(clazz.getDeclaredField(fieldName));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void setStaticFieldReflectively(Field field, Object fieldNewValue) {
    try {
      makeFieldVeryAccessible(field);
      field.set(null, fieldNewValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void setStaticFieldReflectively(Class<?> clazz, String fieldName, Object fieldNewValue) {
    try {
      setStaticFieldReflectively(clazz.getDeclaredField(fieldName), fieldNewValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <R> R callInstanceMethodReflectively(final Object instance, final String methodName, ClassParameter<?>... classParameters) {
    try {
      final Class<?>[] classes = ClassParameter.getClasses(classParameters);
      final Object[] values = ClassParameter.getValues(classParameters);

      return traverseClassHierarchy(instance.getClass(), NoSuchMethodException.class, new InsideTraversal<R>() {
        @Override
        @SuppressWarnings("unchecked")
        public R run(Class<?> traversalClass) throws Exception {
          Method declaredMethod = traversalClass.getDeclaredMethod(methodName, classes);
          declaredMethod.setAccessible(true);
          return (R) declaredMethod.invoke(instance, values);
        }
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

  @SuppressWarnings("unchecked")
  public static <R> R callStaticMethodReflectively(Class<?> containingClass, String methodName, ClassParameter<?>... classParameters) {
    try {
      Class<?>[] classes = ClassParameter.getClasses(classParameters);
      Object[] values = ClassParameter.getValues(classParameters);

      Method method = containingClass.getDeclaredMethod(methodName, classes);
      method.setAccessible(true);
      return (R) method.invoke(null, values);
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

  public static Class<?> loadClassReflectively(ClassLoader classLoader, String fullyQualifiedClassName) {
    try {
      return classLoader.loadClass(fullyQualifiedClassName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static <R> R callConstructorReflectively(Class<? extends R> targetClass, ClassParameter<?>... classParameters) {
    try {
      final Class<?>[] classes = ClassParameter.getClasses(classParameters);
      final Object[] values = ClassParameter.getValues(classParameters);

      Constructor<? extends R> constructor = targetClass.getDeclaredConstructor(classes);
      constructor.setAccessible(true);
      return constructor.newInstance(values);
    } catch (InstantiationException e) {
      throw new RuntimeException("error instantiating " + targetClass.getName(), e);
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

  private static <R, E extends Exception> R traverseClassHierarchy(Class<?> targetClass, Class<? extends E> exceptionClass, InsideTraversal<R> insideTraversal) throws Exception {
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

  private static void makeFieldVeryAccessible(Field field) throws NoSuchFieldException, IllegalAccessException {
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
  }

  private static interface InsideTraversal<R> {
    public R run(Class<?> traversalClass) throws Exception;
  }

  public static class ClassParameter<V> {
    public final Class<? extends V> clazz;
    public final V val;

    private static Map<Class<?>, Class<?>> unboxMap = new HashMap<>();
    
    static {
      unboxMap.put(Boolean.class, boolean.class);
      unboxMap.put(Character.class, char.class);
      unboxMap.put(Byte.class, byte.class);
      unboxMap.put(Short.class, short.class);
      unboxMap.put(Integer.class, int.class);
      unboxMap.put(Long.class, long.class);
      unboxMap.put(Float.class, float.class);
      unboxMap.put(Double.class, double.class);
    }
    
    @SuppressWarnings("unchecked")
    public ClassParameter(V val) {
      Class<?> unboxed = unboxMap.get(val.getClass());
      if (unboxed == null) {
        clazz = (Class<V>)val.getClass();
      } else {
        clazz = (Class<? extends V>)unboxed;
      }
      this.val = val;
    }
    
    public ClassParameter(Class<? extends V> clazz, V val) {
      this.clazz = clazz;
      this.val = val;
    }

    public static <V> ClassParameter<V> from(Class<? extends V> clazz, V val) {
      return new ClassParameter<>(clazz, val);
    }

    /**
     * Convenience factory method for constructing a null parameter for a given type.
     * Equivalent to calling {@link #from(Class, Object) from(clazz, null)}.
     *
     * @param clazz the class of the method's parameter.
     * @return The class parameter with the given class and <tt>null</tt> value.
     */
    public static <V> ClassParameter<V> fromNull(Class<? extends V> clazz) {
      return from(clazz, null);
    }
    
    public static <V> ClassParameter<V> from(V val) {
      return new ClassParameter<>(val);
    }

    public static ClassParameter<?>[] fromSeparateComponentLists(Class<?>[] classes, Object[] values) {
      ClassParameter<?>[] classParameters = new ClassParameter[classes.length];
      for (int i = 0; i < classes.length; i++) {
        classParameters[i] = ClassParameter.from(classes[i], values[i]);
      }
      return classParameters;
    }

    public static ClassParameter<?>[] fromObjectList(Object... values) {
      ClassParameter<?>[] classParameters = new ClassParameter[values.length];
      for (int i = 0; i < values.length; i++) {
        classParameters[i] = ClassParameter.from(values[i]);
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
        Object paramValue = classParameters[i].val;
        values[i] = paramValue;
      }
      return values;
    }
  }

  public static class StringParameter<V> {
    public final String className;
    public final V val;

    public StringParameter(String className, V val) {
      this.className = className;
      this.val = val;
    }

    public static <V> StringParameter<V> from(String className, V val) {
      return new StringParameter<>(className, val);
    }
  }
}
