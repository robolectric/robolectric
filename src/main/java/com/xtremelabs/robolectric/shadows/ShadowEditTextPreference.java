package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.EditText;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(EditTextPreference.class)
public class ShadowEditTextPreference extends ShadowDialogPreference {

	private EditText mEditText;

	public void __constructor__(Context context) {
		__constructor__(context, null, 0);
	}

	public void __constructor__(Context context, AttributeSet attributeSet) {
		__constructor__(context, attributeSet, 0);
	}

	public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
		super.__constructor__(context, attributeSet, defStyle);

		mEditText = new EditText(context, attrs);
		mEditText.setEnabled(true);
	}

	@Implementation
	public EditText getEditText() {
		return mEditText;
	}

	@Implementation
	public void setText(java.lang.String text) {
		mEditText.setText(text);
	}

}
