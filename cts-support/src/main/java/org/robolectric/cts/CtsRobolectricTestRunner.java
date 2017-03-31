package org.robolectric.cts;

import android.app.Instrumentation;
import junit.framework.Test;
import org.junit.rules.Timeout;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.SdkPicker;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.manifest.AndroidManifest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class CtsRobolectricTestRunner extends RobolectricTestRunner {

  private boolean isJunit3;

  public CtsRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);

  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    isJunit3 = Test.class.isAssignableFrom(getTestClass().getJavaClass());

    super.collectInitializationErrors(errors);
  }

  @Override
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    return new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
        .doNotInstrumentClass(method.getDeclaringClass().getName())
        .build();
  }

  @Override
  protected Class<?>[] getExtraShadows(FrameworkMethod frameworkMethod) {
    return new Class[] {
        ShadowInstrumentation.class
    };
  }

  @Override
  protected List<FrameworkMethod> computeTestMethods() {
    if (isJunit3) {
      List<FrameworkMethod> testMethods = new ArrayList<>();
      testMethods.addAll(super.computeTestMethods());
      if (testMethods.isEmpty()) {
        for (Method method : getTestClass().getJavaClass().getMethods()) {
          if (method.getName().startsWith("test")) {
            testMethods.add(new FrameworkMethod(method));
          }
        }
      }
      return testMethods;
    } else {
      return super.computeTestMethods();
    }
  }

  @Override
  protected SdkPicker createSdkPicker() {
    return new SdkPicker() {
      @Override
      public List<SdkConfig> selectSdks(Config config, AndroidManifest appManifest) {
        return asList(new SdkConfig(25));
      }
    };
  }

  @Override
  protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {
    super.afterTest(method, bootstrappedMethod);
  }

  @Override
  protected SandboxTestRunner.HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) throws InitializationError {
    Method fixup;
    Object junit3Adapter;

    try {
      Class<?> junit3AdapterClass = bootstrappedTestClass.getClassLoader()
          .loadClass(JUnit3Adapter.class.getName());
      junit3Adapter = junit3AdapterClass.newInstance();
      fixup = junit3AdapterClass.getMethod("fixup", Object.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new HelperTestRunner(bootstrappedTestClass) {
      @Override
      protected List<FrameworkMethod> computeTestMethods() {
        return CtsRobolectricTestRunner.this.computeTestMethods();
      }

      @Override
      protected Statement methodBlock(FrameworkMethod method) {
        Statement statement = super.methodBlock(method);
        return new Statement() {
          @Override
          public void evaluate() throws Throwable {
            Thread mainThread = Thread.currentThread();
            Thread warningThread = new Thread(() -> {
              try {
                Thread.sleep(20 * 1000);
                System.out.println("Still running after 20s: " + method);
              } catch (InterruptedException e) {
                return;
              }

              try {
                Thread.sleep(20 * 1000);
                System.out.println("Still running after 40s: " + method + "; interrupting.");
                mainThread.interrupt();
              } catch (InterruptedException e) {
                return;
              }

              try {
                Thread.sleep(20 * 1000);
                System.out.println("Still running after 60s: " + method + "; killing.");
                mainThread.stop();
              } catch (InterruptedException e) {
                return;
              }
            });
            warningThread.start();

            try {
              statement.evaluate();
            } catch (ThreadDeath threadDeath) {
              threadDeath.printStackTrace();
            } finally {
              warningThread.interrupt();
              warningThread.join();
            }
          }
        };
      }

      @Override
      protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        try {
          fixup.invoke(junit3Adapter, target);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
          throw new RuntimeException(e);
        }

        Method setUp = getMethod(method.getDeclaringClass(), "setUp");
        Statement befores = super.withBefores(method, target, statement);
        if (setUp == null) {
          return befores;
        } else {
          return new Statement() {
            @Override
            public void evaluate() throws Throwable {
              setUp.setAccessible(true);
              setUp.invoke(target);
              befores.evaluate();
            }
          };
        }
      }

      @Override
      protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
        Method tearDown = getMethod(method.getDeclaringClass(), "tearDown");
        Statement afters = super.withBefores(method, target, statement);
        if (tearDown == null) {
          return afters;
        } else {
          return new Statement() {
            @Override
            public void evaluate() throws Throwable {
              afters.evaluate();
              tearDown.setAccessible(true);
              tearDown.invoke(target);
            }
          };
        }
      }
    };
  }

  private Method getMethod(Class<?> clazz, String methodName) {
    try {
      return clazz.getDeclaredMethod(methodName);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  @Implements(Instrumentation.class)
  public static class ShadowInstrumentation {
    @Implementation
    public final void validateNotAppThread() {
    }
  }
}
