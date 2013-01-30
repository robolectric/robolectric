package com.xtremelabs.robolectric.util;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class SQLiteTestHelper {

    static void verifyColumnValues(List<Object> colValues) {
        assertThat(colValues.get(0), instanceOf(Float.class));
        assertThat(colValues.get(1), instanceOf(byte[].class));
        assertThat(colValues.get(2), instanceOf(String.class));
        assertThat(colValues.get(3), instanceOf(Integer.class));
    }

}
