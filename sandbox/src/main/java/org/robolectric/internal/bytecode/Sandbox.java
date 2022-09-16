package org.robolectric.internal.bytecode;

import static org.robolectric.util.ReflectionHelpers.newInstance;
import static org.robolectric.util.ReflectionHelpers.setStaticField;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Util;

public class Sandbox {
  private final SandboxClassLoader sandboxClassLoader;
  private final ExecutorService executorService;
  private ShadowInvalidator shadowInvalidator;
  public ClassHandler classHandler; // todo not public
  private ShadowMap shadowMap = ShadowMap.EMPTY;

  public Sandbox(
      InstrumentationConfiguration config,
      ResourceProvider resourceProvider,
      ClassInstrumentor classInstrumentor) {
    this(new SandboxClassLoader(config, resourceProvider, classInstrumentor));
  }

  @Inject
  public Sandbox(SandboxClassLoader sandboxClassLoader) {
    this.sandboxClassLoader = sandboxClassLoader;
    executorService = Executors.newSingleThreadExecutor(mainThreadFactory());
  }

  protected ThreadFactory mainThreadFactory() {
    return Thread::new;
  }

  public <T> Class<T> bootstrappedClass(Class<?> clazz) {
    try {
      return (Class<T>) sandboxClassLoader.loadClass(clazz.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

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
    ShadowMap oldShadowMap = this.shadowMap;
    this.shadowMap = shadowMap;
    Set<String> invalidatedClasses = new HashSet<>();
    invalidatedClasses.addAll(shadowMap.getInvalidatedClasses(oldShadowMap));
    invalidatedClasses.addAll(getModeInvalidatedClasses());
    getShadowInvalidator().invalidateClasses(invalidatedClasses);
    clearModeInvalidatedClasses();
  }

  protected Set<String> getModeInvalidatedClasses() {
    return Collections.emptySet();
  }

  protected void clearModeInvalidatedClasses() {}

  public void configure(ClassHandler classHandler, Interceptors interceptors) {
    this.classHandler = classHandler;

    ClassLoader robolectricClassLoader = getRobolectricClassLoader();
    Class<?> robolectricInternalsClass = bootstrappedClass(RobolectricInternals.class);
    ShadowInvalidator invalidator = getShadowInvalidator();
    setStaticField(robolectricInternalsClass, "shadowInvalidator", invalidator);

    setStaticField(robolectricInternalsClass, "classHandler", classHandler);
    setStaticField(robolectricInternalsClass, "classLoader", robolectricClassLoader);

    Class<?> invokeDynamicSupportClass = bootstrappedClass(InvokeDynamicSupport.class);
    setStaticField(invokeDynamicSupportClass, "INTERCEPTORS", interceptors);

    Class<?> shadowClass = bootstrappedClass(Shadow.class);
    setStaticField(shadowClass, "SHADOW_IMPL", newInstance(bootstrappedClass(ShadowImpl.class)));
  }

  public void runOnMainThread(Runnable runnable) {
    runOnMainThread(
        () -> {
          runnable.run();
          return null;
        });
  }

  public <T> T runOnMainThread(Callable<T> callable) {
    Future<T> future = executorService.submit(callable);
    try {
      return future.get();
    } catch (InterruptedException e) {
      future.cancel(true);
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw Util.sneakyThrow(e.getCause());
    }
  }
}
