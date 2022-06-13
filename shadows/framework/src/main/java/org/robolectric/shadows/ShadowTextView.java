package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextView.class)
public class ShadowTextView extends ShadowView {
  @RealObject TextView realTextView;

  private TextView.OnEditorActionListener onEditorActionListener;
  private int textAppearanceId;
  protected int selectionStart = -1;
  protected int selectionEnd = -1;

  private List<TextWatcher> watchers = new ArrayList<>();
  private List<Integer> previousKeyCodes = new ArrayList<>();
  private List<KeyEvent> previousKeyEvents = new ArrayList<>();
  private int compoundDrawablesWithIntrinsicBoundsLeft;
  private int compoundDrawablesWithIntrinsicBoundsTop;
  private int compoundDrawablesWithIntrinsicBoundsRight;
  private int compoundDrawablesWithIntrinsicBoundsBottom;

  @Implementation
  protected void setTextAppearance(Context context, int resid) {
    textAppearanceId = resid;
    reflector(TextViewReflector.class, realTextView).setTextAppearance(context, resid);
  }

  @Implementation
  protected boolean onKeyDown(int keyCode, KeyEvent event) {
    previousKeyCodes.add(keyCode);
    previousKeyEvents.add(event);
    return reflector(TextViewReflector.class, realTextView).onKeyDown(keyCode, event);
  }

  @Implementation
  protected boolean onKeyUp(int keyCode, KeyEvent event) {
    previousKeyCodes.add(keyCode);
    previousKeyEvents.add(event);
    return reflector(TextViewReflector.class, realTextView).onKeyUp(keyCode, event);
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
    reflector(TextViewReflector.class, realTextView).addTextChangedListener(watcher);
  }

  @Implementation
  protected void removeTextChangedListener(TextWatcher watcher) {
    this.watchers.remove(watcher);
    reflector(TextViewReflector.class, realTextView).removeTextChangedListener(watcher);
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
  protected void setOnEditorActionListener(TextView.OnEditorActionListener l) {
    this.onEditorActionListener = l;
    reflector(TextViewReflector.class, realTextView).setOnEditorActionListener(l);
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
    reflector(TextViewReflector.class, realTextView)
        .setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
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

  @ForType(TextView.class)
  interface TextViewReflector {

    @Direct
    void setTextAppearance(Context context, int resid);

    @Direct
    boolean onKeyDown(int keyCode, KeyEvent event);

    @Direct
    boolean onKeyUp(int keyCode, KeyEvent event);

    @Direct
    void addTextChangedListener(TextWatcher watcher);

    @Direct
    void removeTextChangedListener(TextWatcher watcher);

    @Direct
    void setOnEditorActionListener(TextView.OnEditorActionListener l);

    @Direct
    void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom);
  }
}
