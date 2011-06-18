package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.util.Pair;

@RunWith(WithTestDefaultsRunner.class)
public class PairTest {

    @Test
    public void testConstructor() throws Exception {
        Pair<String, Integer> pair = new Pair<String, Integer>("a", 1);
        assertThat(pair.first, equalTo("a"));
        assertThat(pair.second, equalTo(1));
    }

    @Test
    public void testStaticCreate() throws Exception {
        Pair<String, String> p = Pair.create("Foo", "Bar");
        assertThat(p.first, equalTo("Foo"));
        assertThat(p.second, equalTo("Bar"));
    }

    @Test
    public void testEquals() throws Exception {
        assertThat(Pair.create("1", 2), equalTo(Pair.create("1", 2)));
    }

    @Test
    public void testHash() throws Exception {
        assertThat(Pair.create("1", 2).hashCode(), equalTo(Pair.create("1", 2).hashCode()));
    }
}
