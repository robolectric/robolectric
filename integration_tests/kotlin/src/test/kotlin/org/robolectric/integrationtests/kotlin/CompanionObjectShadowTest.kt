// The point of these classes is the shape they compile to, so detekt's advice does not apply:
// turning them into objects would remove the companion that is under test, and the functions
// return marker strings precisely so a test can tell the real one from the shadowed one.
@file:Suppress("FunctionOnlyReturningConstant", "UtilityClassWithPublicConstructor")

package org.robolectric.integrationtests.kotlin

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/** A class whose "static" methods are Kotlin companion functions. */
class CompanionObjectSubject {
  companion object {
    fun companionFunction(): String = "real companionFunction"

    @JvmStatic fun jvmStaticFunction(): String = "real jvmStaticFunction"
  }
}

/**
 * Shadows the synthetic `Companion` class rather than the class that declares it.
 *
 * A companion function is compiled to an instance method on the nested `Companion` class, so this
 * is where the call lands however it was written, and the shadow methods are ordinary instance
 * methods. `@Implements` takes the name as a string because `CompanionObjectSubject.Companion` is
 * not nameable as a Kotlin class literal.
 */
@Implements(className = "org.robolectric.integrationtests.kotlin.CompanionObjectSubject\$Companion")
class ShadowCompanionObjectSubjectCompanion {
  @Implementation fun companionFunction(): String = "shadowed companionFunction"

  @Implementation fun jvmStaticFunction(): String = "shadowed jvmStaticFunction"
}

/**
 * Shadows the class that declares the companion, which is the intuitive thing to reach for and the
 * approach that does not work from Kotlin. See the tests below for what it does and does not
 * intercept.
 */
@Implements(CompanionObjectSubject::class)
class ShadowCompanionObjectSubject {
  companion object {
    @JvmStatic @Implementation fun jvmStaticFunction(): String = "shadowed by outer-class shadow"
  }
}

/**
 * How to shadow a Kotlin companion function.
 *
 * A companion function is not a static method. It is compiled to an instance method on a synthetic
 * nested `Companion` class, and `@JvmStatic` only adds a static bridge on the declaring class that
 * forwards to it. Kotlin call sites go straight to the `Companion` instance and never touch that
 * bridge, so shadowing the declaring class -- the obvious move, and the one that looks like the
 * Java case -- silently does nothing when the caller is Kotlin.
 *
 * Shadow the `Companion` class instead: every caller reaches it, whether it wrote
 * `CompanionObjectSubject.companionFunction()` in Kotlin or called the `@JvmStatic` bridge from
 * Java.
 */
@RunWith(RobolectricTestRunner::class)
class CompanionObjectShadowTest {

  @Test
  fun `companion functions are not shadowed by default`() {
    assertThat(CompanionObjectSubject.companionFunction()).isEqualTo("real companionFunction")
    assertThat(CompanionObjectSubject.jvmStaticFunction()).isEqualTo("real jvmStaticFunction")
  }

  /** The approach to use: shadow the `Companion` class. */
  @Test
  @Config(shadows = [ShadowCompanionObjectSubjectCompanion::class])
  fun `shadowing the Companion class intercepts companion functions`() {
    assertThat(CompanionObjectSubject.companionFunction()).isEqualTo("shadowed companionFunction")
    assertThat(CompanionObjectSubject.jvmStaticFunction()).isEqualTo("shadowed jvmStaticFunction")
  }

  /** It intercepts the `@JvmStatic` bridge as well, so Java callers see the shadow too. */
  @Test
  @Config(shadows = [ShadowCompanionObjectSubjectCompanion::class])
  fun `shadowing the Companion class intercepts the JvmStatic bridge`() {
    assertThat(invokeJvmStaticBridge()).isEqualTo("shadowed jvmStaticFunction")
  }

  /**
   * The trap. `@Implements(CompanionObjectSubject::class)` shadows the declaring class, which
   * Kotlin call sites bypass: `CompanionObjectSubject.jvmStaticFunction()` compiles to `getstatic
   * Companion` followed by `invokevirtual Companion.jvmStaticFunction()`.
   */
  @Test
  @Config(shadows = [ShadowCompanionObjectSubject::class])
  fun `shadowing the declaring class does not intercept Kotlin call sites`() {
    assertThat(CompanionObjectSubject.jvmStaticFunction()).isEqualTo("real jvmStaticFunction")
  }

  /**
   * The same shadow does take effect on the `@JvmStatic` bridge, which is what a Java caller
   * invokes. That asymmetry is why the outer-class shadow looks like it works until Kotlin calls
   * it.
   */
  @Test
  @Config(shadows = [ShadowCompanionObjectSubject::class])
  fun `shadowing the declaring class does intercept the JvmStatic bridge`() {
    assertThat(invokeJvmStaticBridge()).isEqualTo("shadowed by outer-class shadow")
  }

  /** Calls the static bridge on the declaring class, the way compiled Java code would. */
  private fun invokeJvmStaticBridge(): Any? =
    CompanionObjectSubject::class.java.getMethod("jvmStaticFunction").invoke(null)
}
