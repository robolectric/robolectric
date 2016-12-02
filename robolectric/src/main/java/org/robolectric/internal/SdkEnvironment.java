package org.robolectric.internal;

import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.InvokeDynamic;
import org.robolectric.internal.bytecode.RobolectricInternals;
import org.robolectric.internal.bytecode.ShadowInvalidator;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceExtractor;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.util.ReflectionHelpers;

import java.util.Map;
import java.util.Set;

public class SdkEnvironment {
  private final SdkConfig sdkConfig;
  private final ClassLoader robolectricClassLoader;
  private final ShadowInvalidator shadowInvalidator;
  private final Map<ShadowMap, ClassHandler> classHandlersByShadowMap = new LruCacheHashMap<>(10);

  private ShadowMap shadowMap = ShadowMap.EMPTY; // lazily initialized, but then effectively final

  private ResourceLoader systemResourceLoader;

  public SdkEnvironment(SdkConfig sdkConfig, ClassLoader robolectricClassLoader) {
    this.sdkConfig = sdkConfig;
    this.robolectricClassLoader = robolectricClassLoader;
    shadowInvalidator = new ShadowInvalidator();
  }

  public synchronized ResourceLoader getSystemResourceLoader(DependencyResolver dependencyResolver) {
    if (systemResourceLoader == null) {
      ResourcePath resourcePath;
      try {
        Class<?> androidInternalRClass = getRobolectricClassLoader().loadClass("com.android.internal.R");
        Class<?> androidRClass = getRobolectricClassLoader().loadClass("android.R");
        Fs systemResFs = Fs.fromJar(dependencyResolver.getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency()));
        resourcePath = new ResourcePath(androidRClass, androidRClass.getPackage().getName(), systemResFs.join("res"), systemResFs.join("assets"), androidInternalRClass);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      ResourceExtractor resourceExtractor = new ResourceExtractor(resourcePath);
      systemResourceLoader = new PackageResourceLoader(resourcePath, resourceExtractor);
    }
    return systemResourceLoader;
  }

  public <T> Class<T> bootstrappedClass(Class<?> clazz) {
    try {
      //noinspection unchecked
      return (Class<T>) robolectricClassLoader.loadClass(clazz.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> Class<T> bootstrappedClass(Class<?> clazz, Class<T> subclass) {
    return (Class<T>) bootstrappedClass(clazz).asSubclass(subclass);
  }

  public ClassLoader getRobolectricClassLoader() {
    return robolectricClassLoader;
  }

  public SdkConfig getSdkConfig() {
    return sdkConfig;
  }

  public void useShadowMap(ShadowMap shadowMap) {
    ShadowMap oldMap = this.shadowMap;
    this.shadowMap = shadowMap;

    if (InvokeDynamic.ENABLED) {
      Set<String> invalidatedClasses = shadowMap.getInvalidatedClasses(oldMap);
      shadowInvalidator.invalidateClasses(invalidatedClasses);
    }

    injectEnvironment();
  }

  public void injectEnvironment() {
    String className = RobolectricInternals.class.getName();
    Class<?> robolectricInternalsClass = ReflectionHelpers.loadClass(robolectricClassLoader, className);
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "classHandler", getClassHandler(shadowMap));
    ReflectionHelpers.setStaticField(robolectricInternalsClass, "shadowInvalidator", shadowInvalidator);
  }

  protected synchronized ClassHandler getClassHandler(ShadowMap shadowMap) {
    ClassHandler classHandler = classHandlersByShadowMap.get(shadowMap);
    if (classHandler == null) {
      classHandler = new ShadowWrangler(shadowMap, getSdkConfig().getApiLevel());
      classHandlersByShadowMap.put(shadowMap, classHandler);
    }
    return classHandler;
  }
}
