package org.robolectric.internal.bytecode;

import static org.robolectric.util.ReflectionHelpers.newInstance;
import static org.robolectric.util.ReflectionHelpers.setStaticField;

import java.util.Set;
import org.robolectric.shadow.api.Shadow;

public class Sandbox {
  private final ClassLoader robolectricClassLoader;
  private ShadowInvalidator shadowInvalidator;
  public ClassHandler classHandler; // todo not public
  private ShadowMap shadowMap = ShadowMap.EMPTY;

  public Sandbox(ClassLoader robolectricClassLoader) {
    this.robolectricClassLoader = robolectricClassLoader;
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

    ClassLoader robolectricClassLoader = getRobolectricClassLoader();
    Class<?> robolectricInternalsClass = bootstrappedClass(RobolectricInternals.class);
    if (InvokeDynamic.ENABLED) {
      ShadowInvalidator invalidator = getShadowInvalidator();
      setStaticField(robolectricInternalsClass, "shadowInvalidator", invalidator);
    }

    setStaticField(robolectricInternalsClass, "classHandler", classHandler);
    setStaticField(robolectricInternalsClass, "classLoader", robolectricClassLoader);

    Class<?> invokeDynamicSupportClass = bootstrappedClass(InvokeDynamicSupport.class);
    setStaticField(invokeDynamicSupportClass, "INTERCEPTORS", interceptors);

    Class<?> shadowClass = bootstrappedClass(Shadow.class);
    setStaticField(shadowClass, "SHADOW_IMPL", newInstance(bootstrappedClass(ShadowImpl.class)));
  }
}
