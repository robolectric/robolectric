package org.robolectric.internal.bytecode;

import android.content.Context;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Function;
import org.robolectric.internal.ShadowConstants;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodType.methodType;

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
  };
  public static final Plan CALL_REAL_CODE_PLAN = null;
  public static final MethodHandle CALL_REAL_CODE = null;
  public static final MethodHandle DO_NOTHING = constant(Void.class, null).asType(methodType(void.class));
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final boolean STRIP_SHADOW_STACK_TRACES = true;
  private static final ShadowConfig NO_SHADOW_CONFIG = new ShadowConfig(Object.class.getName(), true, false, false);
  private static final Object NO_SHADOW = new Object();
  private static final MethodHandle NO_SHADOW_HANDLE = constant(Object.class, NO_SHADOW);
  private final ShadowMap shadowMap;
  private final Map<Class, MetaShadow> metaShadowMap = new HashMap<>();
  private final Map<String, Plan> planCache = new LinkedHashMap<String, Plan>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Plan> eldest) {
      return size() > 500;
    }
  };
  private final Map<Class, ShadowConfig> shadowConfigCache = new ConcurrentHashMap<>();
  private final ClassValue<ShadowConfig> shadowConfigs = new ClassValue<ShadowConfig>() {
    @Override protected ShadowConfig computeValue(Class<?> type) {
      return shadowMap.get(type);
    }
  };
  public static final HashMap<String, Object> PRIMITIVE_RETURN_VALUES = new HashMap<>();

  static {
    PRIMITIVE_RETURN_VALUES.put("boolean", Boolean.FALSE);
    PRIMITIVE_RETURN_VALUES.put("int", 0);
    PRIMITIVE_RETURN_VALUES.put("long", (long) 0);
    PRIMITIVE_RETURN_VALUES.put("float", (float) 0);
    PRIMITIVE_RETURN_VALUES.put("double", (double) 0);
    PRIMITIVE_RETURN_VALUES.put("short", (short) 0);
    PRIMITIVE_RETURN_VALUES.put("byte", (byte) 0);
  }

  public ShadowWrangler(ShadowMap shadowMap) {
    this.shadowMap = shadowMap;
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
        Method method = shadowClass.getMethod(ShadowConstants.STATIC_INITIALIZER_METHOD_NAME);
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
    if (planCache.containsKey(signature)) return planCache.get(signature);
    Plan plan = calculatePlan(signature, isStatic, theClass);
    planCache.put(signature, plan);
    return plan;
  }

  @Override public MethodHandle findShadowMethod(Class<?> caller, String name, MethodType type,
      boolean isStatic) throws IllegalAccessException {
    ShadowConfig shadowConfig = shadowConfigs.get(caller);
    if (shadowConfig == null) return CALL_REAL_CODE;

    ClassLoader classLoader = caller.getClassLoader();
    MethodType actualType = isStatic ? type : type.dropParameterTypes(0, 1);
    Method method = findShadowMethod(classLoader, shadowConfig, name, actualType.parameterArray());
    if (method == null) {
      return shadowConfig.callThroughByDefault ? CALL_REAL_CODE : DO_NOTHING;
    }

    Class<?> declaredShadowedClass = getShadowedClass(method);
    if (declaredShadowedClass.equals(Object.class)) {
      // e.g. for equals(), hashCode(), toString()
      return CALL_REAL_CODE;
    }

    boolean shadowClassMismatch = !declaredShadowedClass.equals(caller);
    if (shadowClassMismatch && !shadowConfig.inheritImplementationMethods) {
      return CALL_REAL_CODE;
    } else {
      MethodHandle mh = LOOKUP.unreflect(method);

      // Robolectric doesn't actually look for static, this for example happens
      // in MessageQueue.nativeInit() which used to be void non-static in 4.2.
      if (!isStatic && Modifier.isStatic(method.getModifiers())) {
        return dropArguments(mh, 0, Object.class);
      } else {
        return mh;
      }
    }
  }

  private Plan calculatePlan(String signature, boolean isStatic, Class<?> theClass) {
    final InvocationProfile invocationProfile = new InvocationProfile(signature, isStatic, theClass.getClassLoader());
    ShadowConfig shadowConfig = getShadowConfig(theClass);

    if (shadowConfig == null) {
      return CALL_REAL_CODE_PLAN;
    } else {
      try {
        final ClassLoader classLoader = theClass.getClassLoader();
        Class<?>[] types = invocationProfile.getParamClasses(classLoader);
        Method shadowMethod = findShadowMethod(classLoader, shadowConfig, invocationProfile.methodName, types);
        if (shadowMethod == null) {
          return shadowConfig.callThroughByDefault ? CALL_REAL_CODE_PLAN : strict(invocationProfile) ? CALL_REAL_CODE_PLAN : DO_NOTHING_PLAN;
        }

        final Class<?> declaredShadowedClass = getShadowedClass(shadowMethod);

        if (declaredShadowedClass.equals(Object.class)) {
          // e.g. for equals(), hashCode(), toString()
          return CALL_REAL_CODE_PLAN;
        }

        boolean shadowClassMismatch = !declaredShadowedClass.equals(invocationProfile.clazz);
        if (shadowClassMismatch && (!shadowConfig.inheritImplementationMethods || strict(invocationProfile))) {
          return CALL_REAL_CODE_PLAN;
        } else {
          return new ShadowMethodPlan(shadowMethod);
        }
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Method findShadowMethod(ClassLoader classLoader, ShadowConfig config, String name, Class<?>[] types) {
    try {
      Class<?> shadowClass = Class.forName(config.shadowClassName, false, classLoader);
      Method method = getMethod(shadowClass, name, types);
      if (method == null && config.looseSignatures) {
        Class<?>[] genericTypes = MethodType.genericMethodType(types.length).parameterArray();
        method = getMethod(shadowClass, name, genericTypes);
      }

      return method;
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private ShadowConfig getShadowConfig(Class clazz) {
    ShadowConfig shadowConfig = shadowConfigCache.get(clazz);
    if (shadowConfig == null) {
      shadowConfig = shadowMap.get(clazz);
      shadowConfigCache.put(clazz, shadowConfig == null ? NO_SHADOW_CONFIG : shadowConfig);
      return shadowConfig;
    } else {
      return (shadowConfig == NO_SHADOW_CONFIG) ? null : shadowConfig;
    }
  }

  private boolean isAndroidSupport(InvocationProfile invocationProfile) {
    return invocationProfile.clazz.getName().startsWith("android.support");
  }

  private boolean strict(InvocationProfile invocationProfile) {
    return isAndroidSupport(invocationProfile) || invocationProfile.isDeclaredOnObject();
  }

  private Method getMethod(Class<?> shadowClass, String methodName, Class<?>[] paramClasses) throws ClassNotFoundException {
    try {
      return shadowClass.getMethod(methodName, paramClasses);
    } catch (NoSuchMethodException e) {
      return null;
    }
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

  @Override
  public Object intercept(String signature, Object instance, Object[] params, Class theClass) throws Throwable {
    final MethodSignature methodSignature = MethodSignature.parse(signature);
    return getInterceptionHandler(methodSignature).call(theClass, instance, params);
  }

  @SuppressWarnings("UnnecessaryBoxing")
  public Function<Object, Object> getInterceptionHandler(final MethodSignature methodSignature) {
    // TODO: move these somewhere else!
    if (methodSignature.matches(LinkedHashMap.class.getName(), "eldest")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          LinkedHashMap map = (LinkedHashMap) value;
          return map.isEmpty() ? null : map.entrySet().iterator().next();
        }
      };
    } else if (methodSignature.matches("com.android.internal.policy.PolicyManager", "makeNewWindow")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          ClassLoader cl = theClass.getClassLoader();
          Class<?> shadowWindowClass;

          try {
            shadowWindowClass = cl.loadClass("org.robolectric.shadows.ShadowWindow");
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }

          Class<?> activityClass;

          try {
            activityClass = cl.loadClass(Context.class.getName());
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }

          Object context = params[0];
          return ReflectionHelpers.callStaticMethod(shadowWindowClass, "create", ClassParameter.from(activityClass, context));
        }
      };
    } else if (methodSignature.matches("java.lang.System", "nanoTime") || methodSignature.matches("java.lang.System", "currentTimeMillis")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          ClassLoader cl = theClass.getClassLoader();
          Class<?> shadowSystemClockClass;
          try {
            shadowSystemClockClass = cl.loadClass("org.robolectric.shadows.ShadowSystemClock");
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }

          return ReflectionHelpers.callStaticMethod(shadowSystemClockClass, methodSignature.methodName);
        }
      };
    } else if (methodSignature.matches("java.lang.System", "arraycopy")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          //noinspection SuspiciousSystemArraycopy
          System.arraycopy(params[0], (Integer) params[1], params[2], (Integer) params[3], (Integer) params[4]);
          return null;
        }
      };
    } else if (methodSignature.matches("java.util.Locale", "adjustLanguageCode")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          return params[0];
        }
      };
    } else if (methodSignature.matches("java.lang.System", "logE")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          String message = "System.logE: ";
          for (Object param : params) {
            message += param.toString();
          }
          System.err.println(message);
          return null;
        }
      };
    }

    return new Function<Object, Object>() {
      @Override
      public Object call(Class<?> theClass, Object value, Object[] params) {
        return PRIMITIVE_RETURN_VALUES.get(methodSignature.returnType);
      }
    };
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

  public Object createShadowFor(Object instance) {
    String shadowClassName = getShadowClassName(instance.getClass());

    if (shadowClassName == null) return NO_SHADOW;

    try {
      Class<?> shadowClass = loadClass(shadowClassName, instance.getClass().getClassLoader());
      Object shadow = shadowClass.newInstance();
      injectRealObjectOn(shadow, shadowClass, instance);

      return shadow;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Could not instantiate shadow, missing public empty constructor.", e);
    }
  }

  @Override public MethodHandle getShadowCreator(Class<?> caller) {
    String shadowClassName = getShadowClassNameInvoke(caller);

    if (shadowClassName == null) return dropArguments(NO_SHADOW_HANDLE, 0, caller);

    try {
      Class<?> shadowClass = Class.forName(shadowClassName, false, caller.getClassLoader());
      MethodHandle constructor = LOOKUP.findConstructor(shadowClass, methodType(void.class));
      MetaShadow metaShadow = getMetaShadow(shadowClass);

      MethodHandle mh = identity(shadowClass); // (instance)
      mh = dropArguments(mh, 1, caller); // (instance)
      for (Field field : metaShadow.realObjectFields) {
        MethodHandle setter = LOOKUP.unreflectSetter(field);
        MethodType setterType = mh.type().changeReturnType(void.class);
        mh = foldArguments(mh, setter.asType(setterType));
      }
      mh = foldArguments(mh, constructor);  // (shadow, instance)

      return mh; // (instance)
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException("Could not instantiate shadow, missing public empty constructor.", e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not instantiate shadow", e);
    }
  }

  private String getShadowClassNameInvoke(Class<?> cl) {
    Class clazz = cl;
    ShadowConfig shadowConfig = null;
    while (shadowConfig == null && clazz != null) {
      shadowConfig = shadowConfigs.get(clazz);
      clazz = clazz.getSuperclass();
    }
    return shadowConfig == null ? null : shadowConfig.shadowClassName;
  }

  private String getShadowClassName(Class<?> cl) {
    Class clazz = cl;
    ShadowConfig shadowConfig = null;
    while (shadowConfig == null && clazz != null) {
      shadowConfig = getShadowConfig(clazz);
      clazz = clazz.getSuperclass();
    }
    return shadowConfig == null ? null : shadowConfig.shadowClassName;
  }

  private void injectRealObjectOn(Object shadow, Class<?> shadowClass, Object instance) {
    MetaShadow metaShadow = getMetaShadow(shadowClass);
    for (Field realObjectField : metaShadow.realObjectFields) {
      writeField(shadow, instance, realObjectField);
    }
  }

  private MetaShadow getMetaShadow(Class<?> shadowClass) {
    synchronized (metaShadowMap) {
      MetaShadow metaShadow = metaShadowMap.get(shadowClass);
      if (metaShadow == null) {
        metaShadow = new MetaShadow(shadowClass);
        metaShadowMap.put(shadowClass, metaShadow);
      }
      return metaShadow;
    }
  }

  private Class<?> findDirectShadowClass(Class<?> originalClass) {
    ShadowConfig shadowConfig = getShadowConfig(originalClass);
    if (shadowConfig == null) {
      return null;
    }
    return loadClass(shadowConfig.shadowClassName, originalClass.getClassLoader());
  }

  private static void writeField(Object target, Object value, Field realObjectField) {
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
  }

  private class MetaShadow {
    final List<Field> realObjectFields = new ArrayList<>();

    public MetaShadow(Class<?> shadowClass) {
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
}
