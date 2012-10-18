package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
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
}
