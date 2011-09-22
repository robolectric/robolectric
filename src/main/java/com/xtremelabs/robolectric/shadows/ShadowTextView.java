package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.TransformationMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.text.method.MovementMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextView.class)
public class ShadowTextView extends ShadowView {
    private CharSequence text = "";
    private CompoundDrawables compoundDrawablesImpl;
    private Integer textColorHexValue;
    private Integer hintColorHexValue;
    private float textSize = 14.0f;
    private boolean autoLinkPhoneNumbers;
    private int autoLinkMask;
    private CharSequence hintText;
    private int compoundDrawablePadding;
    private MovementMethod movementMethod;
    private boolean linksClickable;
    private int gravity;
    private TextView.OnEditorActionListener onEditorActionListener;
    private int imeOptions = EditorInfo.IME_NULL;
    private int textAppearanceId;
    private TransformationMethod transformationMethod;
    private int inputType;

    @Override
    public void applyAttributes() {
        super.applyAttributes();
        applyTextAttribute();
        applyTextColorAttribute();
        applyHintAttribute();
        applyHintColorAttribute();
        applyCompoundDrawablesWithIntrinsicBoundsAttributes();
    }

    @Implementation(i18nSafe=false)
    public void setText(CharSequence text) {
        if (text == null) {
            text = "";
        }
        this.text = text;
    }

    @Implementation
    public void setText(int textResourceId) {
        this.text = getResources().getText(textResourceId);
    }

    @Implementation
    public CharSequence getText() {
        return text;
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
    public void setInputType(int type){
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

    @Implementation(i18nSafe=false)
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
        if (onKeyListener != null) {
            return onKeyListener.onKey(realView, keyCode, event);
        } else {
            return false;
        }
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

    @Override @Implementation
    public boolean equals(Object o) {
        return super.equals(shadowOf_(o));
    }

    @Override @Implementation
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

    public void triggerEditorAction(int imeAction) {
        onEditorActionListener.onEditorAction((TextView) realView, imeAction, null);
    }

    @Implementation
    public void setTransformationMethod(TransformationMethod transformationMethod) {
        this.transformationMethod = transformationMethod;
    }

    @Implementation
    public TransformationMethod getTransformationMethod() {
        return transformationMethod;
    }

    public static class CompoundDrawables {
        public int left;
        public int top;
        public int right;
        public int bottom;

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
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CompoundDrawables that = (CompoundDrawables) o;

            if (bottom != that.bottom) return false;
            if (left != that.left) return false;
            if (right != that.right) return false;
            if (top != that.top) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = left;
            result = 31 * result + top;
            result = 31 * result + right;
            result = 31 * result + bottom;
            return result;
        }

        @Override
        public String toString() {
            return "CompoundDrawables{" +
                    "left=" + left +
                    ", top=" + top +
                    ", right=" + right +
                    ", bottom=" + bottom +
                    '}';
        }
    }
}
