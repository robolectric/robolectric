package org.robolectric;

import org.robolectric.bytecode.ClassHandler;
import org.robolectric.bytecode.ShadowMap;
import org.robolectric.bytecode.ShadowWrangler;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceExtractor;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SdkEnvironment {
  private final SdkConfig sdkConfig;
  private final ClassLoader robolectricClassLoader;
  public final Map<ShadowMap, ShadowWrangler> classHandlersByShadowMap = new HashMap<ShadowMap, ShadowWrangler>();
  private ClassHandler currentClassHandler;
  private ResourceLoader systemResourceLoader;

  public SdkEnvironment(SdkConfig sdkConfig, ClassLoader robolectricClassLoader) {
    this.sdkConfig = sdkConfig;
    this.robolectricClassLoader = robolectricClassLoader;
  }

  public PackageResourceLoader createSystemResourceLoader(MavenCentral mavenCentral, RobolectricTestRunner robolectricTestRunner) {
    URL url = mavenCentral.getLocalArtifactUrl(robolectricTestRunner, sdkConfig.getSystemResourceDependency());
    Fs systemResFs = Fs.fromJar(url);
    ResourceExtractor resourceExtractor = new ResourceExtractor(getRobolectricClassLoader());
    ResourcePath resourcePath = new ResourcePath(resourceExtractor.getProcessedRFile(), resourceExtractor.getPackageName(), systemResFs.join("res"), systemResFs.join("assets"));
    return new PackageResourceLoader(resourcePath, resourceExtractor);
  }

  public synchronized ResourceLoader getSystemResourceLoader(MavenCentral mavenCentral, RobolectricTestRunner robolectricTestRunner) {
    if (systemResourceLoader == null) {
      systemResourceLoader = createSystemResourceLoader(mavenCentral, robolectricTestRunner);
    }
    return systemResourceLoader;
  }

  public Class<?> bootstrappedClass(Class<?> testClass) {
    try {
      return robolectricClassLoader.loadClass(testClass.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public ClassLoader getRobolectricClassLoader() {
    return robolectricClassLoader;
  }

  /**
   * @deprecated use {@link org.robolectric.Robolectric.Reflection#setFinalStaticField(Class, String, Object)}
   */
  public static void setStaticValue(Class<?> clazz, String fieldName, Object value) {
    Robolectric.Reflection.setFinalStaticField(clazz, fieldName, value);
  }

  public ClassHandler getCurrentClassHandler() {
    return currentClassHandler;
  }

  public void setCurrentClassHandler(ClassHandler currentClassHandler) {
    this.currentClassHandler = currentClassHandler;
  }

  public SdkConfig getSdkConfig() {
    return sdkConfig;
  }

  public interface Factory {
    public SdkEnvironment create();
  }
}
