package com.xtremelabs.robolectric.shadows;

import android.view.MotionEvent;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class MotionEventTest {
    private MotionEvent event;
    private ShadowMotionEvent shadowMotionEvent;

    @Before
    public void setUp() throws Exception {
        event = MotionEvent.obtain(100, 200, MotionEvent.ACTION_MOVE, 5.0f, 10.0f, 0);
        shadowMotionEvent = shadowOf(event);
    }

    @Test
	public void addingSecondPointerSetsCount() {
        assertThat(event.getX(0), equalTo(5.0f));
		assertThat(event.getY(0), equalTo(10.0f));
		assertThat(event.getPointerCount(), equalTo(1));
		
		shadowOf(event).setPointer2( 20.0f, 30.0f );
		
		assertThat(event.getX(1), equalTo(20.0f));
		assertThat(event.getY(1), equalTo(30.0f));
		assertThat(event.getPointerCount(), equalTo(2));
	}
    
    @Test
    public void canSetPointerIdsByIndex() {
        shadowMotionEvent.setPointer2(20.0f, 30.0f);
        shadowMotionEvent.setPointerIds(2, 5);
        assertEquals(2, event.getPointerId(0));
        assertEquals(5, event.getPointerId(1));
    }

    @Test
    public void indexShowsUpInAction() {
        shadowMotionEvent.setPointerIndex(1);
        assertEquals(1 << MotionEvent.ACTION_POINTER_ID_SHIFT | MotionEvent.ACTION_MOVE, event.getAction());
    }
    
    @Test
    public void canGetActionIndex() {
        assertEquals(0, event.getActionIndex());
        shadowMotionEvent.setPointerIndex(1);
        assertEquals(1, event.getActionIndex());
    }

    @Test
    public void getActionMaskedStripsPointerIndexFromAction() {
        assertEquals(MotionEvent.ACTION_MOVE, event.getActionMasked());
        shadowMotionEvent.setPointerIndex(1);
        assertEquals(MotionEvent.ACTION_MOVE, event.getActionMasked());
    }
    
    @Test
    public void canFindPointerIndexFromId() {
        shadowMotionEvent.setPointer2(20.0f, 30.0f);
        shadowMotionEvent.setPointerIds(2, 1);
        assertEquals(0, event.findPointerIndex(2));
        assertEquals(1, event.findPointerIndex(1));
        assertEquals(-1, event.findPointerIndex(3));
    }

    @Test
    public void canSetMotionEventLocation() throws Exception {
        assertEquals(5.0f, event.getX(), 0.0f);
        assertEquals(10.0f, event.getY(), 0.0f);
        shadowMotionEvent.setLocation(10.0f, 20.0f);
        assertEquals(10.0f, event.getX(), 0.0f);
        assertEquals(20.0f, event.getY(), 0.0f);
    }
}
