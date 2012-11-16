package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import com.xtremelabs.robolectric.Robolectric;
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

    @Before public void setUp() throws Exception {
        context = new Activity();
    }

    @Test
    public void shouldGetAndSetStringAttributes() throws Exception {
        TypedArray array = Robolectric.newInstanceOf(TypedArray.class);
        ShadowTypedArray shadowArray = Robolectric.shadowOf(array);
        shadowArray.add("expected value");

        assertThat(array.getString(0), equalTo("expected value"));
    }
    
    @Test
    public void getResources() throws Exception {
        assertNotNull(context.obtainStyledAttributes(null).getResources());
    }

    @Test
    public void getInt_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(null).getInt(1, -1), equalTo(-1));
    }

    @Test
    public void getInteger_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(null).getInteger(1, -1), equalTo(-1));
    }

    @Test
    public void getResourceId_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(null).getResourceId(1, -1), equalTo(-1));
    }

    @Test
    public void getDimension_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(null).getDimension(1, -1f), equalTo(-1f));
    }
}
