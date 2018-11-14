package org.robolectric.internal.bytecode;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.Function;
import org.robolectric.util.Logger;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings("NewApi")
public class ShadowWrangler implements ClassHandler {
  public static final Function<Object, Object> DO_NOTHING_HANDLER = new Function<Object, Object>() {
    @Override
    public Object call(Class<?> theClass, Object value, Object[] params) {
      return null;
    }
  };
  public static final Plan DO_NOTHING_PLAN = new Plan() {
    @Override
    public Object run(Object instance, Object[] params) throws Exception {
      return null;
    }

    @Override
    public String describe() {
      return "do nothing";
    }
  };
  public static final Plan CALL_REAL_CODE_PLAN = null;
  public static final Method CALL_REAL_CODE = null;
  public static final MethodHandle DO_NOTHING = constant(Void.class, null).asType(methodType(void.class));
  public static final Method DO_NOTHING_METHOD;

  static {
    try {
      DO_NOTHING_METHOD = ShadowWrangler.class.getDeclaredMethod("doNothing");
      DO_NOTHING_METHOD.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public static final Implementation IMPLEMENTATION_DEFAULTS =
      ReflectionHelpers.defaultsFor(Implementation.class);

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final Class<?>[] NO_ARGS = new Class<?>[0];
  static final Object NO_SHADOW = new Object();
  private static final MethodHandle NO_SHADOW_HANDLE = constant(Object.class, NO_SHADOW);
  private final ShadowMap shadowMap;
  private final Interceptors interceptors;
  private final int apiLevel;
  private final Map<String, Plan> planCache =
      Collections.synchronizedMap(new LinkedHashMap<String, Plan>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Plan> eldest) {
          return size() > 500;
        }
      });

  /** key is instrumented class */
  private final ClassValueMap<ShadowInfo> cachedShadowInfos = new ClassValueMap<ShadowInfo>() {
    @Override protected ShadowInfo computeValue(Class<?> type) {
      return shadowMap.getShadowInfo(type, apiLevel);
    }
  };

  /** key is shadow class */
  private final ClassValueMap<ShadowMetadata> cachedShadowMetadata = new ClassValueMap<ShadowMetadata>() {
    @Nonnull
    @Override
    protected ShadowMetadata computeValue(Class<?> type) {
      return new ShadowMetadata(type);
    }
  };

  public ShadowWrangler(ShadowMap shadowMap, int apiLevel, Interceptors interceptors) {
    this.shadowMap = shadowMap;
    this.apiLevel = apiLevel;
    this.interceptors = interceptors;
  }

