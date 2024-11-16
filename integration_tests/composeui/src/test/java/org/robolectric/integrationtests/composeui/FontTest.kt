package org.robolectric.integrationtests.composeui

import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties.EditableText
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The test file for ComposeUI's Font compatibility with Robolectric. */
@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class FontTest {
  @get:Rule val rule = createComposeRule()

  @Test
  fun `TextField's default value is expected`() {
    rule.setContent { NumberInput() }

    with(rule) {
      onNodeWithTag(TAG_NUMBER_INPUT).fetchSemanticsNode().run {
        assertThat(config[EditableText].text).isEqualTo(TEXT_CUSTOM_NUMBER)
      }
    }
  }

  @Composable
  fun NumberInput() {
    var text by remember { mutableStateOf(TextFieldValue(TEXT_CUSTOM_NUMBER)) }

    TextField(
      modifier = Modifier.semantics { testTag = TAG_NUMBER_INPUT },
      value = text,
      textStyle = TextStyle.Default,
      onValueChange = { newText -> text = newText },
    )
  }

  companion object {
    const val TAG_NUMBER_INPUT = "number_input"
    const val TEXT_CUSTOM_NUMBER = "123456789"
  }
}
