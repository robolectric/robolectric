package org.robolectric.util.inject

import com.google.auto.service.AutoService
import com.google.common.truth.Truth.assertThat
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Named
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.util.inject.*
import javax.annotation.Priority

@RunWith(JUnit4::class)
class InjectorTest {

  private lateinit var builder: Injector.Builder
  private lateinit var injector: Injector
  private val pluginClasses: MutableList<Class<*>> = ArrayList()

  @Before
  @Throws(Exception::class)
  fun setUp() {
    builder = Injector.Builder()
    injector = builder.build()
  }

  @Test
  @Throws(Exception::class)
  fun whenImplSpecified_shouldProvideInstance() {
    injector = builder.bind(Thing::class.java, MyThing::class.java).build()
    assertThat(injector.getInstance(Thing::class.java)).isInstanceOf(MyThing::class.java)
  }

  @Test
  @Throws(Exception::class)
  fun whenImplSpecified_shouldUseSameInstance() {
    injector = builder.bind(Thing::class.java, MyThing::class.java).build()
    val thing = injector.getInstance(Thing::class.java)
    assertThat(injector.getInstance(Thing::class.java)).isSameInstanceAs(thing)
  }

  @Test
  @Throws(Exception::class)
  fun whenServiceSpecified_shouldProvideInstance() {
    assertThat(injector.getInstance(Thing::class.java))
        .isInstanceOf(ThingFromServiceConfig::class.java)
  }

  @Test
  @Throws(Exception::class)
  fun whenServiceSpecified_shouldUseSameInstance() {
    val thing = injector.getInstance(Thing::class.java)
    assertThat(injector.getInstance(Thing::class.java)).isSameInstanceAs(thing)
  }

  @Test
  @Throws(Exception::class)
  fun whenConcreteClassRequested_shouldProvideInstance() {
    assertThat(injector.getInstance(MyUmm::class.java)).isInstanceOf(MyUmm::class.java)
  }

  @Test
  @Throws(Exception::class)
  fun whenDefaultSpecified_shouldProvideInstance() {
    injector = builder.bindDefault(Umm::class.java, MyUmm::class.java).build()
    assertThat(injector.getInstance(Umm::class.java)).isInstanceOf(MyUmm::class.java)
  }

  @Test
  @Throws(Exception::class)
  fun whenDefaultSpecified_shouldUseSameInstance() {
    val thing = injector.getInstance(Thing::class.java)
    assertThat(injector.getInstance(Thing::class.java)).isSameInstanceAs(thing)
  }

  @Test
  @Throws(Exception::class)
  fun whenNoImplOrServiceOrDefaultSpecified_shouldThrow() {
    try {
      injector.getInstance(Umm::class.java)
      Assert.fail()
    } catch (e: InjectionException) {
      // ok
    }
  }

  @Test
  @Throws(Exception::class)
  fun registerDefaultService_providesFallbackImplOnlyIfNoServiceSpecified() {
    builder.bindDefault(Thing::class.java, MyThing::class.java)
    assertThat(injector.getInstance(Thing::class.java))
        .isInstanceOf(ThingFromServiceConfig::class.java)
    builder.bindDefault(Umm::class.java, MyUmm::class.java)
    assertThat(injector.getInstance(Thing::class.java))
        .isInstanceOf(ThingFromServiceConfig::class.java)
  }

  @Test
  @Throws(Exception::class)
  fun shouldPreferSingularPublicConstructorAnnotatedInject() {
    injector =
        builder
            .bind(Thing::class.java, MyThing::class.java)
            .bind(Umm::class.java, MyUmm::class.java)
            .build()
    val umm = injector.getInstance(Umm::class.java)
    assertThat(umm).isNotNull()
    assertThat(umm).isInstanceOf(MyUmm::class.java)
    val myUmm = umm as MyUmm
    assertThat(myUmm.thing).isNotNull()
    assertThat(myUmm.thing).isInstanceOf(MyThing::class.java)
    assertThat(myUmm.thing).isSameInstanceAs(injector.getInstance(Thing::class.java))
  }

  @Test
  @Throws(Exception::class)
  fun shouldAcceptSingularPublicConstructorWithoutInjectAnnotation() {
    injector =
        builder
            .bind(Thing::class.java, MyThing::class.java)
            .bind(Umm::class.java, MyUmmNoInject::class.java)
            .build()
    val umm = injector.getInstance(Umm::class.java)
    assertThat(umm).isNotNull()
    assertThat(umm).isInstanceOf(MyUmmNoInject::class.java)
    val myUmm = umm as MyUmmNoInject
    assertThat(myUmm.thing).isNotNull()
    assertThat(myUmm.thing).isInstanceOf(MyThing::class.java)
    assertThat(myUmm.thing).isSameInstanceAs(injector.getInstance(Thing::class.java))
  }

