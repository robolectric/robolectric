package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.*;
import android.text.method.MovementMethod;
import android.text.method.TransformationMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextView.class)
public class ShadowTextView extends ShadowView {
    private CharSequence text = "";
    private CompoundDrawables compoundDrawablesImpl = new CompoundDrawables(0, 0, 0, 0);
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
    private TextView.OnEditorActionListener onEditorActionListener;
    private int imeOptions = EditorInfo.IME_NULL;
    private int textAppearanceId;
    private TransformationMethod transformationMethod;
    private int inputType;
    protected int selectionStart = -1;
    protected int selectionEnd = -1;
    private Typeface typeface;
    private InputFilter[] inputFilters;
    private TextPaint textPaint = new TextPaint();

    private List<TextWatcher> watchers = new ArrayList<TextWatcher>();
    private List<Integer> previousKeyCodes = new ArrayList<Integer>();
    private List<KeyEvent> previousKeyEvents = new ArrayList<KeyEvent>();
    private Layout layout;

    @Override
    public void applyAttributes() {
        super.applyAttributes();
        applyTextAttribute();
        applyTextColorAttribute();
        applyHintAttribute();
        applyHintColorAttribute();
        applyCompoundDrawablesWithIntrinsicBoundsAttributes();
    }

    @Implementation(i18nSafe = false)
    public void setText(CharSequence text) {
        if (text == null) {
            text = "";
        }

        sendBeforeTextChanged(text);

        CharSequence oldValue = this.text;
        this.text = text;

        sendOnTextChanged(oldValue);
        sendAfterTextChanged();
    }

    @Implementation
    public final void append(CharSequence text) {
        boolean isSelectStartAtEnd = selectionStart == this.text.length();
        boolean isSelectEndAtEnd = selectionEnd == this.text.length();
        CharSequence oldValue = this.text;
        StringBuffer sb = new StringBuffer(this.text);
        sb.append(text);

        sendBeforeTextChanged(sb.toString());
        this.text = sb.toString();

        if (isSelectStartAtEnd) {
            selectionStart = this.text.length();
        }
        if (isSelectEndAtEnd) {
            selectionEnd = this.text.length();
        }

        sendOnTextChanged(oldValue);
        sendAfterTextChanged();
    }

    @Implementation
    public void setText(int textResourceId) {
        sendBeforeTextChanged(text);

        CharSequence oldValue = this.text;
        this.text = getResources().getText(textResourceId);

        sendOnTextChanged(oldValue);
        sendAfterTextChanged();
    }

    private void sendAfterTextChanged() {
        for (TextWatcher watcher : watchers) {
            watcher.afterTextChanged(new SpannableStringBuilder(getText()));
        }
    }

    private void sendOnTextChanged(CharSequence oldValue) {
        for (TextWatcher watcher : watchers) {
            watcher.onTextChanged(text, 0, oldValue.length(), text.length());
        }
    }

    private void sendBeforeTextChanged(CharSequence newValue) {
        for (TextWatcher watcher : watchers) {
            watcher.beforeTextChanged(this.text, 0, this.text.length(), newValue.length());
        }
    }

    @Implementation
    public CharSequence getText() {
        return text;
    }

    @Implementation
    public Typeface getTypeface() {
        return typeface;
    }

    @Implementation
    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    @Implementation
    public int length() {
        return text.length();
    }

    @Implementation
    public void setTextColor(int color) {
        textColorHexValue = color;
    }

    @Implementation
    public void setTextSize(float size) {
        textSize = size;
    }

    @Implementation
    public void setTextAppearance(Context context, int resid) {
        textAppearanceId = resid;
    }

    @Implementation
    public void setInputType(int type) {
        this.inputType = type;
    }

    @Implementation
    public int getInputType() {
        return this.inputType;
    }

    @Implementation
    public final void setHint(int resId) {
        this.hintText = getResources().getText(resId);
    }

    @Implementation(i18nSafe = false)
    public final void setHint(CharSequence hintText) {
        this.hintText = hintText;
    }

    @Implementation
    public CharSequence getHint() {
        return hintText;
    }

    @Implementation
    public final void setHintTextColor(int color) {
        hintColorHexValue = color;
    }

    @Implementation
    public final boolean getLinksClickable() {
        return linksClickable;
    }

    @Implementation
    public final void setLinksClickable(boolean whether) {
        linksClickable = whether;
    }

    @Implementation
    public final MovementMethod getMovementMethod() {
        return movementMethod;
    }

    @Implementation
    public final void setMovementMethod(MovementMethod movement) {
        movementMethod = movement;
    }

    @Implementation
    public URLSpan[] getUrls() {
        String[] words = text.toString().split("\\s+");
        List<URLSpan> urlSpans = new ArrayList<URLSpan>();
        for (String word : words) {
            if (word.startsWith("http://")) {
                urlSpans.add(new URLSpan(word));
            }
        }
        return urlSpans.toArray(new URLSpan[urlSpans.size()]);
    }

