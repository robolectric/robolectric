package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowAutoCompleteTextViewTest {

    @Test
    public void shouldStoreAdapter() {
        AutoCompleteTextView autoCompleteTextView =
                new AutoCompleteTextView(Robolectric.application);
        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(Robolectric.application, 0);

        autoCompleteTextView.setAdapter(adapter);

        assertSame(adapter, autoCompleteTextView.getAdapter());
    }

    @Test
    public void shouldHaveDefaultThresholdOfTwo() {
        AutoCompleteTextView autoCompleteTextView =
                new AutoCompleteTextView(Robolectric.application);

        assertEquals(2, autoCompleteTextView.getThreshold());
    }

    @Test
    public void shouldStoreThreshold() {
        AutoCompleteTextView autoCompleteTextView =
                new AutoCompleteTextView(Robolectric.application);

        autoCompleteTextView.setThreshold(123);

        assertEquals(123, autoCompleteTextView.getThreshold());
    }

    @Test
    public void shouldNotStoreThresholdLessThanOne() {
        AutoCompleteTextView autoCompleteTextView =
                new AutoCompleteTextView(Robolectric.application);

        autoCompleteTextView.setThreshold(-1);

        assertEquals(1, autoCompleteTextView.getThreshold());
    }

    @Test
    public void shouldStoreOnItemClickListener() {
        AutoCompleteTextView autoCompleteTextView =
                new AutoCompleteTextView(Robolectric.application);
        OnItemClickListener listener = mock(OnItemClickListener.class);

        autoCompleteTextView.setOnItemClickListener(listener);

        assertSame(listener, autoCompleteTextView.getOnItemClickListener());
    }

    @Test
    public void shouldReplaceTextAndUpdateSelection() {
        String text = "hello world";
        ReplaceableAutoCompleteTextView autoCompleteTextView =
                new ReplaceableAutoCompleteTextView(Robolectric.application);

        autoCompleteTextView.publicReplaceText(text);

        assertEquals(text, autoCompleteTextView.getText().toString());
        assertEquals(text.length(), autoCompleteTextView.getSelectionStart());
        assertEquals(text.length(), autoCompleteTextView.getSelectionEnd());
    }

    private static class ReplaceableAutoCompleteTextView extends AutoCompleteTextView {

        public ReplaceableAutoCompleteTextView(Context context) {
            super(context);
        }

        public void publicReplaceText(CharSequence text) {
            replaceText(text);
        }
    }
}