  @Test
  @Throws(Exception::class)
  fun whenArrayRequested_mayReturnMultiplePlugins() {
    val multiThings: Array<MultiThing> = injector.getInstance(Array<MultiThing>::class.java)

    // X comes first because it has a higher priority
    assertThat(classesOf(multiThings))
        .containsExactly(MultiThingX::class.java, MultiThingA::class.java)
        .inOrder()
  }

  @Test
  @Throws(Exception::class)
  fun whenCollectionRequested_mayReturnMultiplePlugins() {
    val it = injector.getInstance(ThingRequiringMultiThings::class.java)

    // X comes first because it has a higher priority
    assertThat(classesOf(it.multiThings))
        .containsExactly(MultiThingX::class.java, MultiThingA::class.java)
        .inOrder()
  }

  @Test
  @Throws(Exception::class)
  fun whenListRequested_itIsUnmodifiable() {
    val it = injector.getInstance(ThingRequiringMultiThings::class.java)
    try {
      it.multiThings.clear()
      Assert.fail()
    } catch (e: Exception) {
      assertThat(e).isInstanceOf(UnsupportedOperationException::class.java)
    }
  }

  @Test
  @Throws(Exception::class)
  fun autoFactory_factoryMethodsCreateNewInstances() {
    injector = builder.bind(Umm::class.java, MyUmm::class.java).build()
    val factory = injector.getInstance(FooFactory::class.java)
    val chauncey = factory.create("Chauncey")
    assertThat(chauncey.name).isEqualTo("Chauncey")
    val anotherChauncey = factory.create("Chauncey")
    assertThat(anotherChauncey).isNotSameInstanceAs(chauncey)
  }

  @Test
  @Throws(Exception::class)
  fun autoFactory_injectedValuesComeFromSuperInjector() {
    injector = builder.bind(Umm::class.java, MyUmm::class.java).build()
    val factory = injector.getInstance(FooFactory::class.java)
    val chauncey = factory.create("Chauncey")
    assertThat(chauncey.thing).isSameInstanceAs(injector.getInstance(Thing::class.java))
  }

  @Test
  @Throws(Exception::class)
  fun whenFactoryRequested_createsInjectedFactory() {
    injector = builder.bind(Umm::class.java, MyUmm::class.java).build()
    val factory = injector.getInstance(FooFactory::class.java)
    val chauncey = factory.create("Chauncey")
    assertThat(chauncey.name).isEqualTo("Chauncey")
    val anotherChauncey = factory.create("Chauncey")
    assertThat(anotherChauncey).isNotSameInstanceAs(chauncey)
    assertThat(chauncey.thing).isSameInstanceAs(injector.getInstance(Thing::class.java))
  }

  @Test
  @Throws(Exception::class)
  fun scopedInjector_shouldCheckParentBeforeProvidingDefault() {
    injector = builder.build()
    val subInjector = Injector.Builder(injector).build()
    val subUmm = subInjector.getInstance(MyUmm::class.java)
    assertThat(injector.getInstance(MyUmm::class.java)).isSameInstanceAs(subUmm)
  }

  @Test
  @Throws(Exception::class)
  fun shouldInjectByNamedKeys() {
    injector =
        builder
            .bind(Injector.Key(String::class.java, "namedThing"), "named value")
            .bind(String::class.java, "unnamed value")
            .build()
    val namedParams = injector.getInstance(NamedParams::class.java)
    assertThat(namedParams.withName).isEqualTo("named value")
    assertThat(namedParams.withoutName).isEqualTo("unnamed value")
  }

  @Test
  @Throws(Exception::class)
  fun shouldPreferPluginsOverConcreteClass() {
    val pluginFinder = PluginFinder(MyServiceFinderAdapter(pluginClasses))
    val injector = Injector.Builder(null, pluginFinder).build()
    pluginClasses.add(SubclassOfConcreteThing::class.java)
    val instance = injector.getInstance(ConcreteThing::class.java)
    assertThat(instance.javaClass).isEqualTo(SubclassOfConcreteThing::class.java)
  }

