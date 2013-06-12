package org.robolectric.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Transcript {
  private List<String> events = new ArrayList<String>();

  public void add(String event) {
    events.add(event);
  }

  public void assertNoEventsSoFar() {
    assertEquals("Expected no events but got " + events + ".", 0, events.size());
  }

  /**
   * Assert that the transcript contains the expected events, exactly. All events are cleared
   * from the transcript.
   *
   * @param expectedEvents
   */
  public void assertEventsSoFar(String... expectedEvents) {
    assertEquals(Arrays.asList(expectedEvents), events);
    events.clear();
  }

  /**
   * Assert that the transcript contains the expected events in order, but possibly ignoring
   * some actual events. For example, if the transcript contains {A, B, C, D, E}, asserting
   * on {A, C, E} would pass, {A, D, B} would fail, {E} would pass, and {F} would fail. Events
   * up to and including the last expected event are cleared from the transcript.
   *
   * @param expectedEvents
   */
  public void assertEventsInclude(String... expectedEvents) {
    List<String> original = new ArrayList<String>(events);
    for (String expectedEvent : expectedEvents) {
      int index = events.indexOf(expectedEvent);
      if (index == -1) {
        assertEquals(Arrays.asList(expectedEvents), original);
      }
      events.subList(0, index + 1).clear();
    }
  }

  public void clear() {
    events.clear();
  }

  public List<String> getEvents() {
    return events;
  }
}
