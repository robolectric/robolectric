package com.xtremelabs.droidsugar.fakes;

import android.widget.Checkable;
import android.widget.CompoundButton;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class FakeCompoundButton extends FakeTextView implements Checkable {
    private boolean mChecked;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public FakeCompoundButton(CompoundButton view) {
        super(view);
    }

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
}
