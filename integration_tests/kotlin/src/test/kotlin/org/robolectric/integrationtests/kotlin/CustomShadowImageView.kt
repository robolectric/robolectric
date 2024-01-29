package org.robolectric.integrationtests.kotlin

import android.widget.ImageView
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadows.ShadowView

@Implements(ImageView::class)
open class CustomShadowImageView : ShadowView() {
  @RealObject lateinit var realImageView: ImageView

  var longClickPerformed: Boolean = false
    private set

  @Implementation
  protected override fun performLongClick(): Boolean {
    longClickPerformed = true
    return super.performLongClick()
  }
}
