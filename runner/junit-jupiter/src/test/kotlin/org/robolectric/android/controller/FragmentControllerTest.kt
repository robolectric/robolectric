package org.robolectric.android.controller

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.shadows.ShadowLooper

@Suppress("DEPRECATION")
@ExtendWith(RobolectricExtension::class)
class FragmentControllerTest {

  @Test
  fun initialNotAttached() {
    val fragment = LoginFragment()
    FragmentController.of(fragment)

    assertThat(fragment.view).isNull()
    assertThat(fragment.activity).isNull()
    assertThat(fragment.isAdded).isFalse()
  }

  @Test
  fun attachedAfterCreate() {
    val fragment = LoginFragment()
    FragmentController.of(fragment).create()
    ShadowLooper.shadowMainLooper().idle()

    assertThat(fragment.view).isNotNull()
    assertThat(fragment.activity).isNotNull()
    assertThat(fragment.isAdded).isTrue()
    assertThat(fragment.isResumed).isFalse()
  }

  class LoginFragment : Fragment() {
    override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
    ): View {
      return TextView(activity).apply { text = "fragment-view" }
    }
  }
}
