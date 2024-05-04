package org.robolectric.integrationtests.kotlin

import android.app.Activity
import android.view.ViewGroup
import android.widget.ImageView
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [CustomShadowImageView::class])
class CustomShadowImageViewTest {
  @Test
  fun `use custom ShadowImageView`() {
    val activity = Robolectric.setupActivity(Activity::class.java)
    val imageView = ImageView(activity)
    (activity.findViewById(android.R.id.content) as ViewGroup).addView(imageView)
    val shadowImageView = Shadow.extract<CustomShadowImageView>(imageView)
    assertThat(shadowImageView).isNotNull()
    assertThat(shadowImageView.realImageView).isSameInstanceAs(imageView)
    imageView.performLongClick()
    assertThat(shadowImageView.longClickPerformed).isTrue()
  }
}
