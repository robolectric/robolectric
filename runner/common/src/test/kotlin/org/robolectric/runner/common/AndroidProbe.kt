package org.robolectric.runner.common

import android.os.Build
import android.os.Handler
import android.os.Looper
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

/** Reads the Android state of an environment. It has to be loaded by the environment. */
@OptIn(ExperimentalRunnerApi::class)
class AndroidProbe {
  fun sdkInt(): Int = Build.VERSION.SDK_INT

  fun applicationIdentity(): Int = System.identityHashCode(RuntimeEnvironment.getApplication())

  fun qualifiers(): String = RuntimeEnvironment.getQualifiers()

  fun isOnMainThread(): Boolean = Looper.getMainLooper().thread === Thread.currentThread()

  fun postToMainLooper(): Boolean = Handler(Looper.getMainLooper()).post {}

  fun idleMainLooper() = Shadows.shadowOf(Looper.getMainLooper()).idle()

  companion object {
    /** Calls the method with the given name on a probe loaded by the environment. */
    @Suppress("UNCHECKED_CAST")
    fun <T> read(environment: RobolectricEnvironment, method: String): T = environment.run {
      val probeClass = environment.loadClass(AndroidProbe::class.java)
      val probe = probeClass.getDeclaredConstructor().newInstance()
      probeClass.getMethod(method).invoke(probe) as T
    }
  }
}
