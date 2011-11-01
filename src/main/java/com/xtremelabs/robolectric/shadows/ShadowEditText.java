package com.xtremelabs.robolectric.shadows;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.widget.EditText;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * A shadow for EditText that provides support for listeners
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(EditText.class)
public class ShadowEditText extends ShadowTextView {
    
	public ShadowEditText() {
        focusable = true;
        focusableInTouchMode = true;
    }

    @Override @Implementation(i18nSafe=true)
    public void setText( CharSequence str ) {
    	super.setText( str );
    }
    
    @Override @Implementation
    public Editable getText() {
        CharSequence text = super.getText();
        if (!(text instanceof Editable)) {
            return new SpannableStringBuilder(text);
        }
        return (Editable) text;
    }

}