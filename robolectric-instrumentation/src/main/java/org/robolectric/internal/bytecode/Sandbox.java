package org.robolectric.internal.bytecode;

import org.robolectric.internal.InvokeDynamic;

import java.util.Set;

public class Sandbox {
  private final ClassLoader robolectricClassLoader;
  private final ShadowInvalidator shadowInvalidator;
  public ClassHandler classHandler; // todo not public
  private ShadowMap shadowMap = ShadowMap.EMPTY;

  public Sandbox(ClassLoader robolectricClassLoader) {
    this.robolectricClassLoader = robolectricClassLoader;
    this.shadowInvalidator = new ShadowInvalidator();
  }

  public <T> Class<T> bootstrappedClass(Class<?> testClass) {
    try {
      return (Class<T>) robolectricClassLoader.loadClass(testClass.getName());
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

  public ClassHandler getClassHandler() {
    return classHandler;
  }
}
