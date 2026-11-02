package org.robolectric.junit.jupiter

import android.os.Build
import com.google.common.truth.Truth.assertThat
import java.io.Serializable
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.robolectric.annotation.Config

/** Tests that the arguments JUnit resolves reach the test in the Android environment. */
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class ArgumentsTest {
  data class Size(val width: Int, val height: Int) : Serializable {
    private companion object {
      private const val serialVersionUID = 1L
    }
  }

  @ParameterizedTest
  @MethodSource("sizes")
  fun `an argument of a class of the test is recreated in the Android environment`(size: Size) {
    // It is of the class that this instance uses, or the cast JUnit's argument needs would fail.
    assertThat(size.javaClass).isSameInstanceAs(Size::class.java)
    assertThat(size).isAnyOf(Size(1, 2), Size(3, 4))
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34)
  }

  @ParameterizedTest
  @MethodSource("listsOfSizes")
  fun `objects inside of a JDK collection are recreated too`(sizes: List<Size>, total: Int) {
    assertThat(sizes.sumOf { it.width }).isEqualTo(total)
  }

  @ParameterizedTest
  @MethodSource("texts")
  fun `an argument of a JDK class is passed as it is`(text: StringBuilder) {
    assertThat(text).isSameInstanceAs(SHARED_TEXT)
  }

  companion object {
    private val SHARED_TEXT: StringBuilder
      get() = System.getProperties()["org.robolectric.junit.jupiter.sharedText"] as StringBuilder

    @JvmStatic fun sizes() = listOf(Size(1, 2), Size(3, 4))

    @JvmStatic
    fun listsOfSizes() =
      listOf(Arguments.of(listOf(Size(1, 2), Size(3, 4)), 4), Arguments.of(emptyList<Size>(), 0))

    @JvmStatic
    fun texts(): List<StringBuilder> {
      // The system properties are the same object inside of the Android environment.
      val text = StringBuilder("shared")
      System.getProperties()["org.robolectric.junit.jupiter.sharedText"] = text
      return listOf(text)
    }
  }
}
