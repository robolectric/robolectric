package org.robolectric.internal.bytecode;

import org.robolectric.shadow.api.Shadow;

import java.util.Set;

import static org.robolectric.util.ReflectionHelpers.newInstance;
import static org.robolectric.util.ReflectionHelpers.setStaticField;

public class Sandbox {
  private final ClassLoader robolectricClassLoader;
  private final ShadowInvalidator shadowInvalidator;
  public ClassHandler classHandler; // todo not public
  private ShadowMap shadowMap = ShadowMap.EMPTY;

  public Sandbox(ClassLoader robolectricClassLoader) {
    this.robolectricClassLoader = robolectricClassLoader;
    this.shadowInvalidator = new ShadowInvalidator();
  }

  public <T> Class<T> bootstrappedClass(Class<?> clazz) {
    try {
      return (Class<T>) robolectricClassLoader.loadClass(clazz.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  public ClassLoader getRobolectricClassLoader() {
    return robolectricClassLoader;
  }

  public ShadowInvalidator getShadowInvalidator() {
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

    ClassLoader robolectricClassLoader = getRobolectricClassLoader();
    ShadowInvalidator invalidator = getShadowInvalidator();

    Class<?> robolectricInternalsClass = bootstrappedClass(RobolectricInternals.class);
    setStaticField(robolectricInternalsClass, "classHandler", classHandler);
    setStaticField(robolectricInternalsClass, "shadowInvalidator", invalidator);
    setStaticField(robolectricInternalsClass, "classLoader", robolectricClassLoader);

    Class<?> invokeDynamicSupportClass = bootstrappedClass(InvokeDynamicSupport.class);
    setStaticField(invokeDynamicSupportClass, "INTERCEPTORS", interceptors);

    Class<?> shadowClass = bootstrappedClass(Shadow.class);
    setStaticField(shadowClass, "SHADOW_IMPL", newInstance(bootstrappedClass(ShadowImpl.class)));
  }
}
