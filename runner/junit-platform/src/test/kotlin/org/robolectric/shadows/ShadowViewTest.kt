package org.robolectric.shadows

import android.view.View
import android.widget.LinearLayout
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@Config(sdk = [34])
class ShadowViewTest {

  @Test
  fun layout_shouldAffectWidthAndHeight() {
    Robolectric.buildActivity(android.app.Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      val view = View(activity)
      view.layout(100, 200, 303, 404)
      assertThat(view.width).isEqualTo(303 - 100)
      assertThat(view.height).isEqualTo(404 - 200)
    }
  }

  @Test
  fun shouldKnowIfThisOrAncestorsAreVisible() {
    Robolectric.buildActivity(android.app.Activity::class.java).use { controller ->
      val activity = controller.setup().get()
      val parent = LinearLayout(activity)
      val view = View(activity)
      parent.addView(view)

      // Add parent to activity's content view so visibility works correctly
      activity.setContentView(parent)

      assertThat(view.isShown).isTrue()
      parent.visibility = View.GONE
      assertThat(view.isShown).isFalse()
    }
  }
}
