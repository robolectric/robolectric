package org.robolectric;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.ClassHandlerBuilder;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.internal.bytecode.ShadowInfo;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.internal.bytecode.UrlResourceProvider;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.util.inject.Injector;

public class BasicSandboxTestRunner extends SandboxTestRunner {

  protected static Injector.Builder defaultInjector() {
    return new Injector.Builder()
        .bindDefault(ClassInstrumentor.class, ClassInstrumentor.pickInstrumentorClass())
        .bind(Properties.class, System.getProperties());
  }

  private ClassInstrumentor classInstrumentor;
  private ShadowProviders shadowProviders;
  private ClassHandlerBuilder classHandlerBuilder;

  public BasicSandboxTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    Injector injector = defaultInjector().build();
    classInstrumentor = injector.getInstance(ClassInstrumentor.class);
    shadowProviders = injector.getInstance(ShadowProviders.class);
    classHandlerBuilder = injector.getInstance(ClassHandlerBuilder.class);
  }

  @Nonnull
  @Override
  protected Sandbox getSandbox(FrameworkMethod method) {
    InstrumentationConfiguration config = createClassLoaderConfig(method);
    return new Sandbox(new SandboxClassLoader(config, new UrlResourceProvider(), classInstrumentor));
  }

  @Nonnull
  protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
    InstrumentationConfiguration.Builder builder =
        InstrumentationConfiguration.newBuilder()
            .doNotAcquirePackage("java.")
            .doNotAcquirePackage("jdk.internal.")
            .doNotAcquirePackage("sun.")
            .doNotAcquirePackage("org.robolectric.annotation.")
            .doNotAcquirePackage("org.robolectric.internal.")
            .doNotAcquirePackage("org.robolectric.pluginapi.")
            .doNotAcquirePackage("org.robolectric.util.")
            .doNotAcquirePackage("org.junit");

    String customPackages = System.getProperty("org.robolectric.packagesToNotAcquire", "");
    for (String pkg : customPackages.split(",")) {
      if (!pkg.isEmpty()) {
        builder.doNotAcquirePackage(pkg);
      }
    }

    String customClassesRegex =
        System.getProperty("org.robolectric.classesToNotInstrumentRegex", "");
    if (!customClassesRegex.isEmpty()) {
      builder.setDoNotInstrumentClassRegex(customClassesRegex);
    }

    for (Class<?> shadowClass : getExtraShadows(method)) {
      ShadowInfo shadowInfo = ShadowMap.obtainShadowInfo(shadowClass);
      builder.addInstrumentedClass(shadowInfo.shadowedClassName);
    }

    addInstrumentedPackages(method, builder);

    return builder.build();
  }

  private void addInstrumentedPackages(FrameworkMethod method, InstrumentationConfiguration.Builder builder) {
    SandboxConfig classConfig = getTestClass().getJavaClass().getAnnotation(SandboxConfig.class);
    if (classConfig != null) {
      for (String pkgName : classConfig.instrumentedPackages()) {
        builder.addInstrumentedPackage(pkgName);
      }
    }

    SandboxConfig methodConfig = method.getAnnotation(SandboxConfig.class);
    if (methodConfig != null) {
      for (String pkgName : methodConfig.instrumentedPackages()) {
        builder.addInstrumentedPackage(pkgName);
      }
    }
  }

  @Override
  protected void configureSandbox(Sandbox sandbox, FrameworkMethod method) {
    ShadowMap.Builder builder = shadowProviders.getBaseShadowMap().newBuilder();

    // Configure shadows *BEFORE* setting the ClassLoader. This is necessary because
    // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
    // not available once we install the Robolectric class loader.
    Class<?>[] shadows = getExtraShadows(method);
    if (shadows.length > 0) {
      builder.addShadowClasses(shadows);
    }
    ShadowMap shadowMap = builder.build();
    sandbox.replaceShadowMap(shadowMap);

    sandbox.configure(createClassHandler(shadowMap, sandbox), getInterceptors());
  }

  private Interceptors getInterceptors() {
    return new Interceptors();
  }

  @Nonnull
  protected Class<?>[] getExtraShadows(FrameworkMethod method) {
    List<Class<?>> shadowClasses = new ArrayList<>();
    addShadows(shadowClasses, getTestClass().getJavaClass().getAnnotation(SandboxConfig.class));
    addShadows(shadowClasses, method.getAnnotation(SandboxConfig.class));
    return shadowClasses.toArray(new Class[shadowClasses.size()]);
  }

  private void addShadows(List<Class<?>> shadowClasses, SandboxConfig annotation) {
    if (annotation != null) {
      shadowClasses.addAll(asList(annotation.shadows()));
    }
  }

  @Nonnull
  protected ClassHandler createClassHandler(ShadowMap shadowMap, Sandbox sandbox) {
    return classHandlerBuilder.build(shadowMap, ShadowMatcher.MATCH_ALL, getInterceptors());
  }

}
