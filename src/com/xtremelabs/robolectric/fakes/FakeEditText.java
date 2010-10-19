package com.xtremelabs.robolectric.fakes;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.widget.EditText;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(EditText.class)
public class FakeEditText extends FakeTextView {
    public List<TextWatcher> watchers = new ArrayList<TextWatcher>();

    public FakeEditText(EditText view) {
        super(view);

        focusable = true;
    }

    @Implementation
    @Override
    public void setText(CharSequence text) {
        super.setText(text);
        for (TextWatcher watcher : watchers) {
            watcher.afterTextChanged(getText());
        }
    }

    @Implementation
    @Override
    public void setText(int textResourceId) {
        super.setText(textResourceId);
        for (TextWatcher watcher : watchers) {
            watcher.afterTextChanged(getText());
        }
    }

    @Implementation
    @Override
    public Editable getText() {
        CharSequence text = super.getText();
        if (!(text instanceof Editable)) {
            return new SpannableStringBuilder(text);
        }
        return (Editable) text;
    }

    @Implementation
    public void addTextChangedListener(TextWatcher watcher) {
        this.watchers.add(watcher);
    }
}
