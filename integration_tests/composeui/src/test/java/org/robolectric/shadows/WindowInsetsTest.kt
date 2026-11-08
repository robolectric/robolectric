package org.robolectric.shadows

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build.VERSION_CODES.BAKLAVA
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(minSdk = P, maxSdk = BAKLAVA)
class WindowInsetsTest {
  private val composeRule = createAndroidComposeRule<InsetsActivity>()
  private val themeRule =
    object : ExternalResource() {
      override fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info =
          ActivityInfo().apply {
            name = InsetsActivity::class.java.name
            packageName = context.packageName
            theme = android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
          }
        shadowOf(context.packageManager).addOrUpdateActivity(info)
      }
    }

  @get:Rule val rules: RuleChain = RuleChain.outerRule(themeRule).around(composeRule)

  @Test
  fun fullscreenFragmentInsetsBecomeIdle() {
    val decorView = composeRule.activity.window.decorView
    var draws = 0
    // Fail instead of hanging if inset animations keep requesting draws.
    val drawListener = ViewTreeObserver.OnPreDrawListener {
      check(++draws < 1000) { "Window insets never became idle" }
      true
    }
    decorView.viewTreeObserver.addOnPreDrawListener(drawListener)
    try {
      val fragment = InsetsFragment()
      composeRule.activityRule.scenario.onActivity {
        it.supportFragmentManager.beginTransaction().add(android.R.id.content, fragment).commitNow()
      }
      composeRule.onNodeWithText("Insets").assertIsDisplayed()

      SystemUi.systemUiForDefaultDisplay()
        .setBehavior(SystemUi.STANDARD_STATUS_BAR, SystemUi.THREE_BUTTON_NAVIGATION)
      fragment.label = "Updated"

      composeRule.onNodeWithText("Updated").assertIsDisplayed()
    } finally {
      decorView.viewTreeObserver.removeOnPreDrawListener(drawListener)
    }
  }

  class InsetsActivity : FragmentActivity()

  class InsetsFragment : Fragment() {
    var label by mutableStateOf("Insets")
    private lateinit var scope: CoroutineScope

    override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
    ): View {
      scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
      val state = scope.launchMolecule(RecompositionMode.Immediate) { label }
      return ComposeView(requireContext()).apply {
        setContent {
          val text by state.collectAsState()
          Text(text, modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars))
        }
      }
    }

    override fun onDestroyView() {
      scope.cancel()
      super.onDestroyView()
    }
  }
}
