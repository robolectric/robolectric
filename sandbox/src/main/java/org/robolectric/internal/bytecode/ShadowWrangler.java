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
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

public class ShadowWrangler implements ClassHandler {
  public static final Function<Object, Object> DO_NOTHING_HANDLER = new Function<Object, Object>() {
    @Override
    public Object call(Class<?> theClass, Object value, Object[] params) {
      return null;
    }
  };
  public static final Plan DO_NOTHING_PLAN = new Plan() {
    @Override
    public Object run(Object instance, Object roboData, Object[] params) throws Exception {
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

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final boolean STRIP_SHADOW_STACK_TRACES = true;
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
  private final ClassValue<ShadowInfo> cachedShadowInfos = new ClassValue<ShadowInfo>() {
    @Override protected ShadowInfo computeValue(Class<?> type) {
      return shadowMap.getShadowInfo(type, apiLevel);
    }
  };

  /** key is shadow class */
  private final ClassValue<ShadowMetadata> cachedShadowMetadata = new ClassValue<ShadowMetadata>() {
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

  @Override
  public void classInitializing(Class clazz) {
    Class<?> shadowClass = findDirectShadowClass(clazz);
    if (shadowClass != null) {
      try {
        Method method = shadowClass.getDeclaredMethod(ShadowConstants.STATIC_INITIALIZER_METHOD_NAME);
        if (!Modifier.isStatic(method.getModifiers())) {
          throw new RuntimeException(shadowClass.getName() + "." + method.getName() + " is not static");
        }
        method.setAccessible(true);
        method.invoke(null);
      } catch (NoSuchMethodException e) {
        RobolectricInternals.performStaticInitialization(clazz);
      } catch (InvocationTargetException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else {
      RobolectricInternals.performStaticInitialization(clazz);
    }
  }

  @Override
  public Object initializing(Object instance) {
    return createShadowFor(instance);
  }

  @Override
  public Plan methodInvoked(String signature, boolean isStatic, Class<?> theClass) {
    if (planCache.containsKey(signature)) {
      return planCache.get(signature);
    }
    Plan plan = calculatePlan(signature, isStatic, theClass);
    planCache.put(signature, plan);
    return plan;
  }

  private Plan calculatePlan(String signature, boolean isStatic, Class<?> definingClass) {
    final ClassLoader classLoader = definingClass.getClassLoader();
    final InvocationProfile invocationProfile = new InvocationProfile(signature, isStatic, classLoader);
    try {
      Class<?>[] types = invocationProfile.getParamClasses(classLoader);
      Method shadowMethod = pickShadowMethod(definingClass, invocationProfile.methodName, types);
      if (shadowMethod == CALL_REAL_CODE) {
        return CALL_REAL_CODE_PLAN;
      } else {
        return new ShadowMethodPlan(shadowMethod);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public MethodHandle findShadowMethodHandle(Class<?> definingClass, String name,
      MethodType methodType, boolean isStatic) throws IllegalAccessException {
    MethodType actualType = isStatic ? methodType : methodType.dropParameterTypes(0, 1);
    Class<?>[] paramTypes = actualType.parameterArray();

    Method shadowMethod = pickShadowMethod(definingClass, name, paramTypes);

    if (shadowMethod == CALL_REAL_CODE) {
      return null;
    } else if (shadowMethod == DO_NOTHING_METHOD) {
      return DO_NOTHING;
    }

    MethodHandle mh = LOOKUP.unreflect(shadowMethod);

    // Robolectric doesn't actually look for static, this for example happens
    // in MessageQueue.nativeInit() which used to be void non-static in 4.2.
    if (!isStatic && Modifier.isStatic(shadowMethod.getModifiers())) {
      return dropArguments(mh, 0, Object.class);
    } else {
      return mh;
    }
  }

  private Method pickShadowMethod(Class<?> definingClass, String name, Class<?>[] paramTypes) {
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

      Method method = findShadowMethod(shadowInfo, shadowClass, name, paramTypes);
      if (method == null) {
        return shadowInfo.callThroughByDefault ? CALL_REAL_CODE : DO_NOTHING_METHOD;
      }

      Class<?> declaredShadowedClass = getShadowedClass(method);
      if (declaredShadowedClass.equals(Object.class)) {
        // e.g. for equals(), hashCode(), toString()
        return CALL_REAL_CODE;
      }

      boolean shadowClassMismatch = !declaredShadowedClass.equals(definingClass);
      if (shadowClassMismatch && !shadowInfo.inheritImplementationMethods) {
        return CALL_REAL_CODE;
      } else {
        return method;
      }
    }
  }

  private Method findShadowMethod(ShadowInfo config, Class<?> shadowClass, String name, Class<?>[] types) {
    Method method = findShadowMethodInternal(shadowClass, name, types);

    if (method == null && config.looseSignatures) {
      Class<?>[] genericTypes = MethodType.genericMethodType(types.length).parameterArray();
      method = findShadowMethodInternal(shadowClass, name, genericTypes);
    }

    Class<?> superclass;
    if (method == null && config.inheritImplementationMethods && (superclass = shadowClass.getSuperclass()) != null) {
      return findShadowMethod(config, superclass, name, types);
    }

    return method;
  }

  private Method findShadowMethodInternal(Class<?> shadowClass, String methodName, Class<?>[] paramClasses) {
    try {
      Method method = shadowClass.getDeclaredMethod(methodName, paramClasses);
      method.setAccessible(true);
      Implementation implementation = getImplementationAnnotation(method);
      return matchesSdk(implementation) ? method : null;

      // todo: allow per-version overloading
//      if (method == null) {
//        String methodPrefix = name + "$$";
//        for (Method candidateMethod : shadowClass.getMethods()) {
//          if (candidateMethod.getName().startsWith(methodPrefix)) {
//
//          }
//        }
//      }

    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private boolean matchesSdk(Implementation implementation) {
    return implementation.minSdk() <= apiLevel && (implementation.maxSdk() == -1 || implementation.maxSdk() >= apiLevel);
  }

  private Class<?> getShadowedClass(Method shadowMethod) {
    Class<?> shadowingClass = shadowMethod.getDeclaringClass();
    if (shadowingClass.equals(Object.class)) {
      return Object.class;
    }

    Implements implementsAnnotation = shadowingClass.getAnnotation(Implements.class);
    if (implementsAnnotation == null) {
      throw new RuntimeException(shadowingClass + " has no @" + Implements.class.getSimpleName() + " annotation");
    }
    String shadowedClassName = implementsAnnotation.className();
    if (shadowedClassName.isEmpty()) {
      return implementsAnnotation.value();
    } else {
      try {
        return shadowingClass.getClassLoader().loadClass(shadowedClassName);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static Implementation getImplementationAnnotation(Method method) {
    if (method == null) {
      return null;
    }
    Implementation implementation = method.getAnnotation(Implementation.class);
    return implementation == null
        ? ReflectionHelpers.defaultsFor(Implementation.class)
        : implementation;
  }

  @Override
  public Object intercept(String signature, Object instance, Object[] params, Class theClass) throws Throwable {
    final MethodSignature methodSignature = MethodSignature.parse(signature);
    return interceptors.getInterceptionHandler(methodSignature).call(theClass, instance, params);
  }

  @Override
  public <T extends Throwable> T stripStackTrace(T throwable) {
    if (STRIP_SHADOW_STACK_TRACES) {
      List<StackTraceElement> stackTrace = new ArrayList<>();

      String previousClassName = null;
      String previousMethodName = null;
      String previousFileName = null;

      for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
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
          methodName = methodName.substring(ShadowConstants.ROBO_PREFIX.length());
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
        throw new RuntimeException("Could not instantiate shadow " + shadowInfo.shadowClassName + " for " + theClass, e);
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

  private Class<?> findDirectShadowClass(Class<?> originalClass) {
    ShadowInfo shadowInfo = getExactShadowInfo(originalClass);
    if (shadowInfo == null) {
      return null;
    }
    return loadClass(shadowInfo.shadowClassName, originalClass.getClassLoader());
  }

  private ShadowInfo getShadowInfo(Class<?> clazz) {
    ShadowInfo shadowInfo = null;
    for (; shadowInfo == null && clazz != null; clazz = clazz.getSuperclass()) {
      shadowInfo = getExactShadowInfo(clazz);
    }
    return shadowInfo;
  }

  private ShadowInfo getExactShadowInfo(Class clazz) {
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
    public Object run(Object instance, Object roboData, Object[] params) throws Throwable {
      //noinspection UnnecessaryLocalVariable
      Object shadow = roboData;
      try {
        return shadowMethod.invoke(shadow, params);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("attempted to invoke " + shadowMethod
            + (shadow == null ? "" : " on instance of " + shadow.getClass() + ", but " + shadow.getClass().getSimpleName() + " doesn't extend " + shadowMethod.getDeclaringClass().getSimpleName()));
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

  private static void doNothing() {
  }
}
