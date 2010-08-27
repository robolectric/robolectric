package com.xtremelabs.droidsugar.fakes;

import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.TextView;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CheckBox.class)
public class FakeCompoundButton extends FakeTextView implements Checkable {
    private boolean mmmmmChecked;

    public FakeCompoundButton(TextView view) {
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
        }
    }
}
