package org.robolectric.kotest.internal

import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import java.lang.reflect.InvocationTargetException
import java.util.IdentityHashMap
import java.util.concurrent.Callable
import kotlin.coroutines.ContinuationInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment
import org.robolectric.runner.common.RobolectricSession

/**
 * Creates specs in Android environments, one for each spec that Kotest asks for and each SDK, and
 * runs them there. An environment lives as long as the spec that Kotest got for it, so the tests
 * that share a spec instance share its Android state.
 */
@OptIn(ExperimentalRunnerApi::class)
internal object SpecEnvironments {
  private val lock = Any()

  // Guarded by the lock: the specs that Kotest got, with the instances that they stand in for.
  private val standIns = IdentityHashMap<Spec, StandIn>()

  // Guarded by the lock: the event loops of the environments that run a spec. What is dispatched
  // to one runs on the main thread of its environment, which waits in it while the spec runs.
  private val mainThreads = IdentityHashMap<RobolectricEnvironment, ContinuationInterceptor>()

  /**
   * Creates the spec for Kotest: one that stands in for an instance of the spec class in an Android
   * environment for each SDK that is selected for it, or one that says why the spec doesn't run if
   * none is. [runSpec] closes the environments.
   */
  fun create(specClass: Class<out Spec>): Spec {
    val session = Sessions.acquire()
    val created = mutableListOf<SpecInstance>()
    try {
      session.plan(specClass, null).forEach { created.add(open(session, it, specClass)) }
      val standIn = if (created.isEmpty()) null else StandIns.create(specClass, created)
      val spec = standIn?.spec ?: NoSdkSpec(specClass)
      Sessions.closeAfterProjectOf(spec)
      if (standIn == null) {
        Sessions.release()
      } else {
        synchronized(lock) { standIns[spec] = standIn }
      }
      return spec
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      created.forEach { close(it) }
      Sessions.release()
      throw e
    }
  }

  /**
   * Runs a spec with the main threads of its environments in event loops, with what the coroutine
   * context of the caller holds apart from its dispatcher, and closes the environments once the
   * spec is done. Kotest also asks about the specs that another extension created.
   */
  suspend fun runSpec(spec: Spec, execute: suspend () -> Unit) {
    val standIn = synchronized(lock) { standIns[spec] }
    val type = spec.javaClass
    check(standIn != null || !ObjectSpecs.isObject(type) || !ObjectSpecs.appliesExtension(type)) {
      ObjectSpecs.hint(type)
    }
    val environments = standIn?.members?.map { it.instance.environment }
    // If the extension is registered twice, it runs the spec already.
    if (environments == null || synchronized(lock) { environments.first() in mainThreads }) {
      return execute()
    }
    try {
      inEventLoops(environments) {
        execute()
        finish(standIn.members)
      }
    } finally {
      close(spec)
    }
  }

  /**
   * Runs a test on the main thread of the environment of the instance that it comes from, which it
   * has left if Kotest runs tests on a dispatcher of their own, and adds Robolectric's hints to its
   * failure.
   */
  suspend fun runTest(testCase: TestCase, execute: suspend (TestCase) -> TestResult): TestResult {
    val member =
      synchronized(lock) { standIns[testCase.spec] }?.memberOf(testCase) ?: return execute(testCase)
    val environment = member.instance.environment
    return onMainThread(environment) {
      MainDispatchers.use(member.instance.mainDispatcher)
      member.start()
      execute(testCase).also { result -> result.errorOrNull?.let { diagnose(environment, it) } }
    }
  }

  /** Lets each instance do what it does after its last test, on its main thread. */
  private suspend fun finish(members: List<Member>) {
    val failures = members.mapNotNull { member ->
      runCatching {
        onMainThread(member.instance.environment) {
          MainDispatchers.use(member.instance.mainDispatcher)
          member.finish()
        }
      }
        .exceptionOrNull()
    }
    failures.drop(1).forEach { failures.first().addSuppressed(it) }
    failures.firstOrNull()?.let { throw it }
  }

  /**
   * Runs the block on the main thread of the environment, in place if it is called from there. The
   * main thread is busy with the spec of the environment, so the block is dispatched to it.
   */
  suspend fun <T> onMainThread(environment: RobolectricEnvironment, block: suspend () -> T): T {
    val mainThread = synchronized(lock) { mainThreads[environment] }
    return if (mainThread == null) block() else withContext(mainThread) { block() }
  }

  /**
   * Runs the block while the main thread of each environment waits in an event loop. The block
   * itself runs in the loop of the last one.
   */
  private suspend fun inEventLoops(
    environments: List<RobolectricEnvironment>,
    block: suspend () -> Unit,
  ) {
    val environment = environments.firstOrNull() ?: return block()
    // The thread that waits for the main thread is not one that other coroutines need.
    withContext(Dispatchers.IO) {
      val context = coroutineContext.minusKey(ContinuationInterceptor)
      environment.run(
        Callable {
          runBlocking(context) {
            val eventLoop = checkNotNull(coroutineContext[ContinuationInterceptor])
            synchronized(lock) { mainThreads[environment] = eventLoop }
            try {
              inEventLoops(environments.drop(1), block)
            } finally {
              synchronized(lock) { mainThreads.remove(environment) }
            }
          }
        }
      )
    }
  }

  /** Adds what the environment knows about a failure of a test to it, as the test runner does. */
  private fun diagnose(environment: RobolectricEnvironment, failure: Throwable) {
    try {
      environment.diagnoseFailure(failure)
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      failure.addSuppressed(e)
    }
  }

  /** Closes the environments that the instances behind the spec were created in. */
  private fun close(spec: Spec) {
    val closed = synchronized(lock) { standIns.remove(spec) } ?: return
    try {
      closed.members.forEach { close(it.instance) }
    } finally {
      Sessions.release()
    }
  }

  private fun close(instance: SpecInstance) {
    MainDispatchers.release(instance.mainDispatcher)
    instance.environment.close()
  }

  /** Opens an environment, and creates the spec in it, from its class as the sandbox loads it. */
  private fun open(
    session: RobolectricSession,
    configuration: Configuration,
    specClass: Class<out Spec>,
  ): SpecInstance {
    val environment = session.open(configuration)
    var mainDispatcher: MainCoroutineDispatcher? = null
    try {
      mainDispatcher = MainDispatchers.create(environment)
      // The body of a spec can use the main dispatcher too.
      MainDispatchers.use(mainDispatcher)
      val instance =
        environment.run(
          Callable {
            val constructor =
              environment.loadClass(specClass).declaredConstructors.firstOrNull {
                it.parameterCount == 0
              }
                ?: error(
                  "${specClass.name} can't be created in the Android environment: it needs a " +
                    "constructor without parameters"
                )
            constructor.isAccessible = true
            try {
              constructor.newInstance() as Spec
            } catch (e: InvocationTargetException) {
              throw e.targetException
            }
          }
        )
      return SpecInstance(instance, environment, mainDispatcher)
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      MainDispatchers.release(mainDispatcher)
      environment.close()
      throw e
    }
  }
}
