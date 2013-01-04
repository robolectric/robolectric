package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.util.Join;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResourceRemapperTest {

    private ResourceRemapper resourceRemapper = new ResourceRemapper();

    @Test
    public void shouldRemapNonFinalIntsInRClasses() throws Exception {
        resetInitialState();

        resourceRemapper.remapRClass(android.R.class);
        resourceRemapper.remapRClass(com.xtremelabs.robolectric.lib1.R.class);
        resourceRemapper.remapRClass(com.xtremelabs.robolectric.lib2.R.class);
        resourceRemapper.remapRClass(com.xtremelabs.robolectric.lib3.R.class);

        assertUnique(
                com.xtremelabs.robolectric.lib1.R.id.button,
                com.xtremelabs.robolectric.lib2.R.id.button,
                com.xtremelabs.robolectric.lib3.R.id.button);

        assertUnique(
                com.xtremelabs.robolectric.lib1.R.id.lib1_button,
                com.xtremelabs.robolectric.lib2.R.id.lib2_button,
                com.xtremelabs.robolectric.lib3.R.id.lib3_button);

        assertUnique(
                com.xtremelabs.robolectric.lib1.R.attr.offsetX,
                com.xtremelabs.robolectric.lib2.R.attr.offsetX,
                com.xtremelabs.robolectric.lib3.R.attr.offsetX);

        assertUnique(
                com.xtremelabs.robolectric.lib1.R.attr.offsetY,
                com.xtremelabs.robolectric.lib2.R.attr.offsetY,
                com.xtremelabs.robolectric.lib3.R.attr.offsetY);

        assertEquals(asIntList(com.xtremelabs.robolectric.lib1.R.styleable.Image),
                asList(com.xtremelabs.robolectric.lib1.R.attr.offsetX, com.xtremelabs.robolectric.lib1.R.attr.offsetY));

        assertEquals(asIntList(com.xtremelabs.robolectric.lib2.R.styleable.Image),
                asList(com.xtremelabs.robolectric.lib2.R.attr.offsetX, com.xtremelabs.robolectric.lib2.R.attr.offsetY));

        assertEquals(asIntList(com.xtremelabs.robolectric.lib3.R.styleable.Image),
                asList(com.xtremelabs.robolectric.lib3.R.attr.offsetX, com.xtremelabs.robolectric.lib3.R.attr.offsetY));

        assertEquals(1, com.xtremelabs.robolectric.lib1.R.styleable.one);
        assertEquals(2, com.xtremelabs.robolectric.lib1.R.styleable.two);
    }

    private void assertUnique(int... values) {
        HashSet<Integer> integers = new HashSet<Integer>();
        for (int value : values) {
            if (!integers.add(value)) {
                fail(Join.join(", ", asIntList(values)) + " contained " + value + " twice");
            }
        }
    }

    private void resetInitialState() {
        com.xtremelabs.robolectric.lib1.R.id.button = 0x7f010001;
        com.xtremelabs.robolectric.lib2.R.id.button = 0x7f010001;
        com.xtremelabs.robolectric.lib3.R.id.button = 0x7f010001;

        com.xtremelabs.robolectric.lib1.R.id.lib1_button = 0x7f010002;
        com.xtremelabs.robolectric.lib2.R.id.lib2_button = 0x7f010002;
        com.xtremelabs.robolectric.lib3.R.id.lib3_button = 0x7f010002;

        com.xtremelabs.robolectric.lib1.R.styleable.Image = new int[] {0x7f010070, 0x7f010071};
        com.xtremelabs.robolectric.lib2.R.styleable.Image = new int[] {0x7f010070, 0x7f010071};
        com.xtremelabs.robolectric.lib3.R.styleable.Image = new int[] {0x7f010070, 0x7f010071};

        com.xtremelabs.robolectric.lib1.R.attr.offsetX = 0x7f010070;
        com.xtremelabs.robolectric.lib2.R.attr.offsetX = 0x7f010070;
        com.xtremelabs.robolectric.lib3.R.attr.offsetX = 0x7f010070;

        com.xtremelabs.robolectric.lib1.R.attr.offsetY = 0x7f010071;
        com.xtremelabs.robolectric.lib2.R.attr.offsetY = 0x7f010071;
        com.xtremelabs.robolectric.lib3.R.attr.offsetY = 0x7f010071;
    }

    private List<Integer> asIntList(int[] ints) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int anInt : ints) {
            list.add(anInt);
        }
        return list;
    }
}
