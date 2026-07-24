package org.robolectric.runner.common

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.util.concurrent.Callable
import org.robolectric.annotation.Config

/**
 * Framework-free entry point to the Robolectric Android environment.
 *
 * Boots a sandboxed Android environment without any test framework, for consumers like REPLs,
 * Compose-preview renderers, or other tools that need Android APIs on the JVM outside a test run:
 * ```kotlin
 * RobolectricRuntime.launch { sdk = 34 }.use { runtime ->
 *   val sdkInt = runtime.execute(Callable {
 *     Class.forName("android.os.Build${'$'}VERSION", true, Thread.currentThread().contextClassLoader)
 *       .getField("SDK_INT").getInt(null)
 *   })
 * }
 * ```
 *
 * ## Classloader contract
 *
 * Android classes only exist inside the sandbox classloader. A lambda passed to [execute] runs
 * as-is on the sandbox main thread with the thread context classloader set to the sandbox loader —
 * reflection-driven code (`Class.forName` against the context classloader) works directly. Code
 * that references `android.*` types **in its own bytecode** must instead be loaded through the
 * sandbox loader: use [executeLoaded], load classes via [sandboxClassLoader] yourself, or register
 * a [FrameworkClassLoadingBridge] for generated/REPL bytecode.
 *
 * One environment per instance; [close] tears it down. Multiple runtimes (e.g. different SDKs) may
 * coexist, each holding its own sandbox and classloader.
 */
@ExperimentalRunnerApi
class RobolectricRuntime
private constructor(
  private val context: SandboxLifecycleManager.SandboxContext,
  private val environment: RobolectricEnvironment,
) : AutoCloseable {

  @Volatile private var closed = false

  /** The sandbox classloader, for callers that manage their own class loading. */
  val sandboxClassLoader: ClassLoader
    get() = environment.getSandboxClassLoader()

  /**
   * Runs [block] on the sandbox main thread inside the Android environment, with the thread context
   * classloader set to the sandbox classloader, and returns its value.
   */
  fun <T> execute(block: Callable<T>): T {
    check(!closed) { "RobolectricRuntime is closed" }
    try {
      return context.sandbox.runOnMainThread(Callable { environment.access { block.call() } })
    } catch (e: InvocationTargetException) {
      throw e.targetException ?: e
    } catch (e: NoClassDefFoundError) {
      throw rewrapAndroidClassloadingError(e)
    }
  }

  /**
   * Loads [className] through the sandbox classloader, invokes its [methodName] with [args]
   * (instantiating the class via its no-arg constructor for instance methods), and returns the
   * result. This is the path for code whose bytecode references `android.*` types directly.
   */
  fun executeLoaded(className: String, methodName: String, vararg args: Any?): Any? =
    execute(
      Callable {
        val loaded = Class.forName(className, true, sandboxClassLoader)
        val candidates = loaded.declaredMethods.filter { it.name == methodName }
        require(candidates.isNotEmpty()) { "No method '$methodName' found on $className" }
        require(candidates.size == 1) {
          "Multiple methods named '$methodName' on $className; executeLoaded requires a unique name"
        }
        val method = candidates.single()
        method.isAccessible = true
        val target =
          if (Modifier.isStatic(method.modifiers)) {
            null
          } else {
            loaded.getDeclaredConstructor().also { it.isAccessible = true }.newInstance()
          }
        try {
          method.invoke(target, *args)
        } catch (e: InvocationTargetException) {
          throw e.targetException ?: e
        }
      }
    )

  /** Tears down the Android environment and releases the sandbox. Idempotent. */
  override fun close() {
    if (closed) {
      return
    }
    closed = true
    context.sandbox.runOnMainThread {
      environment.access { environment.tearDownApplicationState() }
    }
  }

  private fun rewrapAndroidClassloadingError(e: NoClassDefFoundError): Throwable {
    val missing = e.message.orEmpty()
    return if (missing.startsWith("android/") || missing.startsWith("android.")) {
      IllegalStateException(
        "Android class '$missing' is not visible on the caller's classloader. Code whose " +
          "bytecode references android.* types must be loaded through the sandbox " +
          "classloader — use executeLoaded(...) or load it via sandboxClassLoader.",
        e,
      )
    } else {
      e
    }
  }

  /** Configuration for [launch]. Unset values fall back to Robolectric's defaults. */
  class Builder {
    /** Android API level to boot (e.g. 34). Unset selects Robolectric's default SDK. */
    var sdk: Int? = null

    /** Resource qualifiers (e.g. "en-rUS-w400dp-h800dp"). */
    var qualifiers: String? = null

    internal fun buildOverlay(): Config {
      val builder = Config.Builder()
      sdk?.let { builder.setSdk(it) }
      qualifiers?.let { builder.setQualifiers(it) }
      return builder.build()
    }
  }

  companion object {
    /**
     * Boots an Android environment with the given configuration. Blocking; the returned runtime
     * should be reused for many [execute] calls and closed when done.
     */
    @JvmStatic
    fun launch(spec: Builder.() -> Unit = {}): RobolectricRuntime {
      val overlay = Builder().apply(spec).buildOverlay()
      val dependencies = RobolectricDependencies.create()
      val lifecycleManager = SandboxLifecycleManager(dependencies)
      val context = lifecycleManager.createSandboxForConfig(overlay)
      val environment = lifecycleManager.getEnvironment(context)
      context.sandbox.runOnMainThread {
        environment.access { environment.setupApplicationState("RobolectricRuntime") }
      }
      return RobolectricRuntime(context, environment)
    }
  }
}
