package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.widget.Spinner;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SpinnerTest {

    private Spinner spinner;

    @Before
    public void beforeTests() {
        spinner = new Spinner(new Activity());
    }

    @Test
    public void testPrompt() {
        spinner.setPrompt("foo");

        assertThat(spinner.getPrompt().toString(), is("foo"));
    }
}
