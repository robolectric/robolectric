package com.xtremelabs.robolectric.shadows;

import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TextViewTest {

    private TextView textView;

    @Before
    public void setUp() throws Exception {
        textView = new TextView(null);
    }

    @Test
    public void shouldTriggerTheImeListener() {
        TextView textView = new TextView(null);
        TestOnEditorActionListener actionListener = new TestOnEditorActionListener();
        textView.setOnEditorActionListener(actionListener);

        shadowOf(textView).triggerEditorAction(EditorInfo.IME_ACTION_GO);

        assertThat(actionListener.textView, is(textView));
        assertThat(actionListener.sentImeId, equalTo(EditorInfo.IME_ACTION_GO));
    }

    @Test
    public void testGetUrls() throws Exception {
        textView.setText("here's some text http://google.com/\nblah\thttp://another.com/123?456 blah");

        assertThat(urlStringsFrom(textView.getUrls()), equalTo(asList(
                "http://google.com/",
                "http://another.com/123?456"
        )));
    }

    @Test
    public void testGetGravity() throws Exception {
        assertThat(textView.getGravity(), not(equalTo(Gravity.CENTER)));
        textView.setGravity(Gravity.CENTER);
        assertThat(textView.getGravity(), equalTo(Gravity.CENTER));
    }

    @Test
    public void testMovementMethod() {
        MovementMethod movement = new ArrowKeyMovementMethod();

        assertNull(textView.getMovementMethod());
        textView.setMovementMethod(movement);
        assertThat(textView.getMovementMethod(), sameInstance(movement));
    }

    @Test
    public void testLinksClickable() {
        assertThat(textView.getLinksClickable(), equalTo(false));

        textView.setLinksClickable(true);
        assertThat(textView.getLinksClickable(), equalTo(true));

        textView.setLinksClickable(false);
        assertThat(textView.getLinksClickable(), equalTo(false));
    }

    @Test
    public void testGetTextAppearanceId() throws Exception {
        TextView textView = new TextView(null);
        textView.setTextAppearance(null, 5);

        assertThat(shadowOf(textView).getTextAppearanceId(), equalTo(5));
    }

    private List<String> urlStringsFrom(URLSpan[] urlSpans) {
        List<String> urls = new ArrayList<String>();
        for (URLSpan urlSpan : urlSpans) {
            urls.add(urlSpan.getURL());
        }
        return urls;
    }

    private static class TestOnEditorActionListener implements TextView.OnEditorActionListener {
        private TextView textView;
        private int sentImeId;

        @Override
        public boolean onEditorAction(TextView textView, int sentImeId, KeyEvent keyEvent) {
            this.textView = textView;
            this.sentImeId = sentImeId;
            return false;
        }
    }
}
