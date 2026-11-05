package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowHtmlTest {
  private static final String HTML_SHORT = "<img src='foo.png'>";
  private static final String HTML_LONG =
      String.format("<img src='%s.png'>", String.join("", Collections.nCopies(100, "foo")));

  private Context context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void shouldBeAbleToGetTextFromTextViewAfterUsingSetTextWithHtmlDotFromHtml() {
    TextView textView = new TextView(context);
    textView.setText(Html.fromHtml("<b>some</b> html text"));
    assertThat(textView.getText().toString()).isEqualTo("some html text");
  }

  @Test
  public void shouldBeAbleToGetTextFromEditTextAfterUsingSetTextWithHtmlDotFromHtml() {
    EditText editText = new EditText(context);
    editText.setText(Html.fromHtml("<b>some</b> html text"));
    assertThat(editText.getText().toString()).isEqualTo("some html text");
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenNullStringEncountered() {
    Html.fromHtml(null);
  }

  @Test
  public void fromHtml_shouldJustReturnArgByDefault() {
    String text = "<b>foo</b>";
    Spanned spanned = Html.fromHtml(text);
    assertThat(spanned.toString()).isEqualTo("foo");
  }

  @Test
  public void testArraycopyShort() {
    Html.fromHtml(HTML_SHORT, Html.FROM_HTML_MODE_LEGACY, null, null);
  }

  /** this test requires that {@link org.ccil.cowan.tagsoup.HTMLScanner} be instrumented. */
  @Test
  public void testArraycopyLong() {
    Html.fromHtml(HTML_LONG, Html.FROM_HTML_MODE_LEGACY, null, null);
  }
}
