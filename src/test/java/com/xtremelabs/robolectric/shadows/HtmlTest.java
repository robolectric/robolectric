package com.xtremelabs.robolectric.shadows;


import android.app.Activity;
import android.content.Context;
import android.text.Html;
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
        ShadowHtml.clearExpectations();
    }

    @After
    public void tearDown() throws Exception {
        ShadowHtml.clearExpectations();
    }

    @Test
    public void shouldBeAbleToGetTextAfterUsingSetTextWithHtmlDotFromHtml() throws Exception {
        TextView textView = new TextView(context);
        textView.setText(Html.fromHtml("<b>some</b> html text"));
        assertThat(textView.getText().toString(), equalTo("<b>some</b> html text"));
    }

    public void fromHtml_shouldJustReturnArgByDefault() {
        String text = "<b>foo</b>";
        Spanned spanned = Html.fromHtml(text);
        assertEquals(text, spanned.toString());
    }

    @Test
    public void fromHtml_isMockable() {
        String text = "<b>foo</b>";
        ShadowHtml.expect(text).andReturn(new SpannedString("foo"));
        Spanned spanned = Html.fromHtml(text);
        assertEquals("foo", spanned.toString());
    }

    @Test
    public void clearExpectations() {
        ShadowHtml.expect("<b>foo</b>").andReturn(new SpannedString("foo"));
        ShadowHtml.clearExpectations();
        Spanned spanned = Html.fromHtml("<b>foo</b>");
        assertEquals("<b>foo</b>", spanned.toString());
    }

}
