package com.xtremelabs.robolectric.shadows;

import android.widget.Checkable;
import android.widget.CompoundButton;
import com.xtremelabs.robolectric.res.ViewLoader;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

/**
 * Shadows the {@code android.widget.CompoundButton} class.
 * <p/>
 * Keeps track of whether or not its "checked" state is set and deals with listeners in an appropriate way.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class ShadowCompoundButton extends ShadowTextView implements Checkable {
    private boolean mChecked;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    @Implementation
    @Override public void toggle() {
        setChecked(!mChecked);
    }

    @Implementation
    @Override public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Implementation
    @Override public boolean isChecked() {
        return mChecked;
    }

    @Implementation
    @Override public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;

            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged((CompoundButton) realView, mChecked);
            }
        }
    }

    @Implementation
    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override public void applyViewNode(ViewLoader.ViewNode viewNode) {
        super.applyViewNode(viewNode);
        applyCheckedAttribute(viewNode);
    }

    private void applyCheckedAttribute(ViewLoader.ViewNode viewNode) {
        String text = viewNode.getAttributeValue("android:checked");
        if (text != null) {
            setChecked(Boolean.valueOf(text));
        }
    }
}
