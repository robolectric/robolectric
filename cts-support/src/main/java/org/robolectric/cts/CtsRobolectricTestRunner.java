package org.robolectric.cts;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.support.test.InstrumentationRegistry;
import android.view.InputEvent;
import android.view.KeyEvent;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.SdkPicker;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class CtsRobolectricTestRunner extends RobolectricTestRunner {
  private static final boolean KILL_LONG_RUNNING_TESTS = true;
  private static final boolean ASSERT_ON_EXPECTED_TEST_OUTCOME = false; // todo DO NOT SUBMIT

  private boolean isJunit3;
  private static final CtsResults ctsResults;

  static {
    CtsResults loadedCtsResults;
    try {
      InputStream in = CtsRobolectricTestRunner.class.getClassLoader().getResourceAsStream("cts-results.yaml");
      loadedCtsResults = CtsResults.load(in);

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
          if (ctsResults.changed()) {
            try {
              ctsResults.save(new PrintWriter(new FileWriter("cts-results-new.yaml")));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        }
      }));
    } catch (Exception e) {
      loadedCtsResults = null;
      e.printStackTrace();
    }
    ctsResults = loadedCtsResults;
  }

  public CtsRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected ManifestFactory getManifestFactory(Config config) {
    return new ManifestFactory() {
      @Override
      public ManifestIdentifier identify(Config config) {
        return new ManifestIdentifier(
            Fs.fileFromPath(Config.DEFAULT_MANIFEST_NAME),
            Fs.fileFromPath(Config.DEFAULT_RES_FOLDER),
            Fs.fileFromPath(Config.DEFAULT_ASSET_FOLDER),
            null, Collections.emptyList());
      }

      @Override
      public AndroidManifest create(ManifestIdentifier manifestIdentifier) {
        return new AndroidManifest(
            manifestIdentifier.getManifestFile(),
            manifestIdentifier.getResDir(),
            manifestIdentifier.getAssetDir()
        );
      }
    };
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    isJunit3 = Test.class.isAssignableFrom(getTestClass().getJavaClass());

    // because of AlarmClockTestBase
    if (isJunit3 && computeTestMethods().isEmpty()) {
      return;
    }

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
    return new Class[]{
        ShadowInstrumentation.class,
        ShadowInstrumentation.ShadowActivityMonitor.class,
        ShadowUiAutomation.class
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
    Method fixup3;
    Method fixup4;
    Object androidJUnitAdapter;

    try {
      Class<?> androidJUnitAdapterClass = bootstrappedTestClass.getClassLoader()
          .loadClass(AndroidJUnitAdapter.class.getName());
      androidJUnitAdapter = androidJUnitAdapterClass.newInstance();
      fixup3 = androidJUnitAdapterClass.getMethod("fixup3", Object.class);
      fixup4 = androidJUnitAdapterClass.getMethod("fixup4");
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    return new HelperTestRunner(bootstrappedTestClass) {
      @Override
      protected List<FrameworkMethod> computeTestMethods() {
        return CtsRobolectricTestRunner.this.computeTestMethods();
      }

      @Override
      protected Statement methodBlock(FrameworkMethod method) {
        try {
          fixup4.invoke(androidJUnitAdapter);
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }

        Statement statement = super.methodBlock(method);
        return new Statement() {
          @Override
          public void evaluate() throws Throwable {
            CtsTestResult expectedResult = null;
            CtsTestClass ctsTestClass = ctsResults.getCtsClass(method.getDeclaringClass().getName());
            if (ctsTestClass != null) {
              expectedResult = ctsTestClass.methods.get(method.getName());
            }

            if (expectedResult == CtsTestResult.DISABLE) {
              // don't run, it'll probably hang...
              System.out.println("Skipping " + method + ": disabled");
              return;
            }

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

            if (KILL_LONG_RUNNING_TESTS) {
              warningThread.start();
            }

            if (ASSERT_ON_EXPECTED_TEST_OUTCOME) {
              CtsTestResult actualResult;
              Throwable error = null;
              try {
                statement.evaluate();

                actualResult = CtsTestResult.PASS;
              } catch (AssertionFailedError e) {
                error = e;
                actualResult = CtsTestResult.ASSERT_FAIL;
              } catch (ThreadDeath threadDeath) {
                threadDeath.printStackTrace();
                actualResult = CtsTestResult.TIMEOUT;
                error = threadDeath;
              } catch (Throwable e) {
                error = e;
                actualResult = CtsTestResult.FAIL;
              } finally {
                warningThread.interrupt();
                warningThread.join();
              }

              if (actualResult == expectedResult) {
                if (expectedResult != CtsTestResult.PASS) {
                  System.out.println("Expected " + expectedResult + " for " + method + " and got it.");
                }
              } else {
                if (expectedResult != null) {
                  System.out.println("Expected " + expectedResult + " for " + method + ", but got " + actualResult + ".");
                }

                ctsResults.setResult(method.getDeclaringClass().getName(), method.getMethod().getName(), actualResult);

                if (error != null) {
                  throw error;
                } else if (expectedResult != null && actualResult == CtsTestResult.PASS) {
                  throw new RuntimeException(method + " unexpectedly passed (yay!)");
                }
              }
            } else {
              statement.evaluate();
            }
          }
        };
      }

      @Override
      protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        try {
          fixup3.invoke(androidJUnitAdapter, target);
        } catch (Exception e) {
          e.printStackTrace();
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

    @Implements(Instrumentation.ActivityMonitor.class)
    public static class ShadowActivityMonitor {
      @Implementation
      public final Activity waitForActivity() {
        return null;
      }
    }
  }

  @Implements(UiAutomation.class)
  public static class ShadowUiAutomation {
    @Implementation
    public boolean injectInputEvent(InputEvent event, boolean sync) {
      // todo: yeah, this doesn't work at all...
      Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
      if (event instanceof KeyEvent) {
        instrumentation.sendKeyDownUpSync(((KeyEvent) event).getKeyCode());
      } else {
        throw new UnsupportedOperationException(event.toString());
      }
      return true;
    }
  }
}
