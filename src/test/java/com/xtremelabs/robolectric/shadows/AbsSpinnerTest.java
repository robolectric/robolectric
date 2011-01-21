package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.Spinner;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WithTestDefaultsRunner.class)
public class AbsSpinnerTest {
    private Context context;
    private AdapterView adapterView;
	private Spinner spinner;
	private ArrayAdapter<String> arrayAdapter;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        adapterView = new Gallery(context);
		spinner = new Spinner(context);
		String [] testItems = {"foo", "bar"};
		arrayAdapter = new MyArrayAdapter(this.context, testItems);
    }

    @Test
    public void shouldHaveAdapterViewCommonBehavior() throws Exception {
        AdapterViewBehavior.shouldActAsAdapterView(adapterView);
    }

	@Test
	public void checkSetAdapter() {
		spinner.setAdapter(arrayAdapter);
	}

    private static class MyArrayAdapter extends ArrayAdapter<String> {
        public MyArrayAdapter(Context context, String[] testItems) {
            super(context, android.R.layout.simple_spinner_item, testItems);
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            return new View(getContext());
        }
    }
}
