package com.xtremelabs.robolectric.util;

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

    public void assertEventsSoFar(String... expectedEvents) {
        assertEquals(Arrays.asList(expectedEvents), events);
        events.clear();
    }

    public void clear() {
        events.clear();
    }

    public List<String> getEvents() {
        return events;
    }
}
