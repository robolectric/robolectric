package org.robolectric.integrationtests.kotlin

import android.widget.ImageView
import androidx.annotation.DrawableRes
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject

@Implements(ImageView::class)
open class CustomShadowImageView {
  @RealObject lateinit var realImageView: ImageView

  @DrawableRes
  var setImageResource: Int = 0
    private set

  @Implementation
  protected fun setImageResource(resId: Int) {
    setImageResource = resId
  }
}
