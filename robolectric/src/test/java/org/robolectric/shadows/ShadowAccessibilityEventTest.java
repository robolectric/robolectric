package org.robolectric.shadows;

import android.os.Parcel;
import android.view.accessibility.AccessibilityEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowAccessibilityEventTest {

  private AccessibilityEvent event;
  private ShadowAccessibilityEvent shadow;

  @Before
  public void setUp() {
    ShadowAccessibilityEvent.resetObtainedInstances();
    assertThat(ShadowAccessibilityEvent.areThereUnrecycledEvents(true)).isEqualTo(false);
    event = ShadowAccessibilityEvent.obtain();
    shadow = shadowOf(event);
    assertThat(shadow != null).isEqualTo(true);
  }

  @Test
  public void shouldHaveObtainedEvent() {
    assertThat(ShadowAccessibilityEvent.areThereUnrecycledEvents(false)).isEqualTo(true);
  }

  @Test
  public void shouldEqualToClonedEvent() {
    shadow.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
    AccessibilityEvent newEvent = ShadowAccessibilityEvent.obtain(event);
    assertThat(shadow.equals(newEvent)).isEqualTo(true);
    newEvent.recycle();
  }

  @Test
  public void shouldWriteAndReadFromParcelCorrectly() {
    Parcel p = Parcel.obtain();
    event.setContentDescription("test");
    event.writeToParcel(p, 0);
    p.setDataPosition(0);
    AccessibilityEvent anotherEvent = AccessibilityEvent.CREATOR.createFromParcel(p);
    assertThat(event).isEqualTo(anotherEvent);
    event.setContentDescription(null);
    anotherEvent.recycle();
  }

  @After
  public void tearDown() {
    shadow.recycle();
    assertThat(ShadowAccessibilityEvent.areThereUnrecycledEvents(true)).isEqualTo(false);
  }
}

