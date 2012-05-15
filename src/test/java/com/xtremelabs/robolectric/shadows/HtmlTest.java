package com.xtremelabs.robolectric.shadows;


import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.widget.EditText;
import android.widget.TextView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class HtmlTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
    }

    @Test
    public void shouldBeAbleToGetTextFromTextViewAfterUsingSetTextWithHtmlDotFromHtml() throws Exception {
        TextView textView = new TextView(context);
        textView.setText(Html.fromHtml("<b>some</b> html text"));
        assertThat(textView.getText().toString(), equalTo("<b>some</b> html text"));
    }

    @Test
    public void shouldBeAbleToGetTextFromEditTextAfterUsingSetTextWithHtmlDotFromHtml() throws Exception {
        EditText editText = new EditText(context);
        editText.setText(Html.fromHtml("<b>some</b> html text"));
        assertThat(editText.getText().toString(), equalTo("<b>some</b> html text"));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenNullStringEncountered() throws Exception {
        Html.fromHtml(null);
    }
}
