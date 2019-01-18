package org.robolectric.util.inject;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.auto.service.AutoService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InjectorTest {

  private Injector injector;

  @Before
  public void setUp() throws Exception {
    injector = new Injector();
  }

  @Test
  public void whenImplSpecified_shouldProvideInstance() throws Exception {
    injector.register(Thing.class, MyThing.class);

    assertThat(injector.getInstance(Thing.class))
        .isInstanceOf(MyThing.class);
  }

  @Test
  public void whenImplSpecified_shouldUseSameInstance() throws Exception {
    injector.register(Thing.class, MyThing.class);

    Thing thing = injector.getInstance(Thing.class);
    assertThat(injector.getInstance(Thing.class))
        .isSameAs(thing);
  }

  @Test
  public void whenServiceSpecified_shouldProvideInstance() throws Exception {
    assertThat(injector.getInstance(Thing.class))
        .isInstanceOf(ThingFromServiceConfig.class);
  }

  @Test
  public void whenServiceSpecified_shouldUseSameInstance() throws Exception {
    Thing thing = injector.getInstance(Thing.class);
    assertThat(injector.getInstance(Thing.class))
        .isSameAs(thing);
  }

  @Test
  public void whenConcreteClassRequested_shouldProvideInstance() throws Exception {
    assertThat(injector.getInstance(MyUmm.class))
        .isInstanceOf(MyUmm.class);
  }

  @Test
  public void whenDefaultSpecified_shouldProvideInstance() throws Exception {
    injector.registerDefault(Umm.class, MyUmm.class);

    assertThat(injector.getInstance(Umm.class))
        .isInstanceOf(MyUmm.class);
  }

  @Test
  public void whenDefaultSpecified_shouldUseSameInstance() throws Exception {
    Thing thing = injector.getInstance(Thing.class);
    assertThat(injector.getInstance(Thing.class))
        .isSameAs(thing);
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
    injector.registerDefault(Thing.class, MyThing.class);

    assertThat(injector.getInstance(Thing.class))
        .isInstanceOf(ThingFromServiceConfig.class);

    injector.registerDefault(Umm.class, MyUmm.class);
    assertThat(injector.getInstance(Thing.class))
        .isInstanceOf(ThingFromServiceConfig.class);
  }

  @Test
  public void shouldPreferSingularPublicConstructorAnnotatedInject() throws Exception {
    injector.register(Thing.class, MyThing.class);
    injector.register(Umm.class, MyUmm.class);

    Umm umm = injector.getInstance(Umm.class);
    assertThat(umm).isNotNull();
    assertThat(umm).isInstanceOf(MyUmm.class);

    MyUmm myUmm = (MyUmm) umm;
    assertThat(myUmm.thing).isNotNull();
    assertThat(myUmm.thing).isInstanceOf(MyThing.class);

    assertThat(myUmm.thing).isSameAs(injector.getInstance(Thing.class));
  }

  @Test
  public void shouldAcceptSingularPublicConstructorWithoutInjectAnnotation() throws Exception {
    injector.register(Thing.class, MyThing.class);
    injector.register(Umm.class, MyUmmNoInject.class);

    Umm umm = injector.getInstance(Umm.class);
    assertThat(umm).isNotNull();
    assertThat(umm).isInstanceOf(MyUmmNoInject.class);

    MyUmmNoInject myUmm = (MyUmmNoInject) umm;
    assertThat(myUmm.thing).isNotNull();
    assertThat(myUmm.thing).isInstanceOf(MyThing.class);

    assertThat(myUmm.thing).isSameAs(injector.getInstance(Thing.class));
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

  @Test public void whenFactoryRequested_createsInjectedFactory() throws Exception {
    injector.register(Umm.class, MyUmm.class);
    FooFactory factory = injector.getInstance(FooFactory.class);
    Foo chauncey = factory.create("Chauncey");
    assertThat(chauncey.name).isEqualTo("Chauncey");

    Foo anotherChauncey = factory.create("Chauncey");
    assertThat(anotherChauncey).isNotSameAs(chauncey);
  }

  @Test public void shouldInjectByNamedKeys() throws Exception {
    injector
        .register(new Injector.Key<>(String.class, "namedThing"), "named value")
        .register(String.class, "unnamed value");
    NamedParams namedParams = injector.getInstance(NamedParams.class);
    assertThat(namedParams.withName).isEqualTo("named value");
    assertThat(namedParams.withoutName).isEqualTo("unnamed value");
  }
  /////////////////////////////

  private List<? extends Class<?>> classesOf(Object[] items) {
    return classesOf(Arrays.asList(items));
  }

  private List<? extends Class<?>> classesOf(List<?> items) {
    return items.stream().map(Object::getClass).collect(Collectors.toList());
  }

  interface Thing {

  }

  public static class MyThing implements Thing {

  }

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

  public static class MyUmmNoInject implements Umm {

    private final Thing thing;

    public MyUmmNoInject(Thing thing) {
      this.thing = thing;
    }
  }

  private interface MultiThing {

  }

  @Priority(-5)
  @AutoService(MultiThing.class)
  public static class MultiThingA implements MultiThing {
  }

  @AutoService(MultiThing.class)
  public static class MultiThingX implements MultiThing {
  }

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
}