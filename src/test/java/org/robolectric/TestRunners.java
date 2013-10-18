package org.robolectric;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.annotation.Config;
import org.robolectric.bytecode.AndroidTranslatorClassInstrumentedTest;
import org.robolectric.bytecode.ClassInfo;
import org.robolectric.bytecode.Setup;
import org.robolectric.bytecode.ShadowMap;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.ShadowSystemProperties;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.robolectric.util.TestUtil.resourceFile;

public class TestRunners {
  public static class WithCustomClassList extends RobolectricTestRunner {
    public WithCustomClassList(@SuppressWarnings("rawtypes") Class testClass) throws InitializationError {
      super(testClass);
    }

    @Override
    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) {
      return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
    }

    @Override
    public Setup createSetup() {
      return new Setup() {
        @Override
        public boolean shouldInstrument(ClassInfo classInfo) {
          String name = classInfo.getName();
          if (name.equals(AndroidTranslatorClassInstrumentedTest.CustomPaint.class.getName())
              || name.equals(AndroidTranslatorClassInstrumentedTest.ClassWithPrivateConstructor.class.getName())) {
            return true;
          }
          return super.shouldInstrument(classInfo);
        }
      };
    }
  }

  public static class WithoutDefaults extends RobolectricTestRunner {
    public WithoutDefaults(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override protected ShadowMap createShadowMap() {
      // Don't do any class binding except the bare minimum, because that's what we're trying to test here.
      return new ShadowMap.Builder()
          .addShadowClass(ShadowSystemProperties.class)
          .build();
    }

    @Override protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) {
      return null;
    }

    @Override
    protected void setUpApplicationState(Method method, ParallelUniverseInterface parallelUniverseInterface, boolean strictI18n, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
      // Don't do any resource loading or app init, because that's what we're trying to test here.
    }
  }

  public static class WithDefaults extends RobolectricTestRunner {
    public WithDefaults(Class<?> testClass) throws InitializationError {
      super(testClass);
      Locale.setDefault(Locale.ENGLISH);
    }

    @Override public Setup createSetup() {
      return new Setup() {
        @Override public boolean shouldAcquire(String name) {
          // todo: whyyyyy!?!? if this isn't there, tests after TestRunnerSequenceTest start failing bad.
          if (name.startsWith("org.mockito.")) return false;
          return super.shouldAcquire(name);
        }
      };
    }

    @Override
    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) {
      return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
    }
  }

  public static class RealApisWithoutDefaults extends RobolectricTestRunner {
    public RealApisWithoutDefaults(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override protected ShadowMap createShadowMap() {
      // Don't do any class binding, because that's what we're trying to test here.
      return ShadowMap.EMPTY;
    }

    @Override public AndroidManifest getAppManifest(Config config) {
      // Don't do any resource loading or app init, because that's what we're trying to test here.
      return null;
    }

    @Override
    protected void setUpApplicationState(Method method,
        ParallelUniverseInterface parallelUniverseInterface, boolean strictI18n,
        ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
      // Don't do any resource loading or app init, because that's what we're trying to test here.
    }
  }
}
