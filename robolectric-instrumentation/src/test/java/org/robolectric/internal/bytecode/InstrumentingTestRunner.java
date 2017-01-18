package org.robolectric.internal.bytecode;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstrumentingTestRunner extends BlockJUnit4ClassRunner {
  public InstrumentingTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);

    if (shouldIgnore(method)) {
      eachNotifier.fireTestIgnored();
    } else {
      eachNotifier.fireTestStarted();
      try {
        InstrumentationConfiguration instrumentationConfiguration =
            InstrumentationConfiguration.newBuilder()
            .build();
        InstrumentingClassLoader instrumentingClassLoader =
            new InstrumentingClassLoader(instrumentationConfiguration, new URL[0]);
        methodBlock(method).evaluate();
      } catch (AssumptionViolatedException e) {
        eachNotifier.addFailedAssumption(e);
      } catch (Throwable e) {
        eachNotifier.addFailure(e);
      } finally {
        eachNotifier.fireTestFinished();
      }
    }
  }

  Statement methodBlock(final FrameworkMethod method, InstrumentingClassLoader instrumentingClassLoader) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        ShadowMap shadowMap = configureShadows(method);
        ClassHandler classHandler = new ShadowWrangler(shadowMap, 0, new Interceptors());

        Thread.currentThread().setContextClassLoader(sdkEnvironment.getRobolectricClassLoader());

        Class bootstrappedTestClass = sdkEnvironment.bootstrappedClass(getTestClass().getJavaClass());
        HelperTestRunner helperTestRunner = getHelperTestRunner(bootstrappedTestClass);

        final Method bootstrappedMethod;
        try {
          //noinspection unchecked
          bootstrappedMethod = bootstrappedTestClass.getMethod(method.getMethod().getName());
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(e);
        }

        parallelUniverseInterface = getHooksInterface(sdkEnvironment);
        try {
          try {
            // Only invoke @BeforeClass once per class
            if (!loadedTestClasses.contains(bootstrappedTestClass)) {
              invokeBeforeClass(bootstrappedTestClass);
            }
            assureTestLifecycle(sdkEnvironment);

            parallelUniverseInterface.setSdkConfig(sdkEnvironment.getSdkConfig());
            parallelUniverseInterface.resetStaticState(config);

            SdkConfig sdkConfig = ((RobolectricFrameworkMethod) method).sdkConfig;
            Class<?> androidBuildVersionClass = sdkEnvironment.bootstrappedClass(Build.VERSION.class);
            ReflectionHelpers.setStaticField(androidBuildVersionClass, "SDK_INT", sdkConfig.getApiLevel());
            ReflectionHelpers.setStaticField(androidBuildVersionClass, "RELEASE", sdkConfig.getAndroidVersion());

            PackageResourceTable systemResourceTable = sdkEnvironment.getSystemResourceTable(getJarResolver());
            PackageResourceTable appResourceTable = getAppResourceTable(appManifest);

            parallelUniverseInterface.setUpApplicationState(bootstrappedMethod, testLifecycle, appManifest, config, new RoutingResourceTable(getCompiletimeSdkResourceTable(), appResourceTable), new RoutingResourceTable(systemResourceTable, appResourceTable), new RoutingResourceTable(systemResourceTable));
            testLifecycle.beforeTest(bootstrappedMethod);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }

          final Statement statement = helperTestRunner.methodBlock(new FrameworkMethod(bootstrappedMethod));

          // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
          try {
            statement.evaluate();
          } finally {
            try {
              parallelUniverseInterface.tearDownApplication();
            } finally {
              try {
                internalAfterTest(bootstrappedMethod);
              } finally {
                parallelUniverseInterface.resetStaticState(config); // afterward too, so stuff doesn't hold on to classes?
              }
            }
          }
        } finally {
          Thread.currentThread().setContextClassLoader(RobolectricTestRunner.class.getClassLoader());
          parallelUniverseInterface = null;
        }
      }
    };
  }

  private ShadowMap configureShadows(FrameworkMethod method) {
    ShadowMap.Builder builder = ShadowMap.EMPTY.newBuilder();

    // Configure shadows *BEFORE* setting the ClassLoader. This is necessary because
    // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
    // not available once we install the Robolectric class loader.
    RoboConfig roboConfig = method.getAnnotation(RoboConfig.class);
    if (roboConfig != null) {
      builder.addShadowClasses(roboConfig.shadows());
    }
    return builder.build();
  }


  protected boolean shouldIgnore(FrameworkMethod method) {
    return method.getAnnotation(Ignore.class) != null;
  }

}
