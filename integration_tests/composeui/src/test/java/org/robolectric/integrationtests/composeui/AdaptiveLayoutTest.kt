package org.robolectric.integrationtests.composeui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.FoldingFeature
import androidx.window.testing.layout.FoldingFeature as TestFoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Shows how to test Material 3 adaptive layouts across phones, foldables and tablets. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(AndroidJUnit4::class)
class AdaptiveLayoutTest {
  private val publisherRule = WindowLayoutInfoPublisherRule()
  private val composeRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule val rules: RuleChain = RuleChain.outerRule(publisherRule).around(composeRule)

  @Test
  @Config(qualifiers = "w411dp-h891dp")
  fun `phone shows only the list pane`() {
    composeRule.setContent { ListDetail() }

    composeRule.onNodeWithText("List").assertIsDisplayed()
    composeRule.onNodeWithText("Detail").assertDoesNotExist()
  }

  @Test
  @Config(qualifiers = "w1280dp-h800dp")
  fun `tablet shows the list and detail panes`() {
    composeRule.setContent { ListDetail() }

    composeRule.onNodeWithText("List").assertIsDisplayed()
    composeRule.onNodeWithText("Detail").assertIsDisplayed()
  }

  @Test
  @Config(qualifiers = "w411dp-h891dp")
  fun `phone forced to tablet size shows the list and detail panes`() {
    composeRule.setContent {
      DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(1280.dp, 800.dp))) {
        ListDetail()
      }
    }

    composeRule.onNodeWithText("List").assertIsDisplayed()
    composeRule.onNodeWithText("Detail").assertIsDisplayed()
  }

  @Test
  @Config(qualifiers = "w411dp-h891dp")
  fun `phone navigates with a bar`() {
    assertThat(navigationSuiteType()).isEqualTo(NavigationSuiteType.ShortNavigationBarCompact)
  }

  @Test
  @Config(qualifiers = "w1280dp-h800dp")
  fun `tablet navigates with a rail`() {
    assertThat(navigationSuiteType()).isEqualTo(NavigationSuiteType.WideNavigationRailCollapsed)
  }

  @Test
  @Config(qualifiers = "w673dp-h841dp")
  fun `half-opened foldable is tabletop`() {
    var adaptiveInfo: WindowAdaptiveInfo? = null
    composeRule.setContent { adaptiveInfo = currentWindowAdaptiveInfo() }

    composeRule.runOnIdle {
      publisherRule.overrideWindowLayoutInfo(
        TestWindowLayoutInfo(
          listOf(
            TestFoldingFeature(
              activity = composeRule.activity,
              state = FoldingFeature.State.HALF_OPENED,
              orientation = FoldingFeature.Orientation.HORIZONTAL,
            )
          )
        )
      )
    }
    composeRule.waitForIdle()

    assertThat(checkNotNull(adaptiveInfo).windowPosture.isTabletop).isTrue()
  }

  private fun navigationSuiteType(): NavigationSuiteType {
    var type: NavigationSuiteType? = null
    composeRule.setContent {
      type = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())
    }
    composeRule.waitForIdle()
    return checkNotNull(type)
  }

  @Composable
  private fun ListDetail() {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    ListDetailPaneScaffold(
      directive = navigator.scaffoldDirective,
      value = navigator.scaffoldValue,
      listPane = { AnimatedPane { Text("List") } },
      detailPane = { AnimatedPane { Text("Detail") } },
    )
  }
}
