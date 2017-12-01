package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.os.Parcel;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
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
  public void shouldRecordParcelables() {
    final Notification notification = new Notification();
    event.setParcelableData(notification);
    AccessibilityEvent anotherEvent = AccessibilityEvent.obtain(event);
    assertThat(anotherEvent.getParcelableData() instanceof Notification).isEqualTo(true);
    assertThat(anotherEvent.getParcelableData()).isEqualTo(notification);
    anotherEvent.recycle();
  }

  @Test
  public void shouldBeEqualToClonedEvent() {
    shadow.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
    AccessibilityEvent newEvent = ShadowAccessibilityEvent.obtain(event);
    assertThat(event.equals(newEvent)).isEqualTo(true);
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

  @Test
  public void shouldHaveCurrentSourceId() {
    TextView rootView = new TextView(RuntimeEnvironment.application);
    event.setSource(rootView);
    assertThat(shadow.getSourceRoot()).isEqualTo(rootView);
    assertThat(shadow.getVirtualDescendantId()).isEqualTo(ShadowAccessibilityRecord.NO_VIRTUAL_ID);
    event.setSource(rootView, 1);
    assertThat(shadow.getVirtualDescendantId()).isEqualTo(1);
  }

  @After
  public void tearDown() {
    shadow.recycle();
    assertThat(ShadowAccessibilityEvent.areThereUnrecycledEvents(true)).isEqualTo(false);
  }
}

