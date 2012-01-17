package com.xtremelabs.robolectric.shadows;

import android.graphics.Rect;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class RectTest {
    @Before
    public void setUp() {
    }

	@Test
	public void constructorSetsCoordinates() {
		Rect r = new Rect(1, 2, 3, 4);
		assertThat(r.left, is(1));
		assertThat(r.top, is(2));
		assertThat(r.right, is(3));
		assertThat(r.bottom, is(4));
	}

	@Test
	public void secondConstructorSetsCoordinates() {
		Rect existingRect = new Rect(1, 2, 3, 4);
    Rect r = new Rect(existingRect);
		assertThat(r.left, is(1));
		assertThat(r.top, is(2));
		assertThat(r.right, is(3));
		assertThat(r.bottom, is(4));
	}

    
    @Test
    public void width() {
    	Rect r = new Rect(0, 0, 10, 10);
        assertThat(r.width(), is(10));
    }
    
    @Test
    public void height() {
    	Rect r = new Rect(0, 0, 10, 10);
        assertThat(r.height(), is(10));
    }
    
	@Test
	public void doesntEqual() {
		Rect a = new Rect(1, 2, 3, 4);
		Rect b = new Rect(2, 3, 4, 5);
		assertThat(a.equals(b), is(false));
	}
	
	@Test
	public void equals() {
		Rect a = new Rect(1, 2, 3, 4);
		Rect b = new Rect(1, 2, 3, 4);
		assertThat(a.equals(b), is(true));
	}

	@Test
	public void doesntContainPoint() {
		Rect r = new Rect(0, 0, 10, 10);
		assertThat(r.contains(11, 11), is(false));
	}
	
	@Test
	public void containsPoint() {
		Rect r = new Rect(0, 0, 10, 10);
		assertThat(r.contains(5, 5), is(true));
	}

	@Test
	public void doesntContainPointOnLeftEdge() {
		Rect r = new Rect(0, 0, 10, 10);
		assertThat(r.contains(0, 5), is(false));
	}

	@Test
	public void doesntContainPointOnRightEdge() {
		Rect r = new Rect(0, 0, 10, 10);
		assertThat(r.contains(10, 5), is(false));
	}

	@Test
	public void containsPointOnTopEdge() {
		Rect r = new Rect(0, 0, 10, 10);
		assertThat(r.contains(5, 0), is(true));
	}

	@Test
	public void containsPointOnBottomEdge() {
		Rect r = new Rect(0, 0, 10, 10);
		assertThat(r.contains(5, 10), is(true));
	}

	@Test
	public void doesntContainRect() {
		Rect a = new Rect(0, 0, 10, 10);
		Rect b = new Rect(11, 11, 12, 12);
		assertThat(a.contains(b), is(false));
	}
	
	@Test
	public void containsRect() {
		Rect a = new Rect(0, 0, 10, 10);
		Rect b = new Rect(8, 8, 9, 9);
		assertThat(a.contains(b), is(true));
	}
	
	@Test
	public void containsEqualRect() {
		Rect a = new Rect(0, 0, 10, 10);
		Rect b = new Rect(0, 0, 10, 10);
		assertThat(a.contains(b), is(true));
	}
	
	@Test
	public void intersectsButDoesntContainRect() {
		Rect a = new Rect(0, 0, 10, 10);
		Rect b = new Rect(5, 5, 15, 15);
		assertThat(a.contains(b), is(false));
	}

	@Test
	public void doesntIntersect() {
		Rect a = new Rect(0, 0, 10, 10);
		Rect b = new Rect(11, 11, 21, 21);
		assertThat(Rect.intersects(a, b), is(false));
	}
	
	@Test
	public void intersects() {
		Rect a = new Rect(0, 0, 10, 10);
		Rect b = new Rect(5, 0, 15, 10);
		assertThat(Rect.intersects(a, b), is(true));
	}
	
	@Test
	public void almostIntersects() {
		Rect a = new Rect(3, 0, 4, 2);
		Rect b = new Rect(1, 0, 3, 1);
		assertThat(Rect.intersects(a, b), is(false));	
	}
	
	@Test
	public void intersectRect() {
		Rect a = new Rect(0, 0, 10, 10);
		Rect b = new Rect(5, 0, 15, 10);
		assertThat(a.intersect(b), is(true));
	}
	
	@Test
	public void intersectCoordinates() {
		Rect r = new Rect(0, 0, 10, 10);
		assertThat(r.intersect(5, 0, 15, 10), is(true));
	}

	@Test
	public void setWithIntsSetsCoordinates() {
		Rect r = new Rect();
		r.set(1, 2, 3, 4);
		assertThat(r.left, is(1));
		assertThat(r.top, is(2));
		assertThat(r.right, is(3));
		assertThat(r.bottom, is(4));
	}

	@Test
	public void setWithRectSetsCoordinates() {
		Rect rSrc = new Rect(1, 2, 3, 4);
		Rect r = new Rect();
		r.set(rSrc);
		assertThat(r.left, is(1));
		assertThat(r.top, is(2));
		assertThat(r.right, is(3));
		assertThat(r.bottom, is(4));
	}
	
	@Test
	public void offsetModifiesRect() {
	  Rect r = new Rect(1, 2, 3, 4);
	  r.offset(10, 20);
		assertThat(r.left, is(11));
		assertThat(r.top, is(22));
		assertThat(r.right, is(13));
		assertThat(r.bottom, is(24));
	}
}
