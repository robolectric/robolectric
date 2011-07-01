package com.xtremelabs.robolectric.shadows;

import android.view.MotionEvent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class MotionEventTest {
	
	@Test
	public void addingSecondPointerSetsCount() {
		MotionEvent event = MotionEvent.obtain(100, 200, MotionEvent.ACTION_MOVE, 5.0f, 10.0f, 0);
		assertThat(event.getX(0), equalTo(5.0f));
		assertThat(event.getY(0), equalTo(10.0f));
		assertThat(event.getPointerCount(), equalTo(1));
		
		Robolectric.shadowOf(event).setPointer2( 20.0f, 30.0f );
		
		assertThat(event.getX(1), equalTo(20.0f));
		assertThat(event.getY(1), equalTo(30.0f));
		assertThat(event.getPointerCount(), equalTo(2));
	}
}
