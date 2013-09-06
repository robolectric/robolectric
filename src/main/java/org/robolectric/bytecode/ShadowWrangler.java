package org.robolectric.bytecode;

import android.content.Context;
import org.robolectric.SdkConfig;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowWindow;
import org.robolectric.util.Function;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.fest.reflect.core.Reflection.method;
import static org.fest.reflect.core.Reflection.type;

public class ShadowWrangler implements ClassHandler {
  public static final Function<Object, Object> DO_NOTHING_HANDLER = new Function<Object, Object>() {
    @Override
    public Object call(Class<?> theClass, Object value, Object[] params) {
      return null;
    }
  };
  public static final Plan DO_NOTHING_PLAN = new Plan() {
    @Override public Object run(Object instance, Object roboData, Object[] params) throws Exception {
      return null;
    }
  };
  public static final Plan CALL_REAL_CODE_PLAN = null;
  private static final boolean STRIP_SHADOW_STACK_TRACES = true;
  private static final ShadowConfig NO_SHADOW_CONFIG = new ShadowConfig(Object.class.getName(), true, false, false);
  public boolean debug = false;

  private final ShadowMap shadowMap;
  private final Map<Class, MetaShadow> metaShadowMap = new HashMap<Class, MetaShadow>();
  private final Map<String, Plan> planCache = new LinkedHashMap<String, Plan>() {
    @Override protected boolean removeEldestEntry(Map.Entry<String, Plan> eldest) {
      return size() > 500;
    }
  };
  private final Map<Class, ShadowConfig> shadowConfigCache = new HashMap<Class, ShadowConfig>();
  private final SdkConfig sdkConfig;

  public ShadowWrangler(ShadowMap shadowMap, SdkConfig sdkConfig) {
    this.shadowMap = shadowMap;
    this.sdkConfig = sdkConfig;
  }

