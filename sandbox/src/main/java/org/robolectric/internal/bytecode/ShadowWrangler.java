package org.robolectric.internal.bytecode;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodType.methodType;
import static org.robolectric.util.reflector.Reflector.reflector;

import com.google.auto.service.AutoService;
import com.google.errorprone.annotations.Keep;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.util.Function;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.Util;

/**
 * ShadowWrangler matches shadowed classes up with corresponding shadows based on a {@link
 * ShadowMap}.
 *
 * <p>ShadowWrangler has no specific knowledge of Android SDK levels or other peculiarities of the
 * affected classes and shadows.
 *
 * <p>To apply additional rules about which shadow classes and methods are considered matches, pass
 * in a {@link ShadowMatcher}.
 *
 * <p>ShadowWrangler is Robolectric's default {@link ClassHandler} implementation. To inject your
 * own, create a subclass and annotate it with {@link AutoService}(ClassHandler).
 */
@SuppressWarnings("NewApi")
@AutoService(ClassHandler.class)
@Priority(Integer.MIN_VALUE)
public class ShadowWrangler implements ClassHandler {
  public static final Function<Object, Object> DO_NOTHING_HANDLER =
      new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          return null;
        }
      };
  public static final Method CALL_REAL_CODE = null;
  public static final MethodHandle DO_NOTHING =
      constant(Void.class, null).asType(methodType(void.class));
  public static final Method DO_NOTHING_METHOD;

  static {
    try {
      DO_NOTHING_METHOD = ShadowWrangler.class.getDeclaredMethod("doNothing");
      DO_NOTHING_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  // Required to support the equivalent of MethodHandles.privateLookupIn in Java 8. It allows
  // calling protected constructors using incokespecial.
  private static final boolean HAS_PRIVATE_LOOKUP_IN = Util.getJavaVersion() >= 9;
  private static final Constructor<MethodHandles.Lookup> JAVA_8_LOOKUP_CTOR;

  static {
    if (!HAS_PRIVATE_LOOKUP_IN) {
      try {
        JAVA_8_LOOKUP_CTOR = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
        JAVA_8_LOOKUP_CTOR.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new AssertionError(e);
      }
    } else {
      JAVA_8_LOOKUP_CTOR = null;
    }
  }

  private static final Class<?>[] NO_ARGS = new Class<?>[0];
  static final Object NO_SHADOW = new Object();
  private static final MethodHandle NO_SHADOW_HANDLE = constant(Object.class, NO_SHADOW);
  private final ShadowMap shadowMap;
  private final Interceptors interceptors;
  private final ShadowMatcher shadowMatcher;
  private final MethodHandle reflectorHandle;

  /** key is instrumented class */
  private final ClassValueMap<ShadowInfo> cachedShadowInfos =
      new ClassValueMap<ShadowInfo>() {
        @Override
        protected ShadowInfo computeValue(Class<?> type) {
          return shadowMap.getShadowInfo(type, shadowMatcher);
        }
      };

  /** key is shadow class */
  private final ClassValueMap<ShadowMetadata> cachedShadowMetadata =
      new ClassValueMap<ShadowMetadata>() {
        @Nonnull
        @Override
        protected ShadowMetadata computeValue(Class<?> type) {
          return new ShadowMetadata(type);
        }
      };

  public ShadowWrangler(
      ShadowMap shadowMap, ShadowMatcher shadowMatcher, Interceptors interceptors) {
    this.shadowMap = shadowMap;
    this.shadowMatcher = shadowMatcher;
    this.interceptors = interceptors;
    try {
      this.reflectorHandle =
          LOOKUP
              .findVirtual(
                  ShadowWrangler.class,
                  "injectReflectorObjectOn",
                  methodType(void.class, Object.class, Object.class))
              .bindTo(this);
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw new RuntimeException(
          "Could not instantiate MethodHandle for ReflectorObject injection.", e);
    }
  }

  @SuppressWarnings("ReferenceEquality")
  @Override
  public void classInitializing(Class clazz) {
    try {
      Method method =
          pickShadowMethod(clazz, ShadowConstants.STATIC_INITIALIZER_METHOD_NAME, NO_ARGS);

      // if we got back DO_NOTHING_METHOD that means the shadow is {@code callThroughByDefault =
      // false};
      // for backwards compatibility we'll still perform static initialization though for now.
      if (method == DO_NOTHING_METHOD) {
        method = null;
      }

      if (method != null) {
        if (!Modifier.isStatic(method.getModifiers())) {
          throw new RuntimeException(
              method.getDeclaringClass().getName() + "." + method.getName() + " is not static");
        }

        method.invoke(null);
      } else {
        RobolectricInternals.performStaticInitialization(clazz);
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings({"ReferenceEquality"})
  @Override
  public MethodHandle findShadowMethodHandle(
      Class<?> definingClass,
      String name,
      MethodType methodType,
      boolean isStatic,
      boolean isNative)
      throws IllegalAccessException {
    return PerfStatsCollector.getInstance()
        .measure(
            "find shadow method handle",
            () -> {
              MethodType actualType = isStatic ? methodType : methodType.dropParameterTypes(0, 1);
              Class<?>[] paramTypes = actualType.parameterArray();

              Method shadowMethod = pickShadowMethod(definingClass, name, paramTypes);

              if (shadowMethod == CALL_REAL_CODE) {
                ShadowInfo shadowInfo = getExactShadowInfo(definingClass);
                if (isNative && shadowInfo != null && shadowInfo.callNativeMethodsByDefault) {
                  try {
                    Method method =
                        definingClass.getDeclaredMethod(
                            ShadowConstants.ROBO_PREFIX + name + "$nativeBinding", paramTypes);
                    method.setAccessible(true);
                    return LOOKUP.unreflect(method);
                  } catch (NoSuchMethodException e) {
                    throw new LinkageError("Missing native binding method", e);
                  }
                }
                return null;
              } else if (shadowMethod == DO_NOTHING_METHOD) {
                return DO_NOTHING;
              }

              shadowMethod.setAccessible(true);

              MethodHandle mh;
              if (name.equals(ShadowConstants.CONSTRUCTOR_METHOD_NAME)) {
                if (Modifier.isStatic(shadowMethod.getModifiers())) {
                  throw new UnsupportedOperationException(
                      "static __constructor__ shadow methods are not supported");
                }
                // Use invokespecial to call constructor shadow methods. If invokevirtual is used,
                // the wrong constructor may be called in situations where constructors with
                // identical signatures are shadowed in object hierarchies.
                mh =
                    privateLookupFor(shadowMethod.getDeclaringClass())
                        .unreflectSpecial(shadowMethod, shadowMethod.getDeclaringClass());
              } else {
                mh = LOOKUP.unreflect(shadowMethod);
              }

              // Robolectric doesn't actually look for static, this for example happens
              // in MessageQueue.nativeInit() which used to be void non-static in 4.2.
              if (!isStatic && Modifier.isStatic(shadowMethod.getModifiers())) {
                return dropArguments(mh, 0, Object.class);
              } else {
                return mh;
              }
            });
  }

  private MethodHandles.Lookup privateLookupFor(Class<?> lookupClass)
      throws IllegalAccessException {
    if (HAS_PRIVATE_LOOKUP_IN) {
      return MethodHandles.privateLookupIn(lookupClass, LOOKUP);
    }
    try {
      return JAVA_8_LOOKUP_CTOR.newInstance(lookupClass);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
  }

  protected Method pickShadowMethod(Class<?> definingClass, String name, Class<?>[] paramTypes) {
    ShadowInfo shadowInfo = getExactShadowInfo(definingClass);
    if (shadowInfo == null) {
      return CALL_REAL_CODE;
    } else {
      ClassLoader classLoader = definingClass.getClassLoader();
      Class<?> shadowClass;
      try {
        shadowClass = Class.forName(shadowInfo.shadowClassName, false, classLoader);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(e);
      }

      Method method = findShadowMethod(definingClass, name, paramTypes, shadowInfo, shadowClass);
      if (method == null) {
        return shadowInfo.callThroughByDefault ? CALL_REAL_CODE : DO_NOTHING_METHOD;
      } else {
        return method;
      }
    }
  }

  /**
   * Searches for an {@code @Implementation} method on a given shadow class.
   *
   * <p>If the shadow class allows loose signatures, search for them.
   *
   * <p>If the shadow class doesn't have such a method, but does have a superclass which implements
   * the same class as it, call ourself recursively with the shadow superclass.
   */
  private Method findShadowMethod(
      Class<?> definingClass,
      String name,
      Class<?>[] types,
      ShadowInfo shadowInfo,
      Class<?> shadowClass) {
    Method method =
        findShadowMethodDeclaredOnClass(shadowClass, name, types, shadowInfo.looseSignatures);

    if (method != null) {
      return method;
    } else {
      // if the shadow's superclass shadows the same class as this shadow, then recurse.
      // Buffalo buffalo buffalo buffalo buffalo buffalo buffalo.
      Class<?> shadowSuperclass = shadowClass.getSuperclass();
      if (shadowSuperclass != null && !shadowSuperclass.equals(Object.class)) {
        ShadowInfo shadowSuperclassInfo = ShadowMap.obtainShadowInfo(shadowSuperclass, true);
        if (shadowSuperclassInfo != null
            && shadowSuperclassInfo.isShadowOf(definingClass)
            && shadowMatcher.matches(shadowSuperclassInfo)) {

          method =
              findShadowMethod(definingClass, name, types, shadowSuperclassInfo, shadowSuperclass);
        }
      }
    }

    return method;
  }

  private Method findShadowMethodDeclaredOnClass(
      Class<?> shadowClass, String methodName, Class<?>[] paramClasses, boolean looseSignatures) {
    Method foundMethod = null;
    // Try to find shadow method with exact method name and looseSignature.
    Method[] methods = shadowClass.getDeclaredMethods();
    for (Method method : methods) {
      if (!method.getName().equals(methodName)
          || method.getParameterCount() != paramClasses.length) {
        continue;
      }

      if (!Modifier.isPublic(method.getModifiers())
          && !Modifier.isProtected(method.getModifiers())) {
        continue;
      }

      if (Arrays.equals(method.getParameterTypes(), paramClasses)
          && shadowMatcher.matches(method)) {
        // Found an exact match, we can exit early.
        foundMethod = method;
        break;
      }

      if (looseSignatures) {
        boolean allParameterTypesAreObject = true;
        for (Class<?> paramClass : method.getParameterTypes()) {
          if (!paramClass.equals(Object.class)) {
            allParameterTypesAreObject = false;
            break;
          }
        }
        if (allParameterTypesAreObject && shadowMatcher.matches(method)) {
          // Found a looseSignatures match, but continue looking for an exact match.
          foundMethod = method;
        }
      } else {
        // Or maybe support @ClassName.
        if (parameterClassNameMatch(method, paramClasses) && shadowMatcher.matches(method)) {
          // Found a @ClassName match, but continue looking for an exact match.
          foundMethod = method;
        }
      }
    }

    if (foundMethod == null) {
      // Try to find shadow method with Implementation#methodName's mapping name
      for (Method method : methods) {
        Implementation implementation = method.getAnnotation(Implementation.class);
        if (implementation == null) {
          continue;
        }
        String mappedMethodName = implementation.methodName().trim();
        if (mappedMethodName.isEmpty() || !mappedMethodName.equals(methodName)) {
          continue;
        }
        if (Arrays.equals(method.getParameterTypes(), paramClasses)
            || parameterClassNameMatch(method, paramClasses)) {
          foundMethod = method;
          break;
        }
      }
    }

    if (foundMethod != null) {
      foundMethod.setAccessible(true);
      return foundMethod;
    } else {
      return null;
    }
  }

  /**
   * Check whether the parameters (which could be @ClassName annotated) of the {@code method}
   * matches {@code paramClasses}.
   */
  private boolean parameterClassNameMatch(Method method, Class<?>[] paramClasses) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length != paramClasses.length) {
      return false;
    }

    Annotation[][] annotations = method.getParameterAnnotations();
    for (int i = 0; i < paramClasses.length; ++i) {
      if (parameterTypes[i].equals(paramClasses[i])) {
        continue;
      }
      if (!parameterTypes[i].equals(Object.class)) {
        return false; // @ClassName only applicable to parameter of Object type
      }
      boolean matches = false;
      for (Annotation annotation : annotations[i]) {
        if (annotation instanceof ClassName) {
          matches = ((ClassName) annotation).value().equals(paramClasses[i].getName());
          break;
        }
      }
      if (!matches) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Object intercept(String signature, Object instance, Object[] params, Class theClass)
      throws Throwable {
    final MethodSignature methodSignature = MethodSignature.parse(signature);
    return interceptors.getInterceptionHandler(methodSignature).call(theClass, instance, params);
  }

  @Override
  public <T extends Throwable> T stripStackTrace(T throwable) {
    StackTraceElement[] elements = throwable.getStackTrace();
    if (elements != null) {
      List<StackTraceElement> stackTrace = new ArrayList<>();

      String previousClassName = null;
      String previousMethodName = null;
      String previousFileName = null;

      for (StackTraceElement stackTraceElement : elements) {
        String methodName = stackTraceElement.getMethodName();
        String className = stackTraceElement.getClassName();
        String fileName = stackTraceElement.getFileName();

        if (methodName.equals(previousMethodName)
            && className.equals(previousClassName)
            && fileName != null
            && fileName.equals(previousFileName)
            && stackTraceElement.getLineNumber() < 0) {
          continue;
        }

        if (methodName.startsWith(ShadowConstants.ROBO_PREFIX)) {
          methodName =
              methodName.substring(
                  methodName.indexOf('$', ShadowConstants.ROBO_PREFIX.length() + 1) + 1);
          stackTraceElement =
              new StackTraceElement(
                  className,
                  methodName,
                  stackTraceElement.getFileName(),
                  stackTraceElement.getLineNumber());
        }

        if (className.startsWith("sun.reflect.") || className.startsWith("java.lang.reflect.")) {
          continue;
        }

        stackTrace.add(stackTraceElement);

        previousClassName = className;
        previousMethodName = methodName;
        previousFileName = fileName;
      }
      throwable.setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
    }
    return throwable;
  }

  private ShadowMetadata getShadowMetadata(Class<?> shadowClass) {
    return cachedShadowMetadata.get(shadowClass);
  }

  @Override
  public MethodHandle getShadowCreator(Class<?> theClass) {
    ShadowInfo shadowInfo = getShadowInfo(theClass);
    if (shadowInfo == null) return dropArguments(NO_SHADOW_HANDLE, 0, theClass);
    String shadowClassName = shadowInfo.shadowClassName;

    try {
      Class<?> shadowClass = Class.forName(shadowClassName, false, theClass.getClassLoader());
      ShadowMetadata shadowMetadata = getShadowMetadata(shadowClass);

      MethodHandle mh = identity(shadowClass); // (instance)
      mh = dropArguments(mh, 1, theClass); // (instance)
      for (Field field : shadowMetadata.realObjectFields) {
        MethodHandle setter = LOOKUP.unreflectSetter(field);
        MethodType setterType = mh.type().changeReturnType(void.class);
        mh = foldArguments(mh, setter.asType(setterType));
      }

      MethodHandle classHandle =
          reflectorHandle.asType(
              reflectorHandle
                  .type()
                  .changeParameterType(0, shadowClass)
                  .changeParameterType(1, theClass));
      mh = foldArguments(mh, classHandle);

      mh =
          foldArguments(
              mh, LOOKUP.unreflectConstructor(shadowMetadata.constructor)); // (shadow, instance)

      return mh; // (instance)
    } catch (IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException(
          "Could not instantiate shadow " + shadowClassName + " for " + theClass, e);
    }
  }

  @Keep // This method is looked up using reflection.
  private void injectReflectorObjectOn(Object shadow, Object instance) {
    ShadowMetadata shadowMetadata = getShadowMetadata(shadow.getClass());
    for (Field reflectorObjectField : shadowMetadata.reflectorObjectFields) {
      setField(shadow, reflector(reflectorObjectField.getType(), instance), reflectorObjectField);
    }
  }

  private ShadowInfo getShadowInfo(Class<?> clazz) {
    ShadowInfo shadowInfo = null;
    for (; shadowInfo == null && clazz != null; clazz = clazz.getSuperclass()) {
      shadowInfo = getExactShadowInfo(clazz);
    }
    return shadowInfo;
  }

  private ShadowInfo getExactShadowInfo(Class<?> clazz) {
    return cachedShadowInfos.get(clazz);
  }

  private static void setField(Object target, Object value, Field realObjectField) {
    try {
      realObjectField.set(target, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static class ShadowMetadata {
    final Constructor<?> constructor;
    final List<Field> realObjectFields = new ArrayList<>();
    final List<Field> reflectorObjectFields = new ArrayList<>();

    public ShadowMetadata(Class<?> shadowClass) {
      try {
        this.constructor = shadowClass.getConstructor();
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Missing public empty constructor on " + shadowClass, e);
      }

      while (shadowClass != null) {
        for (Field field : shadowClass.getDeclaredFields()) {
          if (field.isAnnotationPresent(RealObject.class)) {
            if (Modifier.isStatic(field.getModifiers())) {
              String message = "@RealObject must be on a non-static field, " + shadowClass;
              System.err.println(message);
              throw new IllegalArgumentException(message);
            }
            field.setAccessible(true);
            realObjectFields.add(field);
          }
          if (field.isAnnotationPresent(ReflectorObject.class)) {
            if (Modifier.isStatic(field.getModifiers())) {
              String message = "@ReflectorObject must be on a non-static field, " + shadowClass;
              System.err.println(message);
              throw new IllegalArgumentException(message);
            }
            field.setAccessible(true);
            reflectorObjectFields.add(field);
          }
        }
        shadowClass = shadowClass.getSuperclass();
      }
    }
  }

  @SuppressWarnings("unused")
  private static void doNothing() {}
}
