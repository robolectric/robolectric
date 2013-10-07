package org.robolectric.shadows;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class TextViewTest {

  private static final String INITIAL_TEXT = "initial text";
  private static final String NEW_TEXT = "new text";
  private TextView textView;

  @Before
  public void setUp() throws Exception {
    textView = new TextView(buildActivity(Activity.class).create().get());
  }

  @Test
  public void shouldTriggerTheImeListener() {
    TextView textView = new TextView(Robolectric.application);
    TestOnEditorActionListener actionListener = new TestOnEditorActionListener();
    textView.setOnEditorActionListener(actionListener);

    textView.onEditorAction(EditorInfo.IME_ACTION_GO);

    assertThat(actionListener.textView).isSameAs(textView);
    assertThat(actionListener.sentImeId).isEqualTo(EditorInfo.IME_ACTION_GO);
  }

  @Test
  public void testGetUrls() throws Exception {
    textView.setAutoLinkMask(Linkify.ALL);
    textView.setText("here's some text http://google.com/\nblah\thttp://another.com/123?456 blah");

    assertThat(urlStringsFrom(textView.getUrls())).isEqualTo(asList(
        "http://google.com",
        "http://another.com/123?456"
    ));
  }

  @Test
  public void testGetGravity() throws Exception {
    assertThat(textView.getGravity()).isNotEqualTo(Gravity.CENTER);
    textView.setGravity(Gravity.CENTER);
    assertThat(textView.getGravity()).isEqualTo(Gravity.CENTER);
  }

  @Test
  public void testMovementMethod() {
    MovementMethod movement = new ArrowKeyMovementMethod();

    assertNull(textView.getMovementMethod());
    textView.setMovementMethod(movement);
    assertThat(textView.getMovementMethod()).isSameAs(movement);
  }

  @Test
  public void testLinksClickable() {
    assertThat(textView.getLinksClickable()).isTrue();

    textView.setLinksClickable(false);
    assertThat(textView.getLinksClickable()).isFalse();

    textView.setLinksClickable(true);
    assertThat(textView.getLinksClickable()).isTrue();
  }

  @Test
  public void testGetTextAppearanceId() throws Exception {
    TextView textView = new TextView(Robolectric.application);
    textView.setTextAppearance(Robolectric.application, android.R.style.TextAppearance_Small);

    assertThat(shadowOf(textView).getTextAppearanceId()).isEqualTo(android.R.style.TextAppearance_Small);
  }

  @Test
  public void shouldSetTextAndTextColorWhileInflatingXmlLayout() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.text_views);

    TextView black = (TextView) activity.findViewById(R.id.black_text_view);
    assertThat(black.getText().toString()).isEqualTo("Black Text");
    assertThat(black.getCurrentTextColor()).isEqualTo(0xff000000);

    TextView white = (TextView) activity.findViewById(R.id.white_text_view);
    assertThat(white.getText().toString()).isEqualTo("White Text");
    assertThat(white.getCurrentTextColor()).isEqualTo(activity.getResources().getColor(android.R.color.white));

    TextView grey = (TextView) activity.findViewById(R.id.grey_text_view);
    assertThat(grey.getText().toString()).isEqualTo("Grey Text");
    assertThat(grey.getCurrentTextColor()).isEqualTo(activity.getResources().getColor(R.color.grey42));
  }

  @Test
  public void shouldSetHintAndHintColorWhileInflatingXmlLayout() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.text_views_hints);

    TextView black = (TextView) activity.findViewById(R.id.black_text_view_hint);
    assertThat(black.getHint().toString()).isEqualTo("Black Hint");
    assertThat(black.getCurrentHintTextColor()).isEqualTo(0xff000000);

    TextView white = (TextView) activity.findViewById(R.id.white_text_view_hint);
    assertThat(white.getHint().toString()).isEqualTo("White Hint");
    assertThat(white.getCurrentHintTextColor()).isEqualTo(activity.getResources().getColor(android.R.color.white));

    TextView grey = (TextView) activity.findViewById(R.id.grey_text_view_hint);
    assertThat(grey.getHint().toString()).isEqualTo("Grey Hint");
    assertThat(grey.getCurrentHintTextColor()).isEqualTo(activity.getResources().getColor(R.color.grey42));
  }

  @Test
  public void shouldNotHaveTransformationMethodByDefault() {
    TextView view = new TextView(Robolectric.application);
    assertThat(view.getTransformationMethod()).isNull();
  }

  @Test
  public void shouldAllowSettingATransformationMethod() {
    TextView view = new TextView(Robolectric.application);
    view.setTransformationMethod(PasswordTransformationMethod.getInstance());
    assertThat(view.getTransformationMethod()).isInstanceOf(PasswordTransformationMethod.class);
  }

  @Test
  public void testGetInputType() throws Exception {
    assertThat(textView.getInputType()).isNotEqualTo(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    textView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    assertThat(textView.getInputType()).isEqualTo(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
  public void givenATextViewWithATextWatcherAdded_WhenSettingNullText_ShouldNotifyTextWatcher() {
    MockTextWatcher mockTextWatcher = new MockTextWatcher();
    textView.addTextChangedListener(mockTextWatcher);

    textView.setText(null);

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

    verify(mockTextWatcher).onTextChanged(new SpannableStringBuilder(NEW_TEXT), 0, INITIAL_TEXT.length(), NEW_TEXT.length());
  }

  @Test
  public void whenSettingText_ShouldFireAfterTextChangedWithCorrectArgument() {
    MockTextWatcher mockTextWatcher = new MockTextWatcher();
    textView.addTextChangedListener(mockTextWatcher);

    textView.setText(NEW_TEXT);

    assertThat(mockTextWatcher.afterTextChangeArgument.toString()).isEqualTo(NEW_TEXT);
  }

  @Test
  public void whenAppendingText_ShouldAppendNewTextAfterOldOne() {
    textView.setText(INITIAL_TEXT);
    textView.append(NEW_TEXT);

    assertThat(textView.getText().toString()).isEqualTo(INITIAL_TEXT + NEW_TEXT);
  }

  @Test
  public void whenAppendingText_ShouldFireBeforeTextChangedWithCorrectArguments() {
    textView.setText(INITIAL_TEXT);
    TextWatcher mockTextWatcher = mock(TextWatcher.class);
    textView.addTextChangedListener(mockTextWatcher);

    textView.append(NEW_TEXT);

    verify(mockTextWatcher).beforeTextChanged(new SpannableStringBuilder(INITIAL_TEXT), 0, INITIAL_TEXT.length(), INITIAL_TEXT.length());
  }

  @Test
  public void whenAppendingText_ShouldFireOnTextChangedWithCorrectArguments() {
    textView.setText(INITIAL_TEXT);
    TextWatcher mockTextWatcher = mock(TextWatcher.class);
    textView.addTextChangedListener(mockTextWatcher);

    textView.append(NEW_TEXT);

    verify(mockTextWatcher).onTextChanged(new SpannableStringBuilder(INITIAL_TEXT + NEW_TEXT), 0, INITIAL_TEXT.length(), INITIAL_TEXT.length());
  }

  @Test
  public void whenAppendingText_ShouldFireAfterTextChangedWithCorrectArgument() {
    textView.setText(INITIAL_TEXT);
    MockTextWatcher mockTextWatcher = new MockTextWatcher();
    textView.addTextChangedListener(mockTextWatcher);

    textView.append(NEW_TEXT);

    assertThat(mockTextWatcher.afterTextChangeArgument.toString()).isEqualTo(INITIAL_TEXT + NEW_TEXT);
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
    assertThat(textView.getPaint().measureText("12345")).isEqualTo(5f);
  }

  @Test
  public void append_whenSelectionIsAtTheEnd_shouldKeepSelectionAtTheEnd() throws Exception {
    textView.setText("1", TextView.BufferType.EDITABLE);
    Selection.setSelection(textView.getEditableText(), 0, 0);
    textView.append("2");
    assertEquals(0, textView.getSelectionEnd());
    assertEquals(0, textView.getSelectionStart());

    Selection.setSelection(textView.getEditableText(), 2, 2);
    textView.append("3");
    assertEquals(3, textView.getSelectionEnd());
    assertEquals(3, textView.getSelectionStart());
  }

  @Test
  public void append_whenSelectionReachesToEnd_shouldExtendSelectionToTheEnd() throws Exception {
    textView.setText("12", TextView.BufferType.EDITABLE);
    Selection.setSelection(textView.getEditableText(), 0, 2);
    textView.append("3");
    assertEquals(3, textView.getSelectionEnd());
    assertEquals(0, textView.getSelectionStart());
  }

  @Test
  public void testSetCompountDrawablesWithIntrinsicBounds_int_shouldCreateDrawablesWithResourceIds() throws Exception {
    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.an_image, R.drawable.an_other_image, R.drawable.third_image, R.drawable.fourth_image);

    assertEquals(R.drawable.an_image, shadowOf(textView.getCompoundDrawables()[0]).getCreatedFromResId());
    assertEquals(R.drawable.an_other_image, shadowOf(textView.getCompoundDrawables()[1]).getCreatedFromResId());
    assertEquals(R.drawable.third_image, shadowOf(textView.getCompoundDrawables()[2]).getCreatedFromResId());
    assertEquals(R.drawable.fourth_image, shadowOf(textView.getCompoundDrawables()[3]).getCreatedFromResId());
  }

  @Test
  public void testSetCompountDrawablesWithIntrinsicBounds_int_shouldNotCreateDrawablesForZero() throws Exception {
    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

    assertNull(textView.getCompoundDrawables()[0]);
    assertNull(textView.getCompoundDrawables()[1]);
    assertNull(textView.getCompoundDrawables()[2]);
    assertNull(textView.getCompoundDrawables()[3]);
  }

  @Test
  public void canSetAndGetTypeface() throws Exception {
    Typeface typeface = Robolectric.newInstanceOf(Typeface.class);
    textView.setTypeface(typeface);
    assertSame(typeface, textView.getTypeface());
  }

  @Test
  public void onTouchEvent_shouldCallMovementMethodOnTouchEventWithSetMotionEvent() throws Exception {
    TestMovementMethod testMovementMethod = new TestMovementMethod();
    textView.setMovementMethod(testMovementMethod);
    textView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
    textView.measure(100, 100);

    MotionEvent event = MotionEvent.obtain(0, 0, 0, 0, 0, 0);
    textView.dispatchTouchEvent(event);

    assertEquals(testMovementMethod.event, event);
  }

  @Test
  public void testGetError() {
    assertNull(textView.getError());
    CharSequence error = "myError";
    textView.setError(error);
    assertEquals(error, textView.getError());
  }

  @Test
  public void canSetAndGetInputFilters() throws Exception {
    final InputFilter[] expectedFilters = new InputFilter[]{new InputFilter.LengthFilter(1)};
    textView.setFilters(expectedFilters);
    assertThat(textView.getFilters()).isSameAs(expectedFilters);
  }

  @Test
  public void testHasSelectionReturnsTrue() {
    textView.setText("1", TextView.BufferType.SPANNABLE);
    textView.onTextContextMenuItem(android.R.id.selectAll);
    assertTrue(textView.hasSelection());
  }

  @Test
  public void testHasSelectionReturnsFalse() {
    textView.setText("1", TextView.BufferType.SPANNABLE);
    assertFalse(textView.hasSelection());
  }

  @Test
  public void whenSettingTextToNull_WatchersSeeEmptyString() {
    TextWatcher mockTextWatcher = mock(TextWatcher.class);
    textView.addTextChangedListener(mockTextWatcher);
    textView.setText(null);
    verify(mockTextWatcher).onTextChanged(new SpannableStringBuilder(""), 0, 0, 0);
  }

  @Test
  public void getPaint_returnsNonNull() {
    assertNotNull(textView.getPaint());
  }

  @Test
  public void testNoArgAppend() {
    textView.setText("a");
    textView.append("b");
    assertThat(textView.getText().toString()).isEqualTo("ab");
  }

  @Test
  public void setTextSize_shouldHandleDips() throws Exception {
    shadowOf(Robolectric.application.getResources()).setDensity(1.5f);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
    assertThat(textView.getTextSize()).isEqualTo(15f);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
    assertThat(textView.getTextSize()).isEqualTo(30f);
  }

  @Test
  public void setTextSize_shouldHandlePixels() throws Exception {
    shadowOf(Robolectric.application.getResources()).setDensity(1.5f);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10);
    assertThat(textView.getTextSize()).isEqualTo(10f);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
    assertThat(textView.getTextSize()).isEqualTo(20f);
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void setLines_setsTheLines() throws Exception {
    textView.measure(100, 100);
    textView.setLines(1);
    assertThat(textView.getLineCount()).isEqualTo(1);
    textView.setLines(4);
    assertThat(textView.getLineCount()).isEqualTo(4);
  }

  @Test
  public void getPaintFlagsAndSetPaintFlags_shouldWork() {
    assertThat(textView.getPaintFlags()).isEqualTo(0);
    textView.setPaintFlags(100);
    assertThat(textView.getPaintFlags()).isEqualTo(100);
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

    assertThat(mockTextWatcher.methodsCalled.get(0)).isEqualTo("beforeTextChanged");
    assertThat(mockTextWatcher.methodsCalled.get(1)).isEqualTo("onTextChanged");
    assertThat(mockTextWatcher.methodsCalled.get(2)).isEqualTo("afterTextChanged");
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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

  private static class TestMovementMethod implements MovementMethod {
    public MotionEvent event;
    public boolean touchEventWasCalled;

    @Override
    public void initialize(TextView widget, Spannable text) {
    }

    @Override
    public boolean onKeyDown(TextView widget, Spannable text, int keyCode, KeyEvent event) {
      return false;
    }

    @Override
    public boolean onKeyUp(TextView widget, Spannable text, int keyCode, KeyEvent event) {
      return false;
    }

    @Override
    public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
      return false;
    }

    @Override
    public void onTakeFocus(TextView widget, Spannable text, int direction) {
    }

    @Override
    public boolean onTrackballEvent(TextView widget, Spannable text, MotionEvent event) {
      return false;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable text, MotionEvent event) {
      this.event = event;
      touchEventWasCalled = true;
      return false;
    }

    @Override
    public boolean canSelectArbitrarily() {
      return false;
    }

    @Override
    public boolean onGenericMotionEvent(TextView widget, Spannable text,
                      MotionEvent event) {
      return false;
    }
  }
}
