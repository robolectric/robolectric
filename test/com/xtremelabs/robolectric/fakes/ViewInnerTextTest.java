package com.xtremelabs.robolectric.fakes;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.DogfoodRobolectricTestRunner.shadowFor;
import static org.junit.Assert.assertEquals;

@RunWith(DogfoodRobolectricTestRunner.class)
public class ViewInnerTextTest {
    private Context activity;

    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addGenericProxies();

        activity = new Activity();
    }

    @Test
    public void testInnerText() throws Exception {
        LinearLayout top = new LinearLayout(activity);
        top.addView(textView("blah"));
        top.addView(new View(activity));
        top.addView(textView("a b c"));

        LinearLayout innerLayout = new LinearLayout(activity);
        top.addView(innerLayout);

        innerLayout.addView(textView("d e f"));
        innerLayout.addView(textView("g h i"));
        innerLayout.addView(textView(""));
        innerLayout.addView(textView(null));
        innerLayout.addView(textView("jkl!"));

        top.addView(textView("mnop"));

        assertEquals("blah a b c d e f g h i jkl! mnop", ((ShadowView) shadowFor(top)).innerText());
    }

    @Test
    public void shouldOnlyIncludeViewTextViewsText() throws Exception {
        LinearLayout top = new LinearLayout(activity);
        top.addView(textView("blah", View.VISIBLE));
        top.addView(textView("blarg", View.GONE));
        top.addView(textView("arrg", View.INVISIBLE));

        assertEquals("blah", ((ShadowView) shadowFor(top)).innerText());
    }

    @Test
    public void shouldNotPrefixBogusSpaces() throws Exception {
        LinearLayout top = new LinearLayout(activity);
        top.addView(textView("blarg", View.GONE));
        top.addView(textView("arrg", View.INVISIBLE));
        top.addView(textView("blah", View.VISIBLE));

        assertEquals("blah", ((ShadowView) shadowFor(top)).innerText());
    }

    private TextView textView(String text) {
        return textView(text, View.VISIBLE);
    }

    private TextView textView(String text, int visibility) {
        TextView textView = new TextView(activity);
        textView.setText(text);
        textView.setVisibility(visibility);
        return textView;
    }
}
