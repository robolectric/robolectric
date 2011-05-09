// Copyright 2010 Google Inc. All Rights Reserved.

package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ArrayAdapterTest {
    private ArrayAdapter<Integer> arrayAdapter;

    @Before public void setUp() throws Exception {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);

        arrayAdapter = new ArrayAdapter<Integer>(Robolectric.application, 0, list);
    }

    @Test
    public void verifyContext() {
        assertEquals(Robolectric.application, arrayAdapter.getContext());
    }

    @Test
    public void verifyListContent() {
        assertEquals(3, arrayAdapter.getCount());
        assertEquals(new Integer(1), arrayAdapter.getItem(0));
        assertEquals(new Integer(2), arrayAdapter.getItem(1));
        assertEquals(new Integer(3), arrayAdapter.getItem(2));
    }

    @Test
    public void usesTextViewResourceIdToSetTextWithinListItemView() throws Exception {
        ListView parent = new ListView(Robolectric.application);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Robolectric.application, R.layout.main, R.id.title, new String[] { "first value" });
        View listItemView = arrayAdapter.getView(0, null, parent);
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title);
        assertEquals("first value", titleTextView.getText().toString());
    }
}