  public static Class<?> loadClass(String paramType, ClassLoader classLoader) {
    Class primitiveClass = RoboType.findPrimitiveClass(paramType);
    if (primitiveClass != null) return primitiveClass;

    int arrayLevel = 0;
    while (paramType.endsWith("[]")) {
      arrayLevel++;
      paramType = paramType.substring(0, paramType.length() - 2);
    }

    Class<?> clazz = RoboType.findPrimitiveClass(paramType);
    if (clazz == null) {
      try {
        clazz = classLoader.loadClass(paramType);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    while (arrayLevel-- > 0) {
      clazz = Array.newInstance(clazz, 0).getClass();
    }

    return clazz;
  }

  @SuppressWarnings("ReferenceEquality")
  @Override
  public void classInitializing(Class clazz) {
    try {
      Method method = pickShadowMethod(clazz,
          ShadowConstants.STATIC_INITIALIZER_METHOD_NAME, NO_ARGS);

      // if we got back DO_NOTHING_METHOD that means the shadow is `callThroughByDefault = false`;
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

  @Override
  public Object initializing(Object instance) {
    return createShadowFor(instance);
  }

  @Override
  public Plan methodInvoked(String signature, boolean isStatic, Class<?> theClass) {
    Plan plan;
    if (planCache.containsKey(signature)) {
      plan = planCache.get(signature);
    } else {
      plan = calculatePlan(signature, isStatic, theClass);
      planCache.put(signature, plan);
    }
    return plan;
  }

  @SuppressWarnings("ReferenceEquality")
  private Plan calculatePlan(String signature, boolean isStatic, Class<?> definingClass) {
    return PerfStatsCollector.getInstance().measure("find shadow method", () -> {
      final ClassLoader classLoader = definingClass.getClassLoader();
      final InvocationProfile invocationProfile =
          new InvocationProfile(signature, isStatic, classLoader);
      try {
        Class<?>[] types = invocationProfile.getParamClasses(classLoader);
        Method shadowMethod = pickShadowMethod(definingClass, invocationProfile.methodName, types);
        if (shadowMethod == CALL_REAL_CODE) {
          return CALL_REAL_CODE_PLAN;
        } else if (shadowMethod == DO_NOTHING_METHOD){
          return DO_NOTHING_PLAN;
        } else {
          return new ShadowMethodPlan(shadowMethod);
        }
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings("ReferenceEquality")
  @Override public MethodHandle findShadowMethodHandle(Class<?> definingClass, String name,
      MethodType methodType, boolean isStatic) throws IllegalAccessException {
    return PerfStatsCollector.getInstance().measure("find shadow method handle", () -> {
      MethodType actualType = isStatic ? methodType : methodType.dropParameterTypes(0, 1);
      Class<?>[] paramTypes = actualType.parameterArray();

      Method shadowMethod = pickShadowMethod(definingClass, name, paramTypes);

      if (shadowMethod == CALL_REAL_CODE) {
        return null;
      } else if (shadowMethod == DO_NOTHING_METHOD) {
        return DO_NOTHING;
      }

      shadowMethod.setAccessible(true);
      MethodHandle mh = LOOKUP.unreflect(shadowMethod);

      // Robolectric doesn't actually look for static, this for example happens
      // in MessageQueue.nativeInit() which used to be void non-static in 4.2.
      if (!isStatic && Modifier.isStatic(shadowMethod.getModifiers())) {
        return dropArguments(mh, 0, Object.class);
      } else {
        return mh;
      }
    });
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
   * Searches for an `@Implementation` method on a given shadow class.
   *
   * <p>If the shadow class allows loose signatures, search for them.
   *
   * <p>If the shadow class doesn't have such a method, but does hav a superclass which implements
   * the same class as it, recursively call {@link #findShadowMethod(Class)} with the shadow
   * superclass.
   */
  private Method findShadowMethod(
      Class<?> definingClass,
      String name,
      Class<?>[] types,
      ShadowInfo shadowInfo,
      Class<?> shadowClass) {
    Method method = findShadowMethodDeclaredOnClass(shadowClass, name, types);

    if (method == null && shadowInfo.looseSignatures) {
      Class<?>[] genericTypes = MethodType.genericMethodType(types.length).parameterArray();
      method = findShadowMethodDeclaredOnClass(shadowClass, name, genericTypes);
    }

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
            && shadowSuperclassInfo.supportsSdk(apiLevel)) {

          method =
              findShadowMethod(definingClass, name, types, shadowSuperclassInfo, shadowSuperclass);
        }
      }
    }

    return method;
  }

  private Method findShadowMethodDeclaredOnClass(
      Class<?> shadowClass, String methodName, Class<?>[] paramClasses) {
    try {
      Method method = shadowClass.getDeclaredMethod(methodName, paramClasses);

      // todo: allow per-version overloading
      // if (method == null) {
      //   String methodPrefix = name + "$$";
      //   for (Method candidateMethod : shadowClass.getDeclaredMethods()) {
      //     if (candidateMethod.getName().startsWith(methodPrefix)) {
      //
      //     }
      //   }
      // }

      if (isValidShadowMethod(method)) {
        method.setAccessible(true);
        return method;
      } else {
        return null;
      }

    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private boolean isValidShadowMethod(Method method) {
    int modifiers = method.getModifiers();
    if (!Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers)) {
      return false;
    }

    Implementation implementation = getImplementationAnnotation(method);
    return matchesSdk(implementation);
  }

  private boolean matchesSdk(Implementation implementation) {
    return implementation.minSdk() <= apiLevel && (implementation.maxSdk() == -1 || implementation.maxSdk() >= apiLevel);
  }

  private static Implementation getImplementationAnnotation(Method method) {
    if (method == null) {
      return null;
    }
    Implementation implementation = method.getAnnotation(Implementation.class);
    if (implementation == null) {
      Logger.warn("No @Implementation annotation on " + method);
    }
    return implementation == null
        ? IMPLEMENTATION_DEFAULTS
        : implementation;
  }

  @Override
  public Object intercept(String signature, Object instance, Object[] params, Class theClass) throws Throwable {
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
            && fileName != null && fileName.equals(previousFileName)
            && stackTraceElement.getLineNumber() < 0) {
          continue;
        }

        if (className.equals(ShadowMethodPlan.class.getName())) {
          continue;
        }

        if (methodName.startsWith(ShadowConstants.ROBO_PREFIX)) {
          methodName = methodName.substring(
              methodName.indexOf('$', ShadowConstants.ROBO_PREFIX.length() + 1) + 1);
          stackTraceElement = new StackTraceElement(className, methodName,
              stackTraceElement.getFileName(), stackTraceElement.getLineNumber());
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

  Object createShadowFor(Object instance) {
    Class<?> theClass = instance.getClass();
    Object shadow = createShadowFor(theClass);
    injectRealObjectOn(shadow, instance);
    return shadow;
  }

  private Object createShadowFor(Class<?> theClass) {
    ShadowInfo shadowInfo = getShadowInfo(theClass);
    if (shadowInfo == null) {
      return NO_SHADOW;
    } else {
      try {
        Class<?> shadowClass = loadClass(shadowInfo.shadowClassName, theClass.getClassLoader());
        ShadowMetadata shadowMetadata = getShadowMetadata(shadowClass);
        return shadowMetadata.constructor.newInstance();
      } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
        throw new RuntimeException("Could not instantiate shadow " + shadowInfo.shadowClassName
            + " for " + theClass, e);
      }
    }
  }

  private ShadowMetadata getShadowMetadata(Class<?> shadowClass) {
    return cachedShadowMetadata.get(shadowClass);
  }

  @Override public MethodHandle getShadowCreator(Class<?> theClass) {
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
      mh = foldArguments(mh, LOOKUP.unreflectConstructor(shadowMetadata.constructor));  // (shadow, instance)

      return mh; // (instance)
    } catch (IllegalAccessException | ClassNotFoundException e) {
      throw new RuntimeException("Could not instantiate shadow " + shadowClassName + " for " + theClass, e);
    }
  }

  private void injectRealObjectOn(Object shadow, Object instance) {
    ShadowMetadata shadowMetadata = getShadowMetadata(shadow.getClass());
    for (Field realObjectField : shadowMetadata.realObjectFields) {
      setField(shadow, instance, realObjectField);
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

  private static class ShadowMethodPlan implements Plan {
    private final Method shadowMethod;

    public ShadowMethodPlan(Method shadowMethod) {
      this.shadowMethod = shadowMethod;
    }

    @Override
    public Object run(Object instance, Object[] params) throws Throwable {
      ShadowedObject shadowedObject = (ShadowedObject) instance;
      Object shadow = shadowedObject == null ? null : shadowedObject.$$robo$getData();
      try {
        return shadowMethod.invoke(shadow, params);
      } catch (IllegalArgumentException e) {
        assert shadow != null; // because IllegalArgumentException could only be thrown if shadow is non-null
        Method tryAgainMethod = shadow.getClass()
            .getDeclaredMethod(shadowMethod.getName(), shadowMethod.getParameterTypes());
        if (!tryAgainMethod.equals(shadowMethod)) {
          tryAgainMethod.setAccessible(true);
          try {
            return tryAgainMethod.invoke(shadow, params);
          } catch (InvocationTargetException e1) {
            throw e1.getCause();
          }
        } else {
          throw new IllegalArgumentException("attempted to invoke " + shadowMethod
              + (shadow == null ? "" : " on instance of " + shadow.getClass() + ", but " + shadow.getClass().getSimpleName() + " doesn't extend " + shadowMethod.getDeclaringClass().getSimpleName()));
        }
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
    }

    @Override
    public String describe() {
      return shadowMethod.toString();
    }
  }

  private static class ShadowMetadata {
    final Constructor<?> constructor;
    final List<Field> realObjectFields = new ArrayList<>();

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
        }
        shadowClass = shadowClass.getSuperclass();
      }
    }
  }

  @SuppressWarnings("unused")
  private static void doNothing() {
  }

}
