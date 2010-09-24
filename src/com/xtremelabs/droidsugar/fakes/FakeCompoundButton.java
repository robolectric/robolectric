package com.xtremelabs.droidsugar.fakes;

import android.widget.Checkable;
import android.widget.CompoundButton;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class FakeCompoundButton extends FakeTextView implements Checkable {
    private boolean mmChecked;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public FakeCompoundButton(CompoundButton view) {
        super(view);
    }

    @Override public void toggle() {
        setChecked(!mmChecked);
    }

    @Override public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override public boolean isChecked() {
        return mmChecked;
    }

    @Override public void setChecked(boolean checked) {
        if (mmChecked != checked) {
            mmChecked = checked;

            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged((CompoundButton) realView, mmChecked);
            }
        }
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }
}