  @Override
  public void classInitializing(Class clazz) {
    Class<?> shadowClass = findDirectShadowClass(clazz);
    if (shadowClass != null) {
      try {
        Method method = shadowClass.getMethod(InstrumentingClassLoader.STATIC_INITIALIZER_METHOD_NAME);
        if (!Modifier.isStatic(method.getModifiers())) {
          throw new RuntimeException(shadowClass.getName() + "." + method.getName() + " is not static");
        }
        method.setAccessible(true);
        method.invoke(null);
      } catch (NoSuchMethodException e) {
        RobolectricInternals.performStaticInitialization(clazz);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else {
      RobolectricInternals.performStaticInitialization(clazz);
    }
  }

  @Override public Object initializing(Object instance) {
    return createShadowFor(instance);
  }

  @Override
  synchronized public Plan methodInvoked(String signature, boolean isStatic, Class<?> theClass) {
    if (debug) System.out.println("[DEBUG] " + signature);
    if (planCache.containsKey(signature)) return planCache.get(signature);
    Plan plan = calculatePlan(signature, isStatic, theClass);
    planCache.put(signature, plan);
    return plan;
  }

  private Plan calculatePlan(String signature, boolean isStatic, Class<?> theClass) {
    final InvocationProfile invocationProfile = new InvocationProfile(signature, isStatic, theClass.getClassLoader());
    ShadowConfig shadowConfig = getShadowConfig(invocationProfile.clazz);

    // enable call-through for for inner classes if an outer class has call-through turned on
    Class<?> clazz = invocationProfile.clazz;
    while (shadowConfig == null && clazz.getDeclaringClass() != null) {
      clazz = clazz.getDeclaringClass();
      ShadowConfig outerConfig = getShadowConfig(clazz);
      if (outerConfig != null && outerConfig.callThroughByDefault) {
        shadowConfig = new ShadowConfig(Object.class.getName(), true, false, false);
      }
    }

    if (shadowConfig == null) {
      if (debug) System.out.println("[DEBUG] no shadow found for " + signature + "; will call real code");
      return CALL_REAL_CODE_PLAN;
    } else {
      try {
        final ClassLoader classLoader = theClass.getClassLoader();
        final Class<?> shadowClass = classLoader.loadClass(shadowConfig.shadowClassName);
        Method shadowMethod = getShadowedMethod(invocationProfile, classLoader, shadowClass);

        if (shadowMethod == null && shadowConfig.looseSignatures) {
          Class[] paramTypes = new Class[invocationProfile.paramTypes.length];
          for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = Object.class;
          }
          shadowMethod = getMethod(shadowClass, invocationProfile.methodName, paramTypes);
        }

        if (shadowMethod == null) {
          if (debug) System.out.println("[DEBUG] no shadow for " + signature + " found on " + shadowConfig.shadowClassName + "; " + describePlan(strict(invocationProfile)));
          return shadowConfig.callThroughByDefault ? CALL_REAL_CODE_PLAN : strict(invocationProfile) ? CALL_REAL_CODE_PLAN : DO_NOTHING_PLAN;
        }

        final Class<?> declaredShadowedClass = getShadowedClass(shadowMethod);

        if (declaredShadowedClass.equals(Object.class)) {
          // e.g. for equals(), hashCode(), toString()
          return CALL_REAL_CODE_PLAN;
        }

        boolean shadowClassMismatch = !declaredShadowedClass.equals(invocationProfile.clazz);
        if (shadowClassMismatch && (!shadowConfig.inheritImplementationMethods || strict(invocationProfile))) {
          boolean isConstructor = invocationProfile.methodName.equals(InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME);
          if (debug && !isConstructor) {
            System.out.println("[DEBUG] Method " + shadowMethod + " is meant to shadow " + declaredShadowedClass + ", not " + invocationProfile.clazz + "; will call real code");
          }
          return CALL_REAL_CODE_PLAN;
        } else {
          if (debug) System.out.println("[DEBUG] found shadow for " + signature + "; will call " + shadowMethod);
          return new ShadowMethodPlan(shadowMethod);
        }
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  synchronized private ShadowConfig getShadowConfig(Class clazz) {
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

  private Method getShadowedMethod(InvocationProfile invocationProfile, ClassLoader classLoader,
      Class<?> shadowClass) throws ClassNotFoundException {
    return getMethod(shadowClass, invocationProfile.methodName, invocationProfile.getParamClasses(classLoader));
  }

  private Method getMethod(Class<?> shadowClass, String methodName, Class<?>[] paramClasses) throws ClassNotFoundException {
    try {
      return shadowClass.getMethod(methodName, paramClasses);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private String describePlan(boolean willCallRealCode) {
    return willCallRealCode ? "will call real code" : "will do no-op";
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
    MethodSignature methodSignature = MethodSignature.parse(signature);

    if (debug) {
      System.out.println("DEBUG: intercepted call to " + methodSignature);
    }

    return getInterceptionHandler(methodSignature).call(theClass, instance, params);
  }

  public Function<Object, Object> getInterceptionHandler(MethodSignature methodSignature) {
    // todo: move these somewhere else!
    if (methodSignature.matches(LinkedHashMap.class.getName(), "eldest")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          LinkedHashMap map = (LinkedHashMap) value;
          return map.entrySet().iterator().next();
        }
      };
    } else if (methodSignature.matches("com.android.internal.policy.PolicyManager", "makeNewWindow")) {
      return new Function<Object, Object>() {
        @Override public Object call(Class<?> theClass, Object value, Object[] params) {
          ClassLoader cl = theClass.getClassLoader();
          Class<?> shadowWindowClass = type(ShadowWindow.class.getName()).withClassLoader(cl).load();
          Class<?> activityClass = type(Context.class.getName()).withClassLoader(cl).load();

          Object context = params[0];
          return method("create")
              .withParameterTypes(activityClass)
              .in(shadowWindowClass)
              .invoke(context);
        }
      };
    } else if (methodSignature.matches("java.lang.System", "nanoTime")) {
      return new Function<Object, Object>() {
        @Override public Object call(Class<?> theClass, Object value, Object[] params) {
          ClassLoader cl = theClass.getClassLoader();
          Class<?> shadowSystemClockClass = type("org.robolectric.shadows.ShadowSystemClock").withClassLoader(cl).load();
          return method("nanoTime")
              .in(shadowSystemClockClass)
              .invoke();
        }
      };
    }

    return ShadowWrangler.DO_NOTHING_HANDLER;
  }

  @Override
  public <T extends Throwable> T stripStackTrace(T throwable) {
    if (STRIP_SHADOW_STACK_TRACES) {
      List<StackTraceElement> stackTrace = new ArrayList<StackTraceElement>();

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

        if (methodName.startsWith(RobolectricInternals.ROBO_PREFIX)) {
          String fullPrefix = RobolectricInternals.directMethodName(stackTraceElement.getClassName(), "");
          if (methodName.startsWith(fullPrefix)) {
            methodName = methodName.substring(fullPrefix.length());
            stackTraceElement = new StackTraceElement(className, methodName,
                stackTraceElement.getFileName(), stackTraceElement.getLineNumber());
          }
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

  public Object createShadowFor(Object instance) {
    Object shadow;

    String shadowClassName = getShadowClassName(instance);

    if (shadowClassName == null) return new Object();

    if (debug)
      System.out.println("creating new " + shadowClassName + " as shadow for " + instance.getClass().getName());
    try {
      Class<?> shadowClass = loadClass(shadowClassName, instance.getClass().getClassLoader());
      Constructor<?> instanceConstructor = findInstanceConstructor(instance, shadowClass);
      Constructor<?> sdkConfigConstructor = findSdkConfigConstructor(shadowClass);
      if (instanceConstructor != null) {
        shadow = instanceConstructor.newInstance(instance);
      } else if (sdkConfigConstructor != null) {
        shadow = sdkConfigConstructor.newInstance(sdkConfig);
      } else {
        shadow = shadowClass.newInstance();
      }

      injectRealObjectOn(shadow, shadowClass, instance);

      return shadow;
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private String getShadowClassName(Object instance) {
    Class clazz = instance.getClass();
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

  private Constructor<?> findInstanceConstructor(Object instance, Class<?> shadowClass) {
    Class clazz = instance.getClass();

    Constructor constructor;
    for (constructor = null; constructor == null && clazz != null; clazz = clazz.getSuperclass()) {
      try {
        constructor = shadowClass.getConstructor(clazz);
      } catch (NoSuchMethodException e) {
        // expected
      }
    }
    return constructor;
  }

  private Constructor<?> findSdkConfigConstructor(Class<?> shadowClass) {

    Constructor constructor = null;
    try {
      constructor = shadowClass.getConstructor(SdkConfig.class);
    } catch (NoSuchMethodException e) {
      // expected
    }
    return constructor;
  }

  public static Object shadowOf(Object instance) {
    if (instance == null) {
      throw new NullPointerException("can't get a shadow for null");
    }
    return method(AsmInstrumentingClassLoader.GET_ROBO_DATA_METHOD_NAME).withReturnType(Object.class).in(instance).invoke();
  }

  private void writeField(Object target, Object value, Field realObjectField) {
    try {
      realObjectField.set(target, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private class MetaShadow {
    List<Field> realObjectFields = new ArrayList<Field>();

    public MetaShadow(Class<?> shadowClass) {
      while (shadowClass != null) {
        for (Field field : shadowClass.getDeclaredFields()) {
          if (field.isAnnotationPresent(RealObject.class)) {
            field.setAccessible(true);
            realObjectFields.add(field);
          }
        }
        shadowClass = shadowClass.getSuperclass();
      }

    }
  }

  private static class ShadowMethodPlan implements Plan {
    private final Method shadowMethod;

    public ShadowMethodPlan(Method shadowMethod) {
      this.shadowMethod = shadowMethod;
    }

    @Override public Object run(Object instance, Object roboData, Object[] params) throws Throwable {
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
}