    @Implementation
    public final void setAutoLinkMask(int mask) {
        autoLinkMask = mask;

        autoLinkPhoneNumbers = (mask & Linkify.PHONE_NUMBERS) != 0;
    }

    @Implementation
    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        compoundDrawablesImpl = new CompoundDrawables(left, top, right, bottom);
    }

    @Implementation
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top,
                                                        Drawable right, Drawable bottom) {
        compoundDrawablesImpl = new CompoundDrawables(left, top, right, bottom);
    }

    @Implementation
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        compoundDrawablesImpl = new CompoundDrawables(left, top, right, bottom);
    }

    @Implementation
    public Drawable[] getCompoundDrawables() {
        if (compoundDrawablesImpl == null) {
            return new Drawable[]{null, null, null, null};
        }
        return new Drawable[]{
                compoundDrawablesImpl.leftDrawable,
                compoundDrawablesImpl.topDrawable,
                compoundDrawablesImpl.rightDrawable,
                compoundDrawablesImpl.bottomDrawable
        };
    }

    @Implementation
    public void setCompoundDrawablePadding(int compoundDrawablePadding) {
        this.compoundDrawablePadding = compoundDrawablePadding;
    }

    @Implementation
    public int getCompoundDrawablePadding() {
        return compoundDrawablePadding;
    }

    @Implementation
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        previousKeyCodes.add(keyCode);
        previousKeyEvents.add(event);
        if (onKeyListener != null) {
            return onKeyListener.onKey(realView, keyCode, event);
        } else {
            return false;
        }
    }

    @Implementation
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        previousKeyCodes.add(keyCode);
        previousKeyEvents.add(event);
        if (onKeyListener != null) {
            return onKeyListener.onKey(realView, keyCode, event);
        } else {
            return false;
        }
    }

    public int getPreviousKeyCode(int index) {
        return previousKeyCodes.get(index);
    }

    public KeyEvent getPreviousKeyEvent(int index) {
        return previousKeyEvents.get(index);
    }

    @Implementation
    public int getGravity() {
        return gravity;
    }

    @Implementation
    public void setGravity(int gravity) {
        this.gravity = gravity;
    }


    @Implementation
    public int getImeOptions() {
        return imeOptions;
    }

    @Implementation
    public void setImeOptions(int imeOptions) {
        this.imeOptions = imeOptions;
    }

    /**
     * Returns the text string of this {@code TextView}.
     * <p/>
     * Robolectric extension.
     */
    @Override
    public String innerText() {
        return (text == null || getVisibility() != VISIBLE) ? "" : text.toString();
    }
    
    @Implementation
    public void setError(CharSequence error) {
      errorText = error;
    }
    
    @Implementation
    public CharSequence getError() {
      return errorText;
    }

    @Override
    @Implementation
    public boolean equals(Object o) {
        return super.equals(shadowOf_(o));
    }

    @Override
    @Implementation
    public int hashCode() {
        return super.hashCode();
    }

    public CompoundDrawables getCompoundDrawablesImpl() {
        return compoundDrawablesImpl;
    }

    void setCompoundDrawablesImpl(CompoundDrawables compoundDrawablesImpl) {
        this.compoundDrawablesImpl = compoundDrawablesImpl;
    }

    public Integer getTextColorHexValue() {
        return textColorHexValue;
    }

    public int getTextAppearanceId() {
        return textAppearanceId;
    }

    public Integer getHintColorHexValue() {
        return hintColorHexValue;
    }

    @Implementation
    public float getTextSize() {
        return textSize;
    }

    public boolean isAutoLinkPhoneNumbers() {
        return autoLinkPhoneNumbers;
    }

    private void applyTextAttribute() {
        String text = attributeSet.getAttributeValue("android", "text");
        if (text != null) {
            if (text.startsWith("@string/")) {
                int textResId = attributeSet.getAttributeResourceValue("android", "text", 0);
                text = context.getResources().getString(textResId);
            }
            setText(text);
        }
    }

    private void applyTextColorAttribute() {
        String colorValue = attributeSet.getAttributeValue("android", "textColor");
        if (colorValue != null) {
            if (colorValue.startsWith("@color/") || colorValue.startsWith("@android:color/")) {
                int colorResId = attributeSet.getAttributeResourceValue("android", "textColor", 0);
                setTextColor(context.getResources().getColor(colorResId));
            } else if (colorValue.startsWith("#")) {
                int colorFromHex = (int) Long.valueOf(colorValue.replaceAll("#", ""), 16).longValue();
                setTextColor(colorFromHex);
            }
        }
    }

    private void applyHintAttribute() {
        String hint = attributeSet.getAttributeValue("android", "hint");
        if (hint != null) {
            if (hint.startsWith("@string/")) {
                int textResId = attributeSet.getAttributeResourceValue("android", "hint", 0);
                hint = context.getResources().getString(textResId);

            }
            setHint(hint);
        }
    }

    private void applyHintColorAttribute() {
        String colorValue = attributeSet.getAttributeValue("android", "hintColor");
        if (colorValue != null) {
            if (colorValue.startsWith("@color/") || colorValue.startsWith("@android:color/")) {
                int colorResId = attributeSet.getAttributeResourceValue("android", "hintColor", 0);
                setHintTextColor(context.getResources().getColor(colorResId));
            } else if (colorValue.startsWith("#")) {
                int colorFromHex = (int) Long.valueOf(colorValue.replaceAll("#", ""), 16).longValue();
                setHintTextColor(colorFromHex);
            }
        }
    }

    private void applyCompoundDrawablesWithIntrinsicBoundsAttributes() {
        setCompoundDrawablesWithIntrinsicBounds(
                attributeSet.getAttributeResourceValue("android", "drawableLeft", 0),
                attributeSet.getAttributeResourceValue("android", "drawableTop", 0),
                attributeSet.getAttributeResourceValue("android", "drawableRight", 0),
                attributeSet.getAttributeResourceValue("android", "drawableBottom", 0));
    }

    @Implementation
    public void setOnEditorActionListener(android.widget.TextView.OnEditorActionListener onEditorActionListener) {
        this.onEditorActionListener = onEditorActionListener;
    }

    public boolean triggerEditorAction(int imeAction) {
        if (onEditorActionListener != null) {
            return onEditorActionListener.onEditorAction((TextView) realView, imeAction, null);
        }
        return false;
    }

    @Implementation
    public void setTransformationMethod(TransformationMethod transformationMethod) {
        this.transformationMethod = transformationMethod;
    }

    @Implementation
    public TransformationMethod getTransformationMethod() {
        return transformationMethod;
    }

    @Implementation
    public void addTextChangedListener(TextWatcher watcher) {
        this.watchers.add(watcher);
    }

    @Implementation
    public void removeTextChangedListener(TextWatcher watcher) {
        this.watchers.remove(watcher);
    }

    @Implementation
    public TextPaint getPaint() {
        return textPaint;
    }

    @Implementation
    public Layout getLayout() {
        return this.layout;
    }

    public void setSelection(int index) {
        setSelection(index, index);
    }

    public void setSelection(int start, int end) {
        selectionStart = start;
        selectionEnd = end;
    }

    @Implementation
    public int getSelectionStart() {
        return selectionStart;
    }

    @Implementation
    public int getSelectionEnd() {
        return selectionEnd;
    }

    @Implementation
    public void setFilters(InputFilter[] inputFilters) {
        this.inputFilters = inputFilters;
    }

    @Implementation
    public InputFilter[] getFilters() {
        return this.inputFilters;
    }

    @Implementation
    public boolean hasSelection() {
        return selectionStart >= 0 && selectionEnd >= 0;
    }

    @Implementation
    public boolean onTouchEvent(MotionEvent event) {
        boolean superResult = super.onTouchEvent(event);

        if (movementMethod != null) {
            boolean handled = movementMethod.onTouchEvent(null, null, event);

            if (handled) {
                return true;
            }
        }

        return superResult;
    }

    /**
     * @return the list of currently registered watchers/listeners
     */
    public List<TextWatcher> getWatchers() {
        return watchers;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    protected void dumpAttributes(PrintStream out) {
        super.dumpAttributes(out);
        CharSequence text = getText();
        if (text != null && text.length() > 0) {
            dumpAttribute(out, "text", text.toString());
        }
    }

    public static class CompoundDrawables {
        public Drawable leftDrawable;
        public Drawable topDrawable;
        public Drawable rightDrawable;
        public Drawable bottomDrawable;

        public CompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
            leftDrawable = left;
            topDrawable = top;
            rightDrawable = right;
            bottomDrawable = bottom;
        }

        public CompoundDrawables(int left, int top, int right, int bottom) {
            leftDrawable = left != 0 ? ShadowDrawable.createFromResourceId(left) : null;
            topDrawable = top != 0 ? ShadowDrawable.createFromResourceId(top) : null;
            rightDrawable = right != 0 ? ShadowDrawable.createFromResourceId(right) : null;
            bottomDrawable = bottom != 0 ? ShadowDrawable.createFromResourceId(bottom) : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CompoundDrawables that = (CompoundDrawables) o;

            if (getBottom() != that.getBottom()) return false;
            if (getLeft() != that.getLeft()) return false;
            if (getRight() != that.getRight()) return false;
            if (getTop() != that.getTop()) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = getLeft();
            result = 31 * result + getTop();
            result = 31 * result + getRight();
            result = 31 * result + getBottom();
            return result;
        }

        @Override
        public String toString() {
            return "CompoundDrawables{" +
                    "left=" + getLeft() +
                    ", top=" + getTop() +
                    ", right=" + getRight() +
                    ", bottom=" + getBottom() +
                    '}';
        }

        public int getLeft() {
            return shadowOf(leftDrawable).getLoadedFromResourceId();
        }

        public int getTop() {
            return shadowOf(topDrawable).getLoadedFromResourceId();
        }

        public int getRight() {
            return shadowOf(rightDrawable).getLoadedFromResourceId();
        }

        public int getBottom() {
            return shadowOf(bottomDrawable).getLoadedFromResourceId();
        }
    }
}
