package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.MovementMethod;
import android.text.method.TransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextView.class)
public class ShadowTextView extends ShadowView {
  @RealObject TextView realTextView;

  private CharSequence text = "";
  private TextView.BufferType bufferType = TextView.BufferType.NORMAL;
  private Integer textColorHexValue;
  private Integer hintColorHexValue;
  private float textSize = 14.0f;
  private boolean autoLinkPhoneNumbers;
  private int autoLinkMask;
  private CharSequence hintText;
  private CharSequence errorText;
  private int compoundDrawablePadding;
  private MovementMethod movementMethod;
  private boolean linksClickable;
  private int gravity;
  private int imeOptions = EditorInfo.IME_NULL;
  private TextView.OnEditorActionListener onEditorActionListener;
  private int textAppearanceId;
  private TransformationMethod transformationMethod;
  private int inputType;
  private int lines;
  protected int selectionStart = -1;
  protected int selectionEnd = -1;
  private Typeface typeface;
  private InputFilter[] inputFilters;
  private TextPaint textPaint = new TextPaint();

  private List<TextWatcher> watchers = new ArrayList<>();
  private List<Integer> previousKeyCodes = new ArrayList<>();
  private List<KeyEvent> previousKeyEvents = new ArrayList<>();
  private int paintFlags;
  private int compoundDrawablesWithIntrinsicBoundsLeft;
  private int compoundDrawablesWithIntrinsicBoundsTop;
  private int compoundDrawablesWithIntrinsicBoundsRight;
  private int compoundDrawablesWithIntrinsicBoundsBottom;

  @Implementation
  protected void setTextAppearance(Context context, int resid) {
    textAppearanceId = resid;
    directlyOn(realTextView, TextView.class).setTextAppearance(context, resid);
  }

  @Implementation
  protected boolean onKeyDown(int keyCode, KeyEvent event) {
    previousKeyCodes.add(keyCode);
    previousKeyEvents.add(event);
    return directlyOn(realTextView, TextView.class).onKeyDown(keyCode, event);
  }

  @Implementation
  protected boolean onKeyUp(int keyCode, KeyEvent event) {
    previousKeyCodes.add(keyCode);
    previousKeyEvents.add(event);
    return directlyOn(realTextView, TextView.class).onKeyUp(keyCode, event);
  }

  public int getPreviousKeyCode(int index) {
    return previousKeyCodes.get(index);
  }

  public KeyEvent getPreviousKeyEvent(int index) {
    return previousKeyEvents.get(index);
  }

  /**
   * Returns the text string of this {@code TextView}.
   *
   * Robolectric extension.
   */
  @Override
  public String innerText() {
    CharSequence text = realTextView.getText();
    return (text == null || realTextView.getVisibility() != View.VISIBLE) ? "" : text.toString();
  }

  public int getTextAppearanceId() {
    return textAppearanceId;
  }

  @Implementation
  protected void addTextChangedListener(TextWatcher watcher) {
    this.watchers.add(watcher);
    directlyOn(realTextView, TextView.class).addTextChangedListener(watcher);
  }

  @Implementation
  protected void removeTextChangedListener(TextWatcher watcher) {
    this.watchers.remove(watcher);
    directlyOn(realTextView, TextView.class).removeTextChangedListener(watcher);
  }

  /**
   * @return the list of currently registered watchers/listeners
   */
  public List<TextWatcher> getWatchers() {
    return watchers;
  }

  @HiddenApi @Implementation
  public Locale getTextServicesLocale() {
    return Locale.getDefault();
  }

  @Override
  protected void dumpAttributes(PrintStream out) {
    super.dumpAttributes(out);
    CharSequence text = realTextView.getText();
    if (text != null && text.length() > 0) {
      dumpAttribute(out, "text", text.toString());
    }
  }

  @Implementation
  protected int getPaintFlags() {
    return paintFlags;
  }

  @Implementation
  protected void setPaintFlags(int paintFlags) {
    this.paintFlags = paintFlags;
  }

  @Implementation
  protected void setOnEditorActionListener(TextView.OnEditorActionListener l) {
    this.onEditorActionListener = l;
    directlyOn(realTextView, TextView.class).setOnEditorActionListener(l);
  }

  public TextView.OnEditorActionListener getOnEditorActionListener() {
    return onEditorActionListener;
  }

  @Implementation
  protected void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
    this.compoundDrawablesWithIntrinsicBoundsLeft = left;
    this.compoundDrawablesWithIntrinsicBoundsTop = top;
    this.compoundDrawablesWithIntrinsicBoundsRight = right;
    this.compoundDrawablesWithIntrinsicBoundsBottom = bottom;
    directlyOn(realTextView, TextView.class).setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
  }

  public int getCompoundDrawablesWithIntrinsicBoundsLeft() {
    return compoundDrawablesWithIntrinsicBoundsLeft;
  }

  public int getCompoundDrawablesWithIntrinsicBoundsTop() {
    return compoundDrawablesWithIntrinsicBoundsTop;
  }

  public int getCompoundDrawablesWithIntrinsicBoundsRight() {
    return compoundDrawablesWithIntrinsicBoundsRight;
  }

  public int getCompoundDrawablesWithIntrinsicBoundsBottom() {
    return compoundDrawablesWithIntrinsicBoundsBottom;
  }
}
