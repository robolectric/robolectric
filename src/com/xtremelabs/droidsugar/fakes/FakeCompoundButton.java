package com.xtremelabs.droidsugar.fakes;

import android.widget.Checkable;
import android.widget.CompoundButton;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CompoundButton.class)
public class FakeCompoundButton extends FakeTextView implements Checkable {
    private boolean mmmmmChecked;
    private CompoundButton.OnCheckedChangeListener onCheckedListener;
    private CompoundButton realButton;

    public FakeCompoundButton(CompoundButton realButton) {
        super(realButton);
        this.realButton = realButton;
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
            if(onCheckedListener != null) {
                onCheckedListener.onCheckedChanged(realButton, checked);
            }
        }
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.onCheckedListener = listener;
    }
}
