package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.text.*;
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

import java.util.*;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(WithTestDefaultsRunner.class)
public class TextViewTest {

	private static final String INITIAL_TEXT = "initial text";
	private static final String NEW_TEXT = "new text";
	private TextView textView;

    @Before
    public void setUp() throws Exception {
        textView = new TextView(new Activity());
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
    
    @Test
	public void givenATextViewWithATextWatcherAdded_WhenSettingTextWithTextResourceId_ShouldNotifyTextWatcher() {
    	MockTextWatcher mockTextWatcher = new MockTextWatcher();
    	textView.addTextChangedListener(mockTextWatcher);

    	textView.setText(R.string.hello);
		
		assertEachTextWatcherEventWasInvoked(mockTextWatcher);
    }
    
    @Test
	public void givenATextViewWithATextWatcherAdded_WhenSettingTextWithCharSequence_ShouldNotifyTextWatcher() {
    	MockTextWatcher mockTextWatcher = new MockTextWatcher();
    	textView.addTextChangedListener(mockTextWatcher);

		textView.setText("text");
		
		assertEachTextWatcherEventWasInvoked(mockTextWatcher);
	}
    
    @Test
	public void givenATextViewWithMultipleTextWatchersAdded_WhenSettingText_ShouldNotifyEachTextWatcher() {
    	List<MockTextWatcher> mockTextWatchers = anyNumberOfTextWatchers();
    	for (MockTextWatcher textWatcher : mockTextWatchers) {
    		textView.addTextChangedListener(textWatcher);
        }
    	
		textView.setText("text");
		
    	for (MockTextWatcher textWatcher : mockTextWatchers) {
    		assertEachTextWatcherEventWasInvoked(textWatcher);
        }
	}
    
    @Test
	public void whenSettingText_ShouldFireBeforeTextChangedWithCorrectArguments() {
		textView.setText(INITIAL_TEXT);
		TextWatcher mockTextWatcher = mock(TextWatcher.class);
		textView.addTextChangedListener(mockTextWatcher);
		
		textView.setText(NEW_TEXT);
		
		verify(mockTextWatcher).beforeTextChanged(INITIAL_TEXT, 0, INITIAL_TEXT.length(), NEW_TEXT.length());
	}
    
    @Test
	public void whenSettingText_ShouldFireOnTextChangedWithCorrectArguments() {
    	textView.setText(INITIAL_TEXT);
		TextWatcher mockTextWatcher = mock(TextWatcher.class);
		textView.addTextChangedListener(mockTextWatcher);
		
		textView.setText(NEW_TEXT);
		
		verify(mockTextWatcher).onTextChanged(NEW_TEXT, 0, INITIAL_TEXT.length(), NEW_TEXT.length());
	}
    
    @Test
	public void whenSettingText_ShouldFireAfterTextChangedWithCorrectArgument() {
		MockTextWatcher mockTextWatcher = new MockTextWatcher();
		textView.addTextChangedListener(mockTextWatcher);
		
		textView.setText(NEW_TEXT);
		
		assertThat(mockTextWatcher.afterTextChangeArgument.toString(), equalTo(NEW_TEXT));
	}
    
    @Test
    public void whenAppendingText_ShouldAppendNewTextAfterOldOne() {
    	textView.setText(INITIAL_TEXT);
    	textView.append(NEW_TEXT);
    	
    	assertEquals(INITIAL_TEXT + NEW_TEXT, textView.getText());
    }
    
    @Test
    public void whenAppendingText_ShouldFireBeforeTextChangedWithCorrectArguments() {
		textView.setText(INITIAL_TEXT);
		TextWatcher mockTextWatcher = mock(TextWatcher.class);
		textView.addTextChangedListener(mockTextWatcher);
		
		textView.append(NEW_TEXT);
		
		verify(mockTextWatcher).beforeTextChanged(INITIAL_TEXT, 0, INITIAL_TEXT.length(), INITIAL_TEXT.length() + NEW_TEXT.length());
    }
    
    @Test
    public void whenAppendingText_ShouldFireOnTextChangedWithCorrectArguments() {
		textView.setText(INITIAL_TEXT);
		TextWatcher mockTextWatcher = mock(TextWatcher.class);
		textView.addTextChangedListener(mockTextWatcher);
		
		textView.append(NEW_TEXT);
		
		verify(mockTextWatcher).onTextChanged(INITIAL_TEXT + NEW_TEXT, 0, INITIAL_TEXT.length(), INITIAL_TEXT.length() + NEW_TEXT.length());
    }
    
    @Test
    public void whenAppendingText_ShouldFireAfterTextChangedWithCorrectArgument() {
    	textView.setText(INITIAL_TEXT);
		MockTextWatcher mockTextWatcher = new MockTextWatcher();
		textView.addTextChangedListener(mockTextWatcher);
		
		textView.append(NEW_TEXT);
		
		assertThat(mockTextWatcher.afterTextChangeArgument.toString(), equalTo(INITIAL_TEXT + NEW_TEXT));
    }

    @Test
    public void removeTextChangedListener_shouldRemoveTheListener() throws Exception {
        MockTextWatcher watcher = new MockTextWatcher();
        textView.addTextChangedListener(watcher);
        assertTrue(shadowOf(textView).getWatchers().contains(watcher));

        textView.removeTextChangedListener(watcher);
        assertFalse(shadowOf(textView).getWatchers().contains(watcher));
    }

    @Test
    public void getPaint_returnsMeasureTextEnabledObject() throws Exception {
        assertThat(textView.getPaint().measureText("12345"), equalTo(5f));
    }

    private List<MockTextWatcher> anyNumberOfTextWatchers() {
		List<MockTextWatcher> mockTextWatchers = new ArrayList<MockTextWatcher>();
		int numberBetweenOneAndTen = new Random().nextInt(10) + 1;
		for (int i = 0; i < numberBetweenOneAndTen; i++) {
			mockTextWatchers.add(new MockTextWatcher());
        }
		return mockTextWatchers;
	}

	private void assertEachTextWatcherEventWasInvoked(MockTextWatcher mockTextWatcher) {
    	assertTrue("Expected each TextWatcher event to have been invoked once", mockTextWatcher.methodsCalled.size() == 3);
    	
		assertThat(mockTextWatcher.methodsCalled.get(0), equalTo("beforeTextChanged"));
		assertThat(mockTextWatcher.methodsCalled.get(1), equalTo("onTextChanged"));
		assertThat(mockTextWatcher.methodsCalled.get(2), equalTo("afterTextChanged"));
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
    
    private static class MockTextWatcher implements TextWatcher {

    	List<String> methodsCalled = new ArrayList<String>();
    	Editable afterTextChangeArgument;
    	
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
			methodsCalled.add("beforeTextChanged");
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			methodsCalled.add("onTextChanged");
		}

		@Override
		public void afterTextChanged(Editable s) {
			methodsCalled.add("afterTextChanged");
			afterTextChangeArgument = s;
		}
    	
    }
}
