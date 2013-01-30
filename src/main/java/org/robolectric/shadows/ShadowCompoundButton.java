package com.xtremelabs.robolectric.shadows;

import android.widget.Checkable;
import android.widget.CompoundButton;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the {@code android.widget.CompoundButton} class.
 * <p/>
 * Keeps track of whether or not its "checked" state is set and deals with listeners in an appropriate way.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class ShadowCompoundButton extends ShadowTextView implements Checkable {
    private boolean checked;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    @Override public void applyAttributes() {
        super.applyAttributes();
        setChecked(this.attributeSet.getAttributeBooleanValue("android", "checked", false));
    }

    @Implementation
    @Override public void toggle() {
        setChecked(!checked);
    }

    @Implementation
    @Override public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Implementation
    @Override public boolean isChecked() {
        return checked;
    }

    @Implementation
    @Override public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged((CompoundButton) realView, this.checked);
            }
        }
    }

    @Implementation
    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }
    
    public CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
    	return onCheckedChangeListener;
    }
}
