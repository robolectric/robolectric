package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;


@RunWith(WithTestDefaultsRunner.class)
public class BundleTest {

    private Bundle bundle;

    @Before public void setUp() throws Exception {
        bundle = new Bundle();
    }

    @Test
    public void testContainsKey() throws Exception {
        assertFalse(bundle.containsKey("foo"));
        bundle.putString("foo", "bar");
        assertTrue(bundle.containsKey("foo"));
    }
    
    @Test
    public void testInt() {
        bundle.putInt("foo", 5);
        assertEquals(5,bundle.getInt("foo"));
        assertEquals(0,bundle.getInt("bar"));
        assertEquals(7, bundle.getInt("bar", 7));
    }
    
    @Test
    public void testLong() {
        bundle.putLong("foo", 5);
        assertEquals(5, bundle.getLong("foo"));
        assertEquals(0,bundle.getLong("bar"));
        assertEquals(7, bundle.getLong("bar", 7));
    }
    
    @Test
    public void testDouble() {
        bundle.putDouble("foo", 5);
        assertEquals(Double.valueOf(5), Double.valueOf(bundle.getDouble("foo")));
        assertEquals(Double.valueOf(0),Double.valueOf(bundle.getDouble("bar")));
        assertEquals(Double.valueOf(7), Double.valueOf(bundle.getDouble("bar", 7)));
    }

    @Test
    public void testBoolean() {
        bundle.putBoolean("foo", true);
        assertEquals(true, bundle.getBoolean("foo"));
        assertEquals(false, bundle.getBoolean("bar"));
        assertEquals(true, bundle.getBoolean("bar", true));
    }

    @Test
    public void testFloat() {
        bundle.putFloat("foo", 5f);
        assertEquals(Float.valueOf(5), Float.valueOf(bundle.getFloat("foo")));
        assertEquals(Float.valueOf(0),Float.valueOf(bundle.getFloat("bar")));
        assertEquals(Float.valueOf(7), Float.valueOf(bundle.getFloat("bar", 7)));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(bundle.isEmpty());
        bundle.putBoolean("foo", true);
        assertFalse(bundle.isEmpty());
    }

    @Test
    public void testStringArray() {
        bundle.putStringArray("foo", new String[] { "a" });
        Assert.assertArrayEquals(new String[] { "a" }, bundle.getStringArray("foo"));
        assertNull(bundle.getStringArray("bar"));
    }
    
    @Test
    public void testStringArrayList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("a");

        bundle.putStringArrayList("foo", new ArrayList<String>(list));
        Assert.assertEquals(list, bundle.getStringArrayList("foo"));
        assertNull(bundle.getStringArrayList("bar"));
    }

    @Test
    public void testIntegerArrayList() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(100);

        bundle.putIntegerArrayList("foo", new ArrayList<Integer>(list));
        Assert.assertEquals(list, bundle.getIntegerArrayList("foo"));
        assertNull(bundle.getIntegerArrayList("bar"));
    }
}
