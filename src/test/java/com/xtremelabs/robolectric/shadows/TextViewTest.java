package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.text.InputType;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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

    @Test
    public void shouldSetTextAndTextColorWhileInflatingXmlLayout() throws Exception {
        Activity activity = new Activity();
        activity.setContentView(R.layout.text_views);

        TextView black = (TextView) activity.findViewById(R.id.black_text_view);
        assertThat(black.getText().toString(), equalTo("Black Text"));
        assertThat(shadowOf(black).getTextColorHexValue(), equalTo(0));

        TextView white = (TextView) activity.findViewById(R.id.white_text_view);
        assertThat(white.getText().toString(), equalTo("White Text"));
        assertThat(shadowOf(white).getTextColorHexValue(), equalTo(activity.getResources().getColor(android.R.color.white)));

        TextView grey = (TextView) activity.findViewById(R.id.grey_text_view);
        assertThat(grey.getText().toString(), equalTo("Grey Text"));
        assertThat(shadowOf(grey).getTextColorHexValue(), equalTo(activity.getResources().getColor(R.color.grey42)));
    }

    @Test
    public void shouldSetHintAndHintColorWhileInflatingXmlLayout() throws Exception {
        Activity activity = new Activity();
        activity.setContentView(R.layout.text_views_hints);

        TextView black = (TextView) activity.findViewById(R.id.black_text_view_hint);
        assertThat(black.getHint().toString(), equalTo("Black Hint"));
        assertThat(shadowOf(black).getHintColorHexValue(), equalTo(0));

        TextView white = (TextView) activity.findViewById(R.id.white_text_view_hint);
        assertThat(white.getHint().toString(), equalTo("White Hint"));
        assertThat(shadowOf(white).getHintColorHexValue(), equalTo(activity.getResources().getColor(android.R.color.white)));

        TextView grey = (TextView) activity.findViewById(R.id.grey_text_view_hint);
        assertThat(grey.getHint().toString(), equalTo("Grey Hint"));
        assertThat(shadowOf(grey).getHintColorHexValue(), equalTo(activity.getResources().getColor(R.color.grey42)));
    }

    @Test
    public void shouldNotHaveTransformationMethodByDefault(){
        ShadowTextView view = new ShadowTextView();
        assertThat(view.getTransformationMethod(), is(CoreMatchers.<Object>nullValue()));
    }

    @Test
    public void shouldAllowSettingATransformationMethod(){
        ShadowTextView view = new ShadowTextView();
        view.setTransformationMethod(new ShadowPasswordTransformationMethod());
        assertEquals(view.getTransformationMethod().getClass(), ShadowPasswordTransformationMethod.class);
    }
    
    @Test
    public void testGetInputType() throws Exception {
        assertThat(textView.getInputType(), not(equalTo(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)));
        textView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        assertThat(textView.getInputType(), equalTo(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
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
