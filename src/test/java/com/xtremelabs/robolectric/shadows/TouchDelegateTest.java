package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class TouchDelegateTest {

	private ShadowTouchDelegate td;
	private Rect rect;
	private View view;
	
	@Before
	public void setUp() throws Exception {
		rect = new Rect( 1, 2, 3, 4 );
		view = new View( Robolectric.application );
		TouchDelegate realTD = new TouchDelegate( rect, view );
		td = Robolectric.shadowOf( realTD );
	}
	
	@Test
	public void testBounds() {
		Rect bounds = td.getBounds();
		assertThat( bounds, equalTo( rect ) );
	}
	
	@Test
	public void tetsDelegateView() {
		View view = td.getDelegateView();
		assertThat( view, equalTo( this.view ) );
	}
	
}
