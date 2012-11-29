package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowTypedArrayTest extends TestCase {

    @Test
    public void emptyArray_shouldReturnNull() throws Exception {
        ShadowTypedArray array = new ShadowTypedArray();
        assertNull(array.getString(0));
    }

    @Test
    public void nonExistantItem_shouldReturnNull() throws Exception {
        ShadowTypedArray array = new ShadowTypedArray();
        array.add("some item");
        assertNotNull(array.getString(0));
        assertNull(array.getString(1));
    }
}
