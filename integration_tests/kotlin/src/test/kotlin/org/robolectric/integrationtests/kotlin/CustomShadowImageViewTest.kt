package org.robolectric.integrationtests.kotlin

import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [CustomShadowImageView::class])
class CustomShadowImageViewTest {
  @Test
  fun `use custom ShadowImageView`() {
    val imageView = ImageView(ApplicationProvider.getApplicationContext())
    val shadowImageView = Shadow.extract<CustomShadowImageView>(imageView)
    assertThat(shadowImageView).isNotNull()
    assertThat(shadowImageView.realImageView).isSameInstanceAs(imageView)
    val resourceId = Int.MAX_VALUE
    imageView.setImageResource(resourceId)
    assertThat(shadowImageView.setImageResource).isEqualTo(resourceId)
  }
}
