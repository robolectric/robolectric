/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.os;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * CTS tests for {@link PersistableBundle}.
 *
 * <p>Copied from <a
 * href="https://cs.android.com/android/platform/superproject/main/+/main:cts/tests/tests/os/src/android/os/cts/PersistableBundleTest.java">PersistableBundleTest</a>
 */
@RunWith(AndroidJUnit4.class)
public class PersistableBundleTest {
  private static final double DELTA_DOUBLE = 0.0d;

  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.R)
  public void testWriteToStreamAndReadFromStream() throws IOException {
    PersistableBundle bundle = new PersistableBundle();
    bundle.putBoolean("boolean", true);
    bundle.putBooleanArray("boolean_array", new boolean[] {false});
    bundle.putDouble("double", 1.23);
    bundle.putDoubleArray("double_array", new double[] {2.34, 3.45});
    bundle.putInt("int", 1);
    bundle.putIntArray("int_array", new int[] {2});
    bundle.putLong("long", 12345L);
    bundle.putLongArray("long_array", new long[] {1234567L, 2345678L});
    bundle.putString("string", "abc123");
    bundle.putStringArray("string_array", new String[] {"xyz789"});
    PersistableBundle nestedBundle = new PersistableBundle();
    nestedBundle.putBooleanArray("boolean_array", new boolean[] {});
    nestedBundle.putInt("int", 9);
    nestedBundle.putLongArray("long_array", new long[] {654321L});
    bundle.putPersistableBundle("bundle", nestedBundle);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    bundle.writeToStream(outputStream);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    PersistableBundle restoredBundle = PersistableBundle.readFromStream(inputStream);

    assertEquals(bundle.size(), restoredBundle.size());
    assertEquals(true, restoredBundle.getBoolean("boolean"));
    assertTrue(
        Arrays.equals(new boolean[] {false}, restoredBundle.getBooleanArray("boolean_array")));
    assertEquals(1.23, restoredBundle.getDouble("double"), DELTA_DOUBLE);
    assertTrue(
        Arrays.equals(new double[] {2.34, 3.45}, restoredBundle.getDoubleArray("double_array")));
    assertEquals(1, restoredBundle.getInt("int"));
    assertTrue(Arrays.equals(new int[] {2}, restoredBundle.getIntArray("int_array")));
    assertEquals(12345L, restoredBundle.getLong("long"));
    assertTrue(
        Arrays.equals(new long[] {1234567L, 2345678L}, restoredBundle.getLongArray("long_array")));
    assertEquals("abc123", restoredBundle.getString("string"));
    assertTrue(
        Arrays.equals(new String[] {"xyz789"}, restoredBundle.getStringArray("string_array")));
    PersistableBundle restoredNestedBundle = restoredBundle.getPersistableBundle("bundle");
    assertEquals(nestedBundle.size(), restoredNestedBundle.size());
    assertTrue(
        Arrays.equals(new boolean[] {}, restoredNestedBundle.getBooleanArray("boolean_array")));
    assertEquals(9, restoredNestedBundle.getInt("int"));
    assertTrue(
        Arrays.equals(new long[] {654321L}, restoredNestedBundle.getLongArray("long_array")));
  }
}
