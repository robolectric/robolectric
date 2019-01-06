package org.robolectric.kotlin

import android.app.Application
import android.widget.Toast
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowToast

object TestApis {
//  class Toast {
//    companion object {
//      fun shownToastCount(): Class<*>? {
//        throw UnsupportedOperationException("not implemented")
//      }
//
//    }
//  }

  val Application.testApi: ShadowApplication
    get() {
      return Shadow.extract(this)
    }

  val Toast.testApi: ShadowToast
    get() {
      return Shadow.extract(this)
    }
}