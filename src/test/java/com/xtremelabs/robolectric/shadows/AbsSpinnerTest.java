package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.test.mock.MockContext;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WithTestDefaultsRunner.class)
public class AbsSpinnerTest {
    private AdapterView adapterView;
	private Spinner spinner;
	private ArrayAdapter<String> arrayAdapter;
	private Context context;

    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
		context = new MockContext();
        adapterView = new Gallery(new Activity());
		spinner = new Spinner(context);
		String [] testItems = {"foo", "bar"};
		arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, testItems);
    }

    @Test
    public void shouldHaveAdapterViewCommonBehavior() throws Exception {
        AdapterViewBehavior.shouldActAsAdapterView(adapterView);
    }

	@Test
	public void checkSetAdapter()
	{
		spinner.setAdapter(arrayAdapter);
	}
}
