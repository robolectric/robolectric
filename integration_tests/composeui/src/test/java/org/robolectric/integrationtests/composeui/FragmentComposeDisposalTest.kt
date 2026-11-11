package org.robolectric.integrationtests.composeui

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowPackageManager

@RunWith(AndroidJUnit4::class)
@Config(minSdk = 29)
class FragmentComposeDisposalTest {
  @get:Rule
  val activityRegistration =
    object : ExternalResource() {
      override fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activityInfo =
          ActivityInfo().apply {
            name = FragmentHostActivity::class.java.name
            packageName = context.packageName
          }
        Shadow.extract<ShadowPackageManager>(context.packageManager)
          .addOrUpdateActivity(activityInfo)
      }
    }

  @Test
  fun closingActivityScenarioDestroysFragmentAndDisposesComposeContent() {
    lateinit var callbacks: MutableList<String>

    ActivityScenario.launch(FragmentHostActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        callbacks = activity.callbacks
        val fragment =
          activity.supportFragmentManager.fragmentFactory.instantiate(
            activity.classLoader,
            RecordingFragment::class.java.name,
          )
        activity.supportFragmentManager
          .beginTransaction()
          .add(android.R.id.content, fragment)
          .commitNow()
      }
    }

    assertThat(callbacks).containsAtLeast("onStop", "onDestroyView", "dispose")
  }

  class FragmentHostActivity : FragmentActivity() {
    val callbacks = mutableListOf<String>()
  }

  class RecordingFragment : Fragment() {
    private lateinit var callbacks: MutableList<String>

    override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
    ): View {
      callbacks = (requireActivity() as FragmentHostActivity).callbacks
      return ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { DisposableEffect(Unit) { onDispose { callbacks.add("dispose") } } }
      }
    }

    override fun onStop() {
      super.onStop()
      callbacks.add("onStop")
    }

    override fun onDestroyView() {
      super.onDestroyView()
      callbacks.add("onDestroyView")
    }
  }
}
