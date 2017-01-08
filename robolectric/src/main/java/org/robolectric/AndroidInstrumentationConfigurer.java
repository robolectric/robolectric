package org.robolectric;

import android.content.Context;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.internal.ShadowedObject;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.DirectObjectMarker;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentingClassLoader;
import org.robolectric.internal.bytecode.Intrinsics;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.internal.bytecode.ShadowInvalidator;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.fakes.RoboCharsets;
import org.robolectric.internal.fakes.RoboExtendedResponseCache;
import org.robolectric.internal.fakes.RoboResponseSource;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.TempDirectory;
import org.robolectric.util.Transcript;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ServiceLoader;

import static java.util.Arrays.asList;

public class AndroidInstrumentationConfigurer {
  public static InstrumentationConfiguration.Builder initialize(InstrumentationConfiguration.Builder builder) {
    for (MethodRef methodRef : Intrinsics.allRefs()) {
      builder.addInterceptedMethod(methodRef);
    }

    for (Class<?> clazz : asList(
        TestLifecycle.class,
        ShadowWrangler.class,
        AndroidManifest.class,
        InstrumentingClassLoader.class,
        SdkEnvironment.class,
        SdkConfig.class,
        RobolectricTestRunner.class,
        RobolectricTestRunner.HelperTestRunner.class,
        ResourcePath.class,
        ResourceTable.class,
        XmlBlock.class,
        ClassHandler.class,
        ClassHandler.Plan.class,
        ShadowInvalidator.class,
        RealObject.class,
        Implements.class,
        Implementation.class,
        Instrument.class,
        DoNotInstrument.class,
        Config.class,
        Transcript.class,
        DirectObjectMarker.class,
        DependencyJar.class,
        ParallelUniverseInterface.class,
        ShadowedObject.class,
        TempDirectory.class
    )) {
      builder.doNotAcquireClass(clazz.getName());
    }

    for (String packageName : asList(
        "java.",
        "javax.",
        "sun.",
        "com.sun.",
        "org.w3c.",
        "org.xml.",
        "org.junit",
        "org.hamcrest",
        "org.specs2",  // allows for android projects with mixed scala\java tests to be
        "scala.",      //  run with Maven Surefire (see the RoboSpecs project on github)
        "kotlin.",
        "com.almworks.sqlite4java" // Fix #958: SQLite native library must be loaded once.
    )) {
      builder.doNotAcquirePackage(packageName);
    }

    builder.addClassNameTranslation("java.net.ExtendedResponseCache", RoboExtendedResponseCache.class.getName());
    builder.addClassNameTranslation("java.net.ResponseSource", RoboResponseSource.class.getName());
    builder.addClassNameTranslation("java.nio.charset.Charsets", RoboCharsets.class.getName());

    // Instrumenting these classes causes a weird failure.
    builder.doNotInstrumentClass("android.R");
    builder.doNotInstrumentClass("android.R$styleable");

    for (String packageName : asList("dalvik.", "libcore.", "android.", "com.android.internal.", "org.apache.http.", "org.kxml2.")) {
      builder.addInstrumentedPackage(packageName);
    }

    for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
      for (String packageName : provider.getProvidedPackageNames()) {
        builder.addInstrumentedPackage(packageName);
      }
    }

    return builder;
  }

  public static InstrumentationConfiguration.Builder withConfig(InstrumentationConfiguration.Builder builder, Config config) {
    for (Class<?> clazz : config.shadows()) {
      Implements annotation = clazz.getAnnotation(Implements.class);
      if (annotation == null) {
        throw new IllegalArgumentException(clazz + " is not annotated with @Implements");
      }

      String className = annotation.className();
      if (className.isEmpty()) {
        className = annotation.value().getName();
      }

      if (!className.isEmpty()) {
        builder.addInstrumentedClass(className);
      }
    }
    for (String packageName : config.instrumentedPackages()) {
      builder.addInstrumentedPackage(packageName);
    }
    return builder;
  }

  public static List<ShadowWrangler.MethodCallHandler> getMethodCallHandlers() {
    List<ShadowWrangler.MethodCallHandler> methodCallHandlers = new ArrayList<>();
    methodCallHandlers.add(new ShadowWrangler.MethodCallHandler() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches(LinkedHashMap.class.getName(), "eldest");
      }

      @Override
      public Function<Object, Object> getInterceptionHandler(MethodSignature methodSignature) {
        return new Function<Object, Object>() {
          @Override
          public Object call(Class<?> theClass, Object value, Object[] params) {
            LinkedHashMap map = (LinkedHashMap) value;
            return map.isEmpty() ? null : map.entrySet().iterator().next();
          }
        };
      }
    });

    methodCallHandlers.add(new ShadowWrangler.MethodCallHandler() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches("com.android.internal.policy.PolicyManager", "makeNewWindow");
      }

      @Override
      public Function<Object, Object> getInterceptionHandler(MethodSignature methodSignature) {
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
            return ReflectionHelpers.callStaticMethod(shadowWindowClass, "create", ReflectionHelpers.ClassParameter.from(activityClass, context));
          }
        };
      }
    });

    methodCallHandlers.add(new ShadowWrangler.MethodCallHandler() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches("java.lang.System", "nanoTime") || methodSignature.matches("java.lang.System", "currentTimeMillis");
      }

      @Override
      public Function<Object, Object> getInterceptionHandler(final MethodSignature methodSignature) {
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
      }
    });

    methodCallHandlers.add(new ShadowWrangler.MethodCallHandler() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches("java.lang.System", "arraycopy");
      }

      @Override
      public Function<Object, Object> getInterceptionHandler(MethodSignature methodSignature) {
        return new Function<Object, Object>() {
          @Override
          public Object call(Class<?> theClass, Object value, Object[] params) {
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(params[0], (Integer) params[1], params[2], (Integer) params[3], (Integer) params[4]);
            return null;
          }
        };
      }
    });

    methodCallHandlers.add(new ShadowWrangler.MethodCallHandler() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches("java.util.Locale", "adjustLanguageCode");
      }

      @Override
      public Function<Object, Object> getInterceptionHandler(MethodSignature methodSignature) {
        return new Function<Object, Object>() {
          @Override
          public Object call(Class<?> theClass, Object value, Object[] params) {
            return params[0];
          }
        };
      }
    });


    methodCallHandlers.add(new ShadowWrangler.MethodCallHandler() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches("java.lang.System", "logE");
      }

      @Override
      public Function<Object, Object> getInterceptionHandler(MethodSignature methodSignature) {
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
    });

    return methodCallHandlers;
  }
}
