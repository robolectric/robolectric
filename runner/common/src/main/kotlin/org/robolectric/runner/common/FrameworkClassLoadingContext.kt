package org.robolectric.runner.common

import org.robolectric.internal.bytecode.ClassBytesSource

internal object FrameworkClassLoadingContext {
  private val state = ThreadLocal<State?>()

  fun <T> withContext(
    bridge: FrameworkClassLoadingBridge,
    sourceClassLoader: ClassLoader?,
    block: () -> T,
  ): T {
    val prior = state.get()
    // Install a thread-local provider so SandboxClassLoader can request transformed class bytes
    // while tests execute inside the sandbox main thread.
    val priorProvider =
      ClassBytesSource.setProvider(
        ClassBytesSource.Provider { classResourceName ->
          val current = state.get() ?: return@Provider null
          current.bridge.openClassBytes(classResourceName, current.sourceClassLoader)
        }
      )
    state.set(State(bridge, sourceClassLoader))
    return try {
      block()
    } finally {
      ClassBytesSource.restoreProvider(priorProvider)
      state.set(prior)
    }
  }

  private data class State(
    val bridge: FrameworkClassLoadingBridge,
    val sourceClassLoader: ClassLoader?,
  )
}
