package org.robolectric.util.inject;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InjectorTest {

  private Injector.Builder builder;
  private Injector injector;
  private final List<Class<?>> pluginClasses = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    builder = new Injector.Builder();
    injector = builder.build();
  }

  @Test
  public void whenImplSpecified_shouldProvideInstance() throws Exception {
    injector = builder.bind(Thing.class, MyThing.class).build();

    assertThat(injector.getInstance(Thing.class))
        .isInstanceOf(MyThing.class);
  }

  @Test
  public void whenImplSpecified_shouldUseSameInstance() throws Exception {
    injector = builder.bind(Thing.class, MyThing.class).build();

    Thing thing = injector.getInstance(Thing.class);
    assertThat(injector.getInstance(Thing.class)).isSameInstanceAs(thing);
  }

  @Test
  public void whenServiceSpecified_shouldProvideInstance() throws Exception {
    assertThat(injector.getInstance(Thing.class))
        .isInstanceOf(ThingFromServiceConfig.class);
  }

  @Test
  public void whenServiceSpecified_shouldUseSameInstance() throws Exception {
    Thing thing = injector.getInstance(Thing.class);
    assertThat(injector.getInstance(Thing.class)).isSameInstanceAs(thing);
  }

  @Test
  public void whenConcreteClassRequested_shouldProvideInstance() throws Exception {
    assertThat(injector.getInstance(MyUmm.class))
        .isInstanceOf(MyUmm.class);
  }

  @Test
  public void whenDefaultSpecified_shouldProvideInstance() throws Exception {
    injector = builder.bindDefault(Umm.class, MyUmm.class).build();

    assertThat(injector.getInstance(Umm.class))
        .isInstanceOf(MyUmm.class);
  }

  @Test
  public void whenDefaultSpecified_shouldUseSameInstance() throws Exception {
    Thing thing = injector.getInstance(Thing.class);
    assertThat(injector.getInstance(Thing.class)).isSameInstanceAs(thing);
  }

  @Test
  public void whenNoImplOrServiceOrDefaultSpecified_shouldThrow() throws Exception {
    try {
      injector.getInstance(Umm.class);
      fail();
    } catch (InjectionException e) {
      // ok
    }
  }

  @Test
  public void registerDefaultService_providesFallbackImplOnlyIfNoServiceSpecified()
      throws Exception {
    builder.bindDefault(Thing.class, MyThing.class);

    assertThat(injector.getInstance(Thing.class))
        .isInstanceOf(ThingFromServiceConfig.class);

    builder.bindDefault(Umm.class, MyUmm.class);
    assertThat(injector.getInstance(Thing.class))
        .isInstanceOf(ThingFromServiceConfig.class);
  }

  @Test
  public void shouldPreferSingularPublicConstructorAnnotatedInject() throws Exception {
    injector = builder
        .bind(Thing.class, MyThing.class)
        .bind(Umm.class, MyUmm.class)
        .build();

    Umm umm = injector.getInstance(Umm.class);
    assertThat(umm).isNotNull();
    assertThat(umm).isInstanceOf(MyUmm.class);

    MyUmm myUmm = (MyUmm) umm;
    assertThat(myUmm.thing).isNotNull();
    assertThat(myUmm.thing).isInstanceOf(MyThing.class);

    assertThat(myUmm.thing).isSameInstanceAs(injector.getInstance(Thing.class));
  }

  @Test
  public void shouldAcceptSingularPublicConstructorWithoutInjectAnnotation() throws Exception {
    injector = builder
        .bind(Thing.class, MyThing.class)
        .bind(Umm.class, MyUmmNoInject.class)
        .build();

    Umm umm = injector.getInstance(Umm.class);
    assertThat(umm).isNotNull();
    assertThat(umm).isInstanceOf(MyUmmNoInject.class);

    MyUmmNoInject myUmm = (MyUmmNoInject) umm;
    assertThat(myUmm.thing).isNotNull();
    assertThat(myUmm.thing).isInstanceOf(MyThing.class);

    assertThat(myUmm.thing).isSameInstanceAs(injector.getInstance(Thing.class));
  }

  @Test
  public void whenArrayRequested_mayReturnMultiplePlugins() throws Exception {
    MultiThing[] multiThings = injector.getInstance(MultiThing[].class);

    // X comes first because it has a higher priority
    assertThat(classesOf(multiThings))
        .containsExactly(MultiThingX.class, MultiThingA.class).inOrder();
  }

  @Test
  public void whenCollectionRequested_mayReturnMultiplePlugins() throws Exception {
    ThingRequiringMultiThings it = injector.getInstance(ThingRequiringMultiThings.class);

    // X comes first because it has a higher priority
    assertThat(classesOf(it.multiThings))
        .containsExactly(MultiThingX.class, MultiThingA.class).inOrder();
  }

  @Test
  public void whenListRequested_itIsUnmodifiable() throws Exception {
    ThingRequiringMultiThings it = injector.getInstance(ThingRequiringMultiThings.class);

    try {
      it.multiThings.clear();
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Test public void autoFactory_factoryMethodsCreateNewInstances() throws Exception {
    injector = builder.bind(Umm.class, MyUmm.class).build();
    FooFactory factory = injector.getInstance(FooFactory.class);
    Foo chauncey = factory.create("Chauncey");
    assertThat(chauncey.name).isEqualTo("Chauncey");

    Foo anotherChauncey = factory.create("Chauncey");
    assertThat(anotherChauncey).isNotSameInstanceAs(chauncey);
  }

  @Test public void autoFactory_injectedValuesComeFromSuperInjector() throws Exception {
    injector = builder.bind(Umm.class, MyUmm.class).build();
    FooFactory factory = injector.getInstance(FooFactory.class);
    Foo chauncey = factory.create("Chauncey");
    assertThat(chauncey.thing).isSameInstanceAs(injector.getInstance(Thing.class));
  }

  @Test public void whenFactoryRequested_createsInjectedFactory() throws Exception {
    injector = builder.bind(Umm.class, MyUmm.class).build();
    FooFactory factory = injector.getInstance(FooFactory.class);
    Foo chauncey = factory.create("Chauncey");
    assertThat(chauncey.name).isEqualTo("Chauncey");

    Foo anotherChauncey = factory.create("Chauncey");
    assertThat(anotherChauncey).isNotSameInstanceAs(chauncey);

    assertThat(chauncey.thing).isSameInstanceAs(injector.getInstance(Thing.class));
  }

  @Test public void scopedInjector_shouldCheckParentBeforeProvidingDefault() throws Exception {
    injector = builder.build();
    Injector subInjector = new Injector.Builder(injector).build();

    MyUmm subUmm = subInjector.getInstance(MyUmm.class);
    assertThat(injector.getInstance(MyUmm.class)).isSameInstanceAs(subUmm);
  }

  @Test public void shouldInjectByNamedKeys() throws Exception {
    injector = builder
        .bind(new Injector.Key<>(String.class, "namedThing"), "named value")
        .bind(String.class, "unnamed value")
        .build();
    NamedParams namedParams = injector.getInstance(NamedParams.class);
    assertThat(namedParams.withName).isEqualTo("named value");
    assertThat(namedParams.withoutName).isEqualTo("unnamed value");
  }

  @Test public void shouldPreferPluginsOverConcreteClass() throws Exception {
    PluginFinder pluginFinder = new PluginFinder(new MyServiceFinderAdapter(pluginClasses));
    Injector injector = new Injector.Builder(null, pluginFinder).build();
    pluginClasses.add(SubclassOfConcreteThing.class);
    ConcreteThing instance = injector.getInstance(ConcreteThing.class);
    assertThat(instance.getClass()).isEqualTo(SubclassOfConcreteThing.class);
  }

  @Test
  public void subInjectorIsUsedForResolvingTransitiveDependencies() throws Exception {
    FakeSandboxManager sandboxManager = injector.getInstance(FakeSandboxManager.class);
    FakeSdk runtimeSdk = new FakeSdk("runtime");
    FakeSdk compileSdk = new FakeSdk("compile");
    FakeSandbox sandbox = sandboxManager.getSandbox(runtimeSdk, compileSdk);
    assertThat(sandbox.runtimeSdk).isSameInstanceAs(runtimeSdk);
    assertThat(sandbox.compileSdk).isSameInstanceAs(compileSdk);
  }

  @Test @Ignore("todo")
  public void objectsCreatedByFactoryShareTransitiveDependencies() throws Exception {
    FakeSandboxManager sandboxManager = injector.getInstance(FakeSandboxManager.class);
    FakeSdk runtimeSdk = new FakeSdk("runtime");
    FakeSdk compileASdk = new FakeSdk("compileA");
    FakeSdk compileBSdk = new FakeSdk("compileB");
    FakeSandbox sandboxA = sandboxManager.getSandbox(runtimeSdk, compileASdk);
    FakeSandbox sandboxB = sandboxManager.getSandbox(runtimeSdk, compileBSdk);
    assertThat(sandboxA.sandboxClassLoader).isSameInstanceAs(sandboxB.sandboxClassLoader);
  }

  @Test
  public void shouldProvideDecentErrorMessages() throws Exception {
    FakeSandboxManager sandboxManager = injector.getInstance(FakeSandboxManager.class);
    Exception actualException = null;
    try {
      sandboxManager.brokenGetSandbox();
      fail();
    } catch (Exception e) {
      actualException = e;
    }
    assertThat(actualException.getMessage())
        .contains("Failed to resolve dependency: FakeSandbox/FakeSdk/String");
  }

  @Test @Ignore("todo")
  public void shouldOnlyAttemptToResolveTypesKnownToClassLoader() throws Exception {
  }

  /////////////////////////////

  private List<? extends Class<?>> classesOf(Object[] items) {
    return classesOf(Arrays.asList(items));
  }

  private List<? extends Class<?>> classesOf(List<?> items) {
    return items.stream().map(Object::getClass).collect(Collectors.toList());
  }

  /** A thing. */
  public interface Thing {
  }

  public static class MyThing implements Thing {
  }

  public static class ConcreteThing {
  }

  public static class SubclassOfConcreteThing extends ConcreteThing {
  }

  /** Class for test. */
  @AutoService(Thing.class)
  public static class ThingFromServiceConfig implements Thing {
  }

  private interface Umm {

  }

  public static class MyUmm implements Umm {

    private final Thing thing;

    @Inject
    public MyUmm(Thing thing) {
      this.thing = thing;
    }

    @SuppressWarnings("unused")
    public MyUmm(String thingz) {
      this.thing = null;
    }
  }

  /** Class for test. */
  public static class MyUmmNoInject implements Umm {

    private final Thing thing;

    public MyUmmNoInject(Thing thing) {
      this.thing = thing;
    }
  }

  private interface MultiThing {

  }

  /** Class for test. */
  @Priority(-5)
  @AutoService(MultiThing.class)
  public static class MultiThingA implements MultiThing {
  }

  /** Class for test. */
  @AutoService(MultiThing.class)
  public static class MultiThingX implements MultiThing {
  }

  /** Class for test. */
  public static class ThingRequiringMultiThings {

    private List<MultiThing> multiThings;

    public ThingRequiringMultiThings(List<MultiThing> multiThings) {
      this.multiThings = multiThings;
    }
  }

  static class Foo {

    private final Thing thing;
    private final Umm umm;
    private final String name;

    public Foo(Thing thing, Umm umm, String name) {
      this.thing = thing;
      this.umm = umm;
      this.name = name;
    }
  }

  @AutoFactory
  interface FooFactory {
    Foo create(String name);
  }

  static class NamedParams {

    private final Thing thing;
    private final String withName;
    private final String withoutName;

    public NamedParams(Thing thing, @Named("namedThing") String withName, String withoutName) {
      this.thing = thing;
      this.withName = withName;
      this.withoutName = withoutName;
    }
  }

  static class FakeSdk {
    private final String name;

    public FakeSdk(String name) {
      this.name = name;
    }
  }

  static class FakeSandbox {

    private final FakeSdk runtimeSdk;
    private final FakeSdk compileSdk;
    private final FakeSandboxClassLoader sandboxClassLoader;

    public FakeSandbox(
        @Named("runtimeSdk") FakeSdk runtimeSdk,
        @Named("compileSdk") FakeSdk compileSdk,
        FakeSandboxClassLoader sandboxClassLoader) {
      this.runtimeSdk = runtimeSdk;
      this.compileSdk = compileSdk;
      this.sandboxClassLoader = sandboxClassLoader;
    }
  }

  static class FakeSandboxClassLoader {
    private final FakeSdk runtimeSdk;

    public FakeSandboxClassLoader(@Named("runtimeSdk") FakeSdk runtimeSdk) {
      this.runtimeSdk = runtimeSdk;
    }
  }

  static class FakeSandboxManager {

    private final FakeSandboxFactory sandboxFactory;

    public FakeSandboxManager(FakeSandboxFactory sandboxFactory) {
      this.sandboxFactory = sandboxFactory;
    }

    public FakeSandbox getSandbox(FakeSdk runtimeSdk, FakeSdk compileSdk) {
      return sandboxFactory.createSandbox(runtimeSdk, compileSdk);
    }

    public FakeSandbox brokenGetSandbox() {
      return sandboxFactory.createSandbox();
    }
  }

  @AutoFactory
  private interface FakeSandboxFactory {
    FakeSandbox createSandbox(@Named("runtimeSdk") FakeSdk runtimeSdk,
        @Named("compileSdk") FakeSdk compileSdk);
    FakeSandbox createSandbox();
  }
}