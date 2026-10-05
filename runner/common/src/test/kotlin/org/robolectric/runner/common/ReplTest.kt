package org.robolectric.runner.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.View
import android.view.View.MeasureSpec
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.Callable
import org.junit.jupiter.api.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * How a REPL uses the API, such as one that renders previews: it opens an environment once, and
 * evaluates each of its lines in it. A line is a class that the REPL compiles and loads itself,
 * with a class loader whose parent is that of the environment, so that the line can use Android.
 */
@OptIn(ExperimentalRunnerApi::class)
class ReplTest {
  @Test
  fun `the lines of a REPL use Android and share its state`() {
    repl { android ->
      evaluate(android, RememberGreeting::class.java)

      assertThat(evaluate(android, RecallGreeting::class.java)).isEqualTo("hello")
    }
  }

  @Test
  fun `a line of a REPL renders a view with Android's graphics`() {
    repl { android ->
      val pixels = evaluate(android, RenderRedOval::class.java) as IntArray

      // What only a real renderer gets right: the oval is red, and there is nothing around it.
      assertThat(pixels.asList()).containsExactly(Color.RED, Color.TRANSPARENT).inOrder()
    }
  }

  /** Runs the action with an environment that renders with Android's own graphics. */
  private fun repl(action: (RobolectricEnvironment) -> Unit) {
    RobolectricSession.builder().properties(testProperties()).build().use { session ->
      val config = Config.Builder().setSdk(34).build()
      session.open(session.plan(config, GraphicsMode.Mode.NATIVE).single()).use(action)
    }
  }

  /** Evaluates a line as a REPL does: loaded by a class loader for it, on Android's main thread. */
  private fun evaluate(android: RobolectricEnvironment, line: Class<out Callable<*>>): Any? {
    val loader = LineClassLoader(android.classLoader, line)
    return android.run {
      (loader.loadClass(line.name).getDeclaredConstructor().newInstance() as Callable<*>).call()
    }
  }

  /** Loads a line itself, as a REPL loads what it compiles, and everything else with its parent. */
  private class LineClassLoader(parent: ClassLoader, private val line: Class<*>) :
    ClassLoader(parent) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> =
      if (name == line.name) {
        synchronized(getClassLoadingLock(name)) { findLoadedClass(name) ?: define(name) }
      } else {
        super.loadClass(name, resolve)
      }

    private fun define(name: String): Class<*> {
      val file = "/${name.replace('.', '/')}.class"
      val bytes = line.getResourceAsStream(file)!!.use { it.readBytes() }
      return defineClass(name, bytes, 0, bytes.size)
    }
  }
}

private const val PREFERENCES = "repl"
private const val GREETING = "greeting"
private const val SIZE = 16

/** A line that changes the state of Android. */
class RememberGreeting : Callable<Boolean> {
  override fun call(): Boolean =
    RuntimeEnvironment.getApplication()
      .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
      .edit()
      .putString(GREETING, "hello")
      .commit()
}

/** A line that reads what an earlier line left. */
class RecallGreeting : Callable<String?> {
  override fun call(): String? =
    RuntimeEnvironment.getApplication()
      .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
      .getString(GREETING, null)
}

/** A line that draws a view with a red oval, and returns its pixels in the middle and a corner. */
class RenderRedOval : Callable<IntArray> {
  override fun call(): IntArray {
    val oval = ShapeDrawable(OvalShape())
    oval.paint.color = Color.RED
    val view = View(RuntimeEnvironment.getApplication())
    view.background = oval
    val side = MeasureSpec.makeMeasureSpec(SIZE, MeasureSpec.EXACTLY)
    view.measure(side, side)
    view.layout(0, 0, SIZE, SIZE)
    val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
    view.draw(Canvas(bitmap))
    return intArrayOf(bitmap.getPixel(SIZE / 2, SIZE / 2), bitmap.getPixel(0, 0))
  }
}
