package com.xtremelabs.robolectric.shadows;

import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class HtmlTest {

    @Before
    public void setUp() throws Exception {
        ShadowHtml.clearExpectations();
    }

    @After
    public void tearDown() throws Exception {
        ShadowHtml.clearExpectations();
    }

    @Test
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
