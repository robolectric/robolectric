/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2011 Eric Bowman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xtremelabs.robolectric.shadows;

import android.util.SparseArray;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for shadow implementation of SparseArray. Basically copied & pasted the
 * real SparseArray tests from the 2.2 SDK.
 *
 * @author Eric Bowman (ebowman@boboco.ie)
 * @since 2011-02-25 11:01
 */
@RunWith(WithTestDefaultsRunner.class)
public class SparseArrayTest {
    private static final int[] KEYS = {12, 23, 4, 6, 8, 1, 3, -12, 0, -3, 11, 14, -23};
    private static final Integer[] VALUES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private static final int LENGTH = VALUES.length;
    private static final int NON_EXISTED_KEY = 123;
    private static final Integer VALUE_FOR_NON_EXISTED_KEY = -1;

    @Test
    public void testSparseArrayWithDefaultCapacity() {
        ShadowWrangler.getInstance().logMissingInvokedShadowMethods();
        SparseArray<Integer> sparseArray = new SparseArray<Integer>();
        assertEquals(0, sparseArray.size());

        int length = VALUES.length;

        for (int i = 0; i < length; i++) {
            sparseArray.put(KEYS[i], VALUES[i]);
            assertEquals(i + 1, sparseArray.size());
        }

        for (int i = 0; i < length; i++) {
            assertEquals(new Integer(i), sparseArray.get(KEYS[i]));
        }

        for (int i = 0; i < length; i++) {
            assertEquals(sparseArray.indexOfValue(VALUES[i]), sparseArray.indexOfKey(KEYS[i]));
        }

        // for key already exist, old value will be replaced
        int existKey = KEYS[0];
        Integer oldValue = VALUES[0]; // 0
        Integer newValue = 100;
        assertEquals(oldValue, sparseArray.get(existKey));
        assertEquals(LENGTH, sparseArray.size());
        sparseArray.put(existKey, newValue);
        assertEquals(newValue, sparseArray.get(existKey));
        assertEquals(LENGTH, sparseArray.size());

        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                sparseArray.get(NON_EXISTED_KEY, VALUE_FOR_NON_EXISTED_KEY));
        assertNull(sparseArray.get(NON_EXISTED_KEY)); // the default value is null

        int size = sparseArray.size();
        sparseArray.append(NON_EXISTED_KEY, VALUE_FOR_NON_EXISTED_KEY);
        assertEquals(size + 1, sparseArray.size());
        assertEquals(size, sparseArray.indexOfKey(NON_EXISTED_KEY));
        assertEquals(size, sparseArray.indexOfValue(VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(NON_EXISTED_KEY, sparseArray.keyAt(size));
        assertEquals(VALUE_FOR_NON_EXISTED_KEY, sparseArray.valueAt(size));

        sparseArray.setValueAt(size, VALUES[1]);
        assertTrue(VALUE_FOR_NON_EXISTED_KEY != sparseArray.valueAt(size));
        assertEquals(VALUES[1], sparseArray.valueAt(size));

        size = sparseArray.size();
        assertEquals(VALUES[1], sparseArray.get(KEYS[1]));
        assertFalse(VALUE_FOR_NON_EXISTED_KEY == VALUES[1]);
        sparseArray.delete(KEYS[1]);
        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                sparseArray.get(KEYS[1], VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(size - 1, sparseArray.size());

        size = sparseArray.size();
        assertEquals(VALUES[2], sparseArray.get(KEYS[2]));
        assertFalse(VALUE_FOR_NON_EXISTED_KEY == VALUES[2]);
        sparseArray.remove(KEYS[2]);
        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                sparseArray.get(KEYS[2], VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(size - 1, sparseArray.size());

        sparseArray.clear();
        assertEquals(0, sparseArray.size());
    }

    @Test
    public void testSparseArrayWithSpecifiedCapacity() {
        SparseArray<Integer> sparseArray = new SparseArray<Integer>(5);
        assertEquals(0, sparseArray.size());

        int length = VALUES.length;

        for (int i = 0; i < length; i++) {
            sparseArray.put(KEYS[i], VALUES[i]);
            assertEquals(i + 1, sparseArray.size());
        }

        for (int i = 0; i < length; i++) {
            assertEquals(VALUES[i], sparseArray.get(KEYS[i]));
        }

        for (int i = 0; i < length; i++) {
            assertEquals(sparseArray.indexOfValue(VALUES[i]), sparseArray.indexOfKey(KEYS[i]));
        }

        // for key already exist, old value will be replaced
        int existKey = KEYS[0];
        Integer oldValue = VALUES[0]; // 0
        Integer newValue = 100;
        assertEquals(oldValue, sparseArray.get(existKey));
        assertEquals(LENGTH, sparseArray.size());
        sparseArray.put(existKey, newValue);
        assertEquals(newValue, sparseArray.get(existKey));
        assertEquals(LENGTH, sparseArray.size());

        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                     sparseArray.get(NON_EXISTED_KEY, VALUE_FOR_NON_EXISTED_KEY));
        assertNull(sparseArray.get(NON_EXISTED_KEY)); // the default value is null

        int size = sparseArray.size();
        sparseArray.append(NON_EXISTED_KEY, VALUE_FOR_NON_EXISTED_KEY);
        assertEquals(size + 1, sparseArray.size());
        assertEquals(size, sparseArray.indexOfKey(NON_EXISTED_KEY));
        assertEquals(size, sparseArray.indexOfValue(VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(NON_EXISTED_KEY, sparseArray.keyAt(size));
        assertEquals(VALUE_FOR_NON_EXISTED_KEY, sparseArray.valueAt(size));

        sparseArray.setValueAt(size, VALUES[1]);
        assertTrue(VALUE_FOR_NON_EXISTED_KEY != sparseArray.valueAt(size));
        assertEquals(VALUES[1], sparseArray.valueAt(size));

        size = sparseArray.size();
        assertEquals(VALUES[1], sparseArray.get(KEYS[1]));
        assertFalse(VALUE_FOR_NON_EXISTED_KEY == VALUES[1]);
        sparseArray.delete(KEYS[1]);
        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                sparseArray.get(KEYS[1], VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(size - 1, sparseArray.size());

        size = sparseArray.size();
        assertEquals(VALUES[2], sparseArray.get(KEYS[2]));
        assertFalse(VALUE_FOR_NON_EXISTED_KEY == VALUES[2]);
        sparseArray.remove(KEYS[2]);
        assertEquals(VALUE_FOR_NON_EXISTED_KEY,
                sparseArray.get(KEYS[2], VALUE_FOR_NON_EXISTED_KEY));
        assertEquals(size - 1, sparseArray.size());

        sparseArray.clear();
        assertEquals(0, sparseArray.size());
    }
}
