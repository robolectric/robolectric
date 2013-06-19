package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.widget.AbsSeekBar;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AbsSeekBarTest {
	
	@Test
	public void testInheritance() {
		TestAbsSeekBar seekBar = new TestAbsSeekBar(new Activity());
		ShadowAbsSeekBar shadow = shadowOf(seekBar);
		assertThat(shadow, instanceOf(ShadowProgressBar.class));
	}

    @Test
    public void canSetThumbOffset() throws Exception {
        AbsSeekBar absSeekBar = new TestAbsSeekBar(new Activity());
        absSeekBar.setThumbOffset(97);
        assertThat(shadowOf(absSeekBar).getThumbOffset(), equalTo(97));
    }

	private static class TestAbsSeekBar extends AbsSeekBar {
		
		public TestAbsSeekBar(Context context) {
			super(context);
		}
	}
}
