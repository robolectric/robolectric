package com.xtremelabs.robolectric.shadows;

import android.text.style.URLSpan;
import android.widget.TextView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TextViewTest {
    @Test
    public void testGetUrls() throws Exception {
        TextView textView = new TextView(null);
        textView.setText("here's some text http://google.com/\nblah\thttp://another.com/123?456 blah");

        assertThat(urlStringsFrom(textView.getUrls()), equalTo(asList(
                "http://google.com/",
                "http://another.com/123?456"
        )));
    }

    private List<String> urlStringsFrom(URLSpan[] urlSpans) {
        List<String> urls = new ArrayList<String>();
        for (URLSpan urlSpan : urlSpans) {
            urls.add(urlSpan.getURL());
        }
        return urls;
    }
}
