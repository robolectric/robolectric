package com.xtremelabs.droidsugar.fakes;

import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CheckBox.class)
public class FakeCompoundButton extends FakeTextView implements Checkable {
    private boolean mmmmmChecked;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public FakeCompoundButton(CompoundButton view) {
        super(view);
    }

    @Override public void toggle() {
        setChecked(!mmmmmChecked);
    }

    @Override public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override public boolean isChecked() {
        return mmmmmChecked;
    }

    @Override public void setChecked(boolean checked) {
        if (mmmmmChecked != checked) {
            mmmmmChecked = checked;

            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged((CompoundButton) realView, mmmmmChecked);
            }
        }
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }
}
