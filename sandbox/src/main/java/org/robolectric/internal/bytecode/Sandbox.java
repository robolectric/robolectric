package org.robolectric.internal.bytecode;

import static org.robolectric.util.ReflectionHelpers.newInstance;
import static org.robolectric.util.ReflectionHelpers.setStaticField;

import java.util.Set;
import org.robolectric.sandbox.UrlResourceProvider;
import org.robolectric.shadow.api.Shadow;

/**
 * A `Sandbox` represents an isolated execution environment with instrumented classes.
 */
public class Sandbox {
  private final ClassLoader sandboxClassLoader;
  private ShadowInvalidator shadowInvalidator;
  public ClassHandler classHandler; // todo not public
  private ShadowMap shadowMap = ShadowMap.EMPTY;

  public Sandbox(InstrumentationConfiguration instrumentationConfiguration,
      UrlResourceProvider resourceProvider) {
    this.sandboxClassLoader = new SandboxClassLoader(
        ClassLoader.getSystemClassLoader(),
        instrumentationConfiguration,
        resourceProvider);
  }

  protected ClassLoader getClassLoader() {
    return sandboxClassLoader;
  }

  public <T> Class<T> bootstrappedClass(Class<?> clazz) {
    return bootstrappedClass(clazz.getName());
  }

  public <T> Class<T> bootstrappedClass(String className) {
    try {
      return (Class<T>) sandboxClassLoader.loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @deprecated
   */
  @Deprecated
  public ClassLoader getRobolectricClassLoader() {
    return sandboxClassLoader;
  }

  private ShadowInvalidator getShadowInvalidator() {
    if (shadowInvalidator == null) {
      this.shadowInvalidator = new ShadowInvalidator();
    }
    return shadowInvalidator;
  }

  public void replaceShadowMap(ShadowMap shadowMap) {
    if (InvokeDynamic.ENABLED) {
      ShadowMap oldShadowMap = this.shadowMap;
      this.shadowMap = shadowMap;
      Set<String> invalidatedClasses = shadowMap.getInvalidatedClasses(oldShadowMap);
      getShadowInvalidator().invalidateClasses(invalidatedClasses);
    }
  }

  public void configure(ClassHandler classHandler, Interceptors interceptors) {
    this.classHandler = classHandler;

    Class<?> robolectricInternalsClass = bootstrappedClass(RobolectricInternals.class);
    if (InvokeDynamic.ENABLED) {
      ShadowInvalidator invalidator = getShadowInvalidator();
      setStaticField(robolectricInternalsClass, "shadowInvalidator", invalidator);
    }

    setStaticField(robolectricInternalsClass, "classHandler", classHandler);
    setStaticField(robolectricInternalsClass, "classLoader", sandboxClassLoader);

    Class<?> invokeDynamicSupportClass = bootstrappedClass(InvokeDynamicSupport.class);
    setStaticField(invokeDynamicSupportClass, "INTERCEPTORS", interceptors);

    Class<?> shadowClass = bootstrappedClass(Shadow.class);
    setStaticField(shadowClass, "SHADOW_IMPL", newInstance(bootstrappedClass(ShadowImpl.class)));
  }
}
