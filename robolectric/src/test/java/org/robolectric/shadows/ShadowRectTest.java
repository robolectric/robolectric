package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Rect;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowRectTest {
  @Before
  public void setUp() {
  }

  @Test
  public void constructorSetsCoordinates() {
    Rect r = new Rect(1, 2, 3, 4);
    assertThat(r.left).isEqualTo(1);
    assertThat(r.top).isEqualTo(2);
    assertThat(r.right).isEqualTo(3);
    assertThat(r.bottom).isEqualTo(4);
  }

  @Test
  public void secondConstructorSetsCoordinates() {
    Rect existingRect = new Rect(1, 2, 3, 4);
    Rect r = new Rect(existingRect);
    assertThat(r.left).isEqualTo(1);
    assertThat(r.top).isEqualTo(2);
    assertThat(r.right).isEqualTo(3);
    assertThat(r.bottom).isEqualTo(4);
  }

  @Test
  public void width() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.width()).isEqualTo(10);
  }

  @Test
  public void height() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.height()).isEqualTo(10);
  }

  @Test
  public void doesntEqual() {
    Rect a = new Rect(1, 2, 3, 4);
    Rect b = new Rect(2, 3, 4, 5);
    assertThat(a.equals(b)).isFalse();
  }

  @Test
  public void equals() {
    Rect a = new Rect(1, 2, 3, 4);
    Rect b = new Rect(1, 2, 3, 4);
    assertThat(a.equals(b)).isTrue();
  }

  @Test
  public void doesntContainPoint() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.contains(11, 11)).isFalse();
  }

  @Test
  public void containsPoint() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.contains(5, 5)).isTrue();
  }

  @Test
  public void doesntContainPointOnLeftEdge() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.contains(0, 10)).isFalse();
  }

  @Test
  public void doesntContainPointOnRightEdge() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.contains(10, 5)).isFalse();
  }

  @Test
  public void containsPointOnTopEdge() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.contains(5, 0)).isTrue();
  }

  @Test
  public void containsPointOnBottomEdge() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.contains(5, 10)).isFalse();
  }

  @Test
  public void doesntContainRect() {
    Rect a = new Rect(0, 0, 10, 10);
    Rect b = new Rect(11, 11, 12, 12);
    assertThat(a.contains(b)).isFalse();
  }

  @Test
  public void containsRect() {
    Rect a = new Rect(0, 0, 10, 10);
    Rect b = new Rect(8, 8, 9, 9);
    assertThat(a.contains(b)).isTrue();
  }

  @Test
  public void containsEqualRect() {
    Rect a = new Rect(0, 0, 10, 10);
    Rect b = new Rect(0, 0, 10, 10);
    assertThat(a.contains(b)).isTrue();
  }

  @Test
  public void intersectsButDoesntContainRect() {
    Rect a = new Rect(0, 0, 10, 10);
    Rect b = new Rect(5, 5, 15, 15);
    assertThat(a.contains(b)).isFalse();
  }

  @Test
  public void doesntIntersect() {
    Rect a = new Rect(0, 0, 10, 10);
    Rect b = new Rect(11, 11, 21, 21);
    assertThat(Rect.intersects(a, b)).isFalse();
  }

  @Test
  public void intersects() {
    Rect a = new Rect(0, 0, 10, 10);
    Rect b = new Rect(5, 0, 15, 10);
    assertThat(Rect.intersects(a, b)).isTrue();
  }

  @Test
  public void almostIntersects() {
    Rect a = new Rect(3, 0, 4, 2);
    Rect b = new Rect(1, 0, 3, 1);
    assertThat(Rect.intersects(a, b)).isFalse();
  }

  @Test
  public void intersectRect() {
    Rect a = new Rect(0, 0, 10, 10);
    Rect b = new Rect(5, 0, 15, 10);
    assertThat(a.intersect(b)).isTrue();
  }

  @Test
  public void intersectCoordinates() {
    Rect r = new Rect(0, 0, 10, 10);
    assertThat(r.intersect(5, 0, 15, 10)).isTrue();
  }

  @Test
  public void setWithIntsSetsCoordinates() {
    Rect r = new Rect();
    r.set(1, 2, 3, 4);
    assertThat(r.left).isEqualTo(1);
    assertThat(r.top).isEqualTo(2);
    assertThat(r.right).isEqualTo(3);
    assertThat(r.bottom).isEqualTo(4);
  }

  @Test
  public void setWithRectSetsCoordinates() {
    Rect rSrc = new Rect(1, 2, 3, 4);
    Rect r = new Rect();
    r.set(rSrc);
    assertThat(r.left).isEqualTo(1);
    assertThat(r.top).isEqualTo(2);
    assertThat(r.right).isEqualTo(3);
    assertThat(r.bottom).isEqualTo(4);
  }

  @Test
  public void offsetModifiesRect() {
    Rect r = new Rect(1, 2, 3, 4);
    r.offset(10, 20);
    assertThat(r.left).isEqualTo(11);
    assertThat(r.top).isEqualTo(22);
    assertThat(r.right).isEqualTo(13);
    assertThat(r.bottom).isEqualTo(24);
  }
}