  @Test
  @Throws(Exception::class)
  fun subInjectorIsUsedForResolvingTransitiveDependencies() {
    val sandboxManager = injector.getInstance(FakeSandboxManager::class.java)
    val runtimeSdk = FakeSdk("runtime")
    val compileSdk = FakeSdk("compile")
    val sandbox = sandboxManager.getSandbox(runtimeSdk, compileSdk)
    assertThat(sandbox.runtimeSdk).isSameInstanceAs(runtimeSdk)
    assertThat(sandbox.compileSdk).isSameInstanceAs(compileSdk)
  }

  @Test
  @Ignore("todo")
  @Throws(Exception::class)
  fun objectsCreatedByFactoryShareTransitiveDependencies() {
    val sandboxManager = injector.getInstance(FakeSandboxManager::class.java)
    val runtimeSdk = FakeSdk("runtime")
    val compileASdk = FakeSdk("compileA")
    val compileBSdk = FakeSdk("compileB")
    val sandboxA = sandboxManager.getSandbox(runtimeSdk, compileASdk)
    val sandboxB = sandboxManager.getSandbox(runtimeSdk, compileBSdk)
    assertThat(sandboxA.sandboxClassLoader).isSameInstanceAs(sandboxB.sandboxClassLoader)
  }

  @Test
  @Throws(Exception::class)
  fun shouldProvideDecentErrorMessages() {
    val sandboxManager = injector.getInstance(FakeSandboxManager::class.java)
    var actualException: Exception? = null
    try {
      sandboxManager.brokenGetSandbox()
      Assert.fail()
    } catch (e: Exception) {
      actualException = e
    }
    assertThat(actualException?.message)
        .contains("Failed to resolve dependency: FakeSandbox/FakeSdk/String")
  }

  @Test
  @Ignore("todo")
  @Throws(Exception::class)
  fun shouldOnlyAttemptToResolveTypesKnownToClassLoader() {}

  /////////////////////////////
  private fun classesOf(items: Array<*>): List<Class<*>?> {
    return classesOf(listOf(*items))
  }

  private fun classesOf(items: List<*>): List<Class<*>?> {
    return items
        .stream()
        .map { obj: Any? -> obj?.javaClass?.javaClass }
        .collect(Collectors.toList())
  }

  class MyThing : Thing

  open class ConcreteThing

  class SubclassOfConcreteThing : ConcreteThing()

  /** Class for test. */
  @AutoService(Thing::class) class ThingFromServiceConfig : Thing

  class MyUmm : Umm {
    internal val thing: Thing?

    @Inject
    constructor(thing: Thing?) {
      this.thing = thing
    }

    @SuppressWarnings("unused")
    constructor(thingz: String?) {
      thing = null
    }
  }

  /** Class for test. */
  class MyUmmNoInject(internal val thing: Thing) : Umm

  interface MultiThing

  /** Class for test. */
  @Priority(-5) @AutoService(MultiThing::class)
  class MultiThingA : MultiThing

  /** Class for test. */
  @AutoService(MultiThing::class)
  class MultiThingX : MultiThing

  /** Class for test. */
  class ThingRequiringMultiThings(var multiThings: MutableList<MultiThing>)

  internal class Foo
  internal constructor(internal val thing: Thing, private val umm: Umm, internal val name: String)

  @AutoFactory
  internal interface FooFactory {
    fun create(name: String?): Foo
  }

  internal class NamedParams(
      private val thing: String,
      @param:Named("namedThing") internal val withName: String,
      internal val withoutName: String
  )

  internal class FakeSdk(private val name: String)

  internal class FakeSandbox(
    @param:Named("runtimeSdk") internal val runtimeSdk: FakeSdk,
    @param:Named("compileSdk") internal val compileSdk: FakeSdk,
    internal val sandboxClassLoader: FakeSandboxClassLoader
  )

  internal class FakeSandboxClassLoader(@param:Named("runtimeSdk") private val runtimeSdk: FakeSdk)

  internal class FakeSandboxManager(private var sandboxFactory: FakeSandboxFactory) {

    fun getSandbox(runtimeSdk: FakeSdk?, compileSdk: FakeSdk?): FakeSandbox {
      return sandboxFactory.createSandbox(runtimeSdk, compileSdk)
    }

    fun brokenGetSandbox(): FakeSandbox {
      return sandboxFactory.createSandbox()
    }
  }

  @AutoFactory
  internal interface FakeSandboxFactory {
    fun createSandbox(
      @Named("runtimeSdk") runtimeSdk: FakeSdk?,
      @Named("compileSdk") compileSdk: FakeSdk?
    ): FakeSandbox

    fun createSandbox(): FakeSandbox
  }
}
