package org.robolectric.integrationtests.composeui

import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FocusRequesterTest {
  @get:Rule val composeTestRule = createComposeRule()

  /** Test for https://github.com/robolectric/robolectric/issues/9703 */
  @Test
  fun `check FocusRequester is initialized`() {
    composeTestRule.setContent {
      val focusRequester = rememberFocusRequester()

      TextField(value = "", onValueChange = {}, modifier = Modifier.focusRequester(focusRequester))
    }
  }

  @Composable
  private fun rememberFocusRequester(): FocusRequester {
    return remember { FocusRequester() }.apply { LaunchedEffect(this) { requestFocus() } }
  }
}
