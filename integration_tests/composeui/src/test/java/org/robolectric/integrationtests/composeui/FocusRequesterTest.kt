package org.robolectric.integrationtests.composeui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

  /**
   * Test to ensure that `FocusRequester` can be used within a `Scaffold`.
   *
   * Originally reported in
   * [robolectric/robolectric#9703](https://github.com/robolectric/robolectric/issues/9703).
   * Solution coming from https://issuetracker.google.com/issues/206249038.
   */
  @Test
  fun `check FocusRequester is initialized`() {
    composeTestRule.setContent {
      Scaffold { contentPadding ->
        val focusRequester = rememberFocusRequester()

        Text(
          text = "Robolectric",
          modifier = Modifier.padding(contentPadding).focusRequester(focusRequester),
        )
      }
    }
  }

  @Composable
  private fun rememberFocusRequester(): FocusRequester {
    return remember { FocusRequester() }.apply { LaunchedEffect(this) { requestFocus() } }
  }
}
