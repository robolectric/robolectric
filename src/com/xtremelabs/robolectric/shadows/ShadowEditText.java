package com.xtremelabs.robolectric.shadows;

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
public class ShadowEditText extends ShadowTextView {
    private List<TextWatcher> watchers = new ArrayList<TextWatcher>();

    public ShadowEditText() {
        focusable = true;
        focusableInTouchMode = true;
    }

    @Override @Implementation
    public void setText(CharSequence text) {
        super.setText(text);
        for (TextWatcher watcher : watchers) {
            watcher.afterTextChanged(getText());
        }
    }

    @Override @Implementation
    public void setText(int textResourceId) {
        super.setText(textResourceId);
        for (TextWatcher watcher : watchers) {
            watcher.afterTextChanged(getText());
        }
    }

    @Override @Implementation
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

    public List<TextWatcher> getWatchers() {
        return watchers;
    }
}
