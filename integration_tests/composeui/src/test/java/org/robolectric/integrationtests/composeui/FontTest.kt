package org.robolectric.integrationtests.composeui

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The test file for ComposeUI's Font compatibility with Robolectric. */
@RunWith(RobolectricTestRunner::class)
class FontTest {
  @get:Rule val rule = createComposeRule()

  @Test
  fun `Set text with custom font and it works without crash`() {
    rule.setContent {
      Text(text = "Foo", style = TextStyle(fontFamily = FontFamily(Font(R.font.my_font))))
    }
  }
}
