package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class TypedArrayTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
    }

    @Test
    public void getResources() throws Exception {
        assertNotNull(context.obtainStyledAttributes(null).getResources());
    }

    @Test
    public void getInt_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getInt(0, -1), equalTo(-1));
    }

    @Test
    public void getInteger_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getInteger(0, -1), equalTo(-1));
    }

    @Test
    public void getResourceId_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getResourceId(0, -1), equalTo(-1));
    }

    @Test
    public void getDimension_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getDimension(0, -1f), equalTo(-1f));
    }
}
