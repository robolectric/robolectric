package org.robolectric.shadows;


import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class HtmlTest {
  private Context context;

  @Before
  public void setUp() throws Exception {
    context = Robolectric.application;
  }

  @Test
  public void shouldBeAbleToGetTextFromTextViewAfterUsingSetTextWithHtmlDotFromHtml() throws Exception {
    TextView textView = new TextView(context);
    textView.setText(Html.fromHtml("<b>some</b> html text"));
    assertThat(textView.getText().toString()).isEqualTo("some html text");
  }

  @Test
  public void shouldBeAbleToGetTextFromEditTextAfterUsingSetTextWithHtmlDotFromHtml() throws Exception {
    EditText editText = new EditText(context);
    editText.setText(Html.fromHtml("<b>some</b> html text"));
    assertThat(editText.getText().toString()).isEqualTo("some html text");
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenNullStringEncountered() throws Exception {
    Html.fromHtml(null);
  }

  @Test
  public void fromHtml_shouldJustReturnArgByDefault() {
    String text = "<b>foo</b>";
    Spanned spanned = Html.fromHtml(text);
    assertThat(spanned.toString()).isEqualTo("foo");
  }
}
