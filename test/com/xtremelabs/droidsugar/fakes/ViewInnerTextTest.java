package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner.proxyFor;
import static org.junit.Assert.assertEquals;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ViewInnerTextTest {
    private Context activity;

    @Before
    public void setUp() throws Exception {
        activity = new Activity();
    }

    @Test
    public void testInnerText() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(View.class, FakeView.class);
        DroidSugarAndroidTestRunner.addProxy(ViewGroup.class, FakeViewGroup.class);
        DroidSugarAndroidTestRunner.addProxy(TextView.class, FakeTextView.class);

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

        assertEquals("blah a b c d e f g h i jkl! mnop", ((FakeView) proxyFor(top)).innerText());
    }

    private TextView textView(String text) {
        TextView textView = new TextView(activity);
        textView.setText(text);
        return textView;
    }
}
