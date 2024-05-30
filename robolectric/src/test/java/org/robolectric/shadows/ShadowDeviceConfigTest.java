/*
 * Copyright (C) 2018 The Android Open Source Project
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

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.provider.DeviceConfig;
import android.provider.DeviceConfig.Properties;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests that ensure appropriate settings are backed up. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class ShadowDeviceConfigTest {
  // 2 sec
  private static final String DEFAULT_VALUE = "test_defaultValue";
  private static final String NAMESPACE = "namespace1";
  private static final String KEY = "key1";
  private static final String KEY2 = "key2";
  private static final String KEY3 = "key3";
  private static final String VALUE = "value1";
  private static final String VALUE2 = "value2";
  private static final String VALUE3 = "value3";

  @Test
  public void getProperty_empty() {
    String result = DeviceConfig.getProperty(NAMESPACE, KEY);
    assertThat(result).isNull();
  }

  @Test
  public void getProperty_nullNamespace() {
    assertThrows(
        NullPointerException.class,
        () -> {
          DeviceConfig.getProperty(null, KEY);
        });
  }

  @Test
  public void getProperty_nullName() {
    assertThrows(
        NullPointerException.class,
        () -> {
          DeviceConfig.getProperty(NAMESPACE, null);
        });
  }

  @Test
  public void getString_empty() {
    final String defaultValue = "defaultValue";
    final String result = DeviceConfig.getString(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void getString_nullDefault() {
    final String result = DeviceConfig.getString(NAMESPACE, KEY, null);
    assertThat(result).isNull();
  }

  @Test
  public void getString_nonEmpty() {
    final String value = "new_value";
    final String defaultValue = "default";
    DeviceConfig.setProperty(NAMESPACE, KEY, value, false);

    final String result = DeviceConfig.getString(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(value);
  }

  @Test
  public void getString_nullNamespace() {
    try {
      DeviceConfig.getString(null, KEY, "defaultValue");
      Assert.fail("Null namespace should have resulted in an NPE.");
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void getString_nullName() {
    assertThrows(
        NullPointerException.class,
        () -> {
          DeviceConfig.getString(NAMESPACE, null, "defaultValue");
        });
  }

  @Test
  public void getBoolean_empty() {
    final boolean defaultValue = true;
    final boolean result = DeviceConfig.getBoolean(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void getBoolean_valid() {
    final boolean value = true;
    final boolean defaultValue = false;
    DeviceConfig.setProperty(NAMESPACE, KEY, String.valueOf(value), false);

    final boolean result = DeviceConfig.getBoolean(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(value);
  }

  @Test
  public void getBoolean_invalid() {
    final boolean defaultValue = true;
    DeviceConfig.setProperty(NAMESPACE, KEY, "not_a_boolean", false);

    final boolean result = DeviceConfig.getBoolean(NAMESPACE, KEY, defaultValue);
    // Anything non-null other than case insensitive "true" parses to false.
    assertThat(result).isFalse();
  }

  @Test
  public void getBoolean_nullNamespace() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.getBoolean(null, KEY, false));
  }

  @Test
  public void getBoolean_nullName() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.getBoolean(NAMESPACE, null, false));
  }

  @Test
  public void getInt_empty() {
    final int defaultValue = 999;
    final int result = DeviceConfig.getInt(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void getInt_valid() {
    final int value = 123;
    final int defaultValue = 999;
    DeviceConfig.setProperty(NAMESPACE, KEY, String.valueOf(value), false);

    final int result = DeviceConfig.getInt(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(value);
  }

  @Test
  public void getInt_invalid() {
    final int defaultValue = 999;
    DeviceConfig.setProperty(NAMESPACE, KEY, "not_an_int", false);

    final int result = DeviceConfig.getInt(NAMESPACE, KEY, defaultValue);
    // Failure to parse results in using the default value
    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void getInt_nullNamespace() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.getInt(null, KEY, 0));
  }

  @Test
  public void getInt_nullName() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.getInt(NAMESPACE, null, 0));
  }

  @Test
  public void getLong_empty() {
    final long defaultValue = 123456;
    final long result = DeviceConfig.getLong(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void getLong_valid() {
    final long value = 456789;
    final long defaultValue = 123456;
    DeviceConfig.setProperty(NAMESPACE, KEY, String.valueOf(value), false);

    final long result = DeviceConfig.getLong(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(value);
  }

  @Test
  public void getLong_invalid() {
    final long defaultValue = 123456;
    DeviceConfig.setProperty(NAMESPACE, KEY, "not_a_long", false);

    final long result = DeviceConfig.getLong(NAMESPACE, KEY, defaultValue);
    // Failure to parse results in using the default value
    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void getLong_nullNamespace() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.getLong(null, KEY, 0));
  }

  @Test
  public void getLong_nullName() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.getLong(NAMESPACE, null, 0));
  }

  @Test
  public void getFloat_empty() {
    final float defaultValue = 123.456f;
    final float result = DeviceConfig.getFloat(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void getFloat_valid() {
    final float value = 456.789f;
    final float defaultValue = 123.456f;
    DeviceConfig.setProperty(NAMESPACE, KEY, String.valueOf(value), false);

    final float result = DeviceConfig.getFloat(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(value);
  }

  @Test
  public void getFloat_invalid() {
    final float defaultValue = 123.456f;
    DeviceConfig.setProperty(NAMESPACE, KEY, "not_a_float", false);

    final float result = DeviceConfig.getFloat(NAMESPACE, KEY, defaultValue);
    // Failure to parse results in using the default value
    assertThat(result).isEqualTo(defaultValue);
  }

  @Test
  public void getFloat_nullNamespace() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.getFloat(null, KEY, 0));
  }

  @Test
  public void getFloat_nullName() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.getFloat(NAMESPACE, null, 0));
  }

  @Test
  public void setProperty_nullNamespace() {
    assertThrows(
        NullPointerException.class, () -> DeviceConfig.setProperty(null, KEY, VALUE, false));
  }

  @Test
  public void setProperty_nullName() {
    assertThrows(
        NullPointerException.class, () -> DeviceConfig.setProperty(NAMESPACE, null, VALUE, false));
  }

  @Test
  public void setAndGetProperty_sameNamespace() {
    DeviceConfig.setProperty(NAMESPACE, KEY, VALUE, false);
    String result = DeviceConfig.getProperty(NAMESPACE, KEY);
    assertThat(result).isEqualTo(VALUE);
  }

  @Test
  public void setAndGetProperty_differentNamespace() {
    String newNamespace = "namespace2";
    DeviceConfig.setProperty(NAMESPACE, KEY, VALUE, false);
    String result = DeviceConfig.getProperty(newNamespace, KEY);
    assertThat(result).isNull();
  }

  @Test
  public void setAndGetProperty_multipleNamespaces() {
    String newNamespace = "namespace2";
    String newValue = "value2";
    DeviceConfig.setProperty(NAMESPACE, KEY, VALUE, false);
    DeviceConfig.setProperty(newNamespace, KEY, newValue, false);
    String result = DeviceConfig.getProperty(NAMESPACE, KEY);
    assertThat(result).isEqualTo(VALUE);
    result = DeviceConfig.getProperty(newNamespace, KEY);
    assertThat(result).isEqualTo(newValue);
  }

  @Test
  public void setAndGetProperty_overrideValue() {
    String newValue = "value2";
    DeviceConfig.setProperty(NAMESPACE, KEY, VALUE, false);
    DeviceConfig.setProperty(NAMESPACE, KEY, newValue, false);
    String result = DeviceConfig.getProperty(NAMESPACE, KEY);
    assertThat(result).isEqualTo(newValue);
  }

  @Config(minSdk = R)
  @Test
  public void getProperties_fullNamespace() {
    Properties properties = DeviceConfig.getProperties(NAMESPACE);
    assertThat(properties.getKeyset()).isEmpty();

    DeviceConfig.setProperty(NAMESPACE, KEY, VALUE, false);
    DeviceConfig.setProperty(NAMESPACE, KEY2, VALUE2, false);
    properties = DeviceConfig.getProperties(NAMESPACE);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(VALUE);
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(VALUE2);

    DeviceConfig.setProperty(NAMESPACE, KEY, VALUE3, false);
    properties = DeviceConfig.getProperties(NAMESPACE);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(VALUE3);
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(VALUE2);

    DeviceConfig.setProperty(NAMESPACE, KEY3, VALUE, false);
    properties = DeviceConfig.getProperties(NAMESPACE);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2, KEY3);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(VALUE3);
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(VALUE2);
    assertThat(properties.getString(KEY3, DEFAULT_VALUE)).isEqualTo(VALUE);
  }

  @Config(minSdk = R)
  @Test
  public void getProperties_getString() {
    DeviceConfig.setProperty(NAMESPACE, KEY, VALUE, false);
    DeviceConfig.setProperty(NAMESPACE, KEY2, VALUE2, false);

    Properties properties = DeviceConfig.getProperties(NAMESPACE, KEY, KEY2);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(VALUE);
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(VALUE2);
  }

  @Config(minSdk = R)
  @Test
  public void getProperties_getBoolean() {
    DeviceConfig.setProperty(NAMESPACE, KEY, "true", false);
    DeviceConfig.setProperty(NAMESPACE, KEY2, "false", false);
    DeviceConfig.setProperty(NAMESPACE, KEY3, "not a valid boolean", false);

    Properties properties = DeviceConfig.getProperties(NAMESPACE, KEY, KEY2, KEY3);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2, KEY3);
    assertThat(properties.getBoolean(KEY, true)).isTrue();
    assertThat(properties.getBoolean(KEY, false)).isTrue();
    assertThat(properties.getBoolean(KEY2, true)).isFalse();
    assertThat(properties.getBoolean(KEY2, false)).isFalse();
    // KEY3 was set to garbage, anything nonnull but "true" will parse as false
    assertThat(properties.getBoolean(KEY3, true)).isFalse();
    assertThat(properties.getBoolean(KEY3, false)).isFalse();
    // If a key was not set, it will return the default value
    assertThat(properties.getBoolean("missing_key", true)).isTrue();
    assertThat(properties.getBoolean("missing_key", false)).isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void getProperties_getInt() {
    final int value = 101;

    DeviceConfig.setProperty(NAMESPACE, KEY, Integer.toString(value), false);
    DeviceConfig.setProperty(NAMESPACE, KEY2, "not a valid int", false);

    Properties properties = DeviceConfig.getProperties(NAMESPACE, KEY, KEY2);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2);
    assertThat(properties.getInt(KEY, -1)).isEqualTo(value);
    // KEY2 was set to garbage, the default value is returned if an int cannot be parsed
    assertThat(properties.getInt(KEY2, -1)).isEqualTo(-1);
  }

  @Config(minSdk = R)
  @Test
  public void getProperties_getFloat() {
    final float value = 101.010f;

    DeviceConfig.setProperty(NAMESPACE, KEY, Float.toString(value), false);
    DeviceConfig.setProperty(NAMESPACE, KEY2, "not a valid float", false);

    Properties properties = DeviceConfig.getProperties(NAMESPACE, KEY, KEY2);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2);
    assertThat(properties.getFloat(KEY, -1.0f)).isEqualTo(value);
    // KEY2 was set to garbage, the default value is returned if a float cannot be parsed
    assertThat(properties.getFloat(KEY2, -1.0f)).isEqualTo(-1.0f);
  }

  @Config(minSdk = R)
  @Test
  public void getProperties_getLong() {
    final long value = 101;

    DeviceConfig.setProperty(NAMESPACE, KEY, Long.toString(value), false);
    DeviceConfig.setProperty(NAMESPACE, KEY2, "not a valid long", false);

    Properties properties = DeviceConfig.getProperties(NAMESPACE, KEY, KEY2);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2);
    assertThat(properties.getLong(KEY, -1)).isEqualTo(value);
    // KEY2 was set to garbage, the default value is returned if a long cannot be parsed
    assertThat(properties.getLong(KEY2, -1)).isEqualTo(-1);
  }

  @Config(minSdk = R)
  @Test
  public void getProperties_defaults() {
    DeviceConfig.setProperty(NAMESPACE, KEY, VALUE, false);
    DeviceConfig.setProperty(NAMESPACE, KEY3, VALUE3, false);

    Properties properties = DeviceConfig.getProperties(NAMESPACE, KEY, KEY2);
    assertThat(properties.getKeyset()).containsExactly(KEY);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(VALUE);
    // not set in DeviceConfig, but requested in getProperties
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(DEFAULT_VALUE);
    // set in DeviceConfig, but not requested in getProperties
    assertThat(properties.getString(KEY3, DEFAULT_VALUE)).isEqualTo(DEFAULT_VALUE);
  }

  @Config(minSdk = R)
  @Test
  public void setProperties() throws Exception {
    Properties properties =
        new Properties.Builder(NAMESPACE).setString(KEY, VALUE).setString(KEY2, VALUE2).build();

    DeviceConfig.setProperties(properties);
    properties = DeviceConfig.getProperties(NAMESPACE);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(VALUE);
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(VALUE2);

    properties =
        new Properties.Builder(NAMESPACE).setString(KEY, VALUE2).setString(KEY3, VALUE3).build();

    DeviceConfig.setProperties(properties);
    properties = DeviceConfig.getProperties(NAMESPACE);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY3);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(VALUE2);
    assertThat(properties.getString(KEY3, DEFAULT_VALUE)).isEqualTo(VALUE3);

    assertThat(properties.getKeyset()).doesNotContain(KEY2);
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(DEFAULT_VALUE);
  }

  @Config(minSdk = R)
  @Test
  public void setProperties_multipleNamespaces() throws Exception {
    final String namespace2 = "namespace2";
    Properties properties1 =
        new Properties.Builder(NAMESPACE).setString(KEY, VALUE).setString(KEY2, VALUE2).build();
    Properties properties2 =
        new Properties.Builder(namespace2).setString(KEY2, VALUE).setString(KEY3, VALUE2).build();

    assertThat(DeviceConfig.setProperties(properties1)).isTrue();
    assertThat(DeviceConfig.setProperties(properties2)).isTrue();

    Properties properties = DeviceConfig.getProperties(NAMESPACE);
    assertThat(properties.getKeyset()).containsExactly(KEY, KEY2);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(VALUE);
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(VALUE2);

    assertThat(properties.getKeyset()).doesNotContain(KEY3);
    assertThat(properties.getString(KEY3, DEFAULT_VALUE)).isEqualTo(DEFAULT_VALUE);

    properties = DeviceConfig.getProperties(namespace2);
    assertThat(properties.getKeyset()).containsExactly(KEY2, KEY3);
    assertThat(properties.getString(KEY2, DEFAULT_VALUE)).isEqualTo(VALUE);
    assertThat(properties.getString(KEY3, DEFAULT_VALUE)).isEqualTo(VALUE2);

    assertThat(properties.getKeyset()).doesNotContain(KEY);
    assertThat(properties.getString(KEY, DEFAULT_VALUE)).isEqualTo(DEFAULT_VALUE);
  }

  @Config(minSdk = R)
  @Test
  public void propertiesBuilder() {
    boolean booleanValue = true;
    int intValue = 123;
    float floatValue = 4.56f;
    long longValue = -789L;
    String key4 = "key4";
    String key5 = "key5";

    Properties properties =
        new Properties.Builder(NAMESPACE)
            .setString(KEY, VALUE)
            .setBoolean(KEY2, booleanValue)
            .setInt(KEY3, intValue)
            .setLong(key4, longValue)
            .setFloat(key5, floatValue)
            .build();
    assertThat(properties.getNamespace()).isEqualTo(NAMESPACE);
    assertThat(properties.getString(KEY, "defaultValue")).isEqualTo(VALUE);
    assertThat(properties.getBoolean(KEY2, false)).isEqualTo(booleanValue);
    assertThat(properties.getInt(KEY3, 0)).isEqualTo(intValue);
    assertThat(properties.getLong("key4", 0L)).isEqualTo(longValue);
    assertThat(properties.getFloat("key5", 0f)).isEqualTo(floatValue);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void deleteProperty_nullNamespace() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.deleteProperty(null, KEY));
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void deleteProperty_nullName() {
    assertThrows(NullPointerException.class, () -> DeviceConfig.deleteProperty(NAMESPACE, null));
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void deletePropertyString() {
    final String value = "new_value";
    final String defaultValue = "default";
    DeviceConfig.setProperty(NAMESPACE, KEY, value, false);
    DeviceConfig.deleteProperty(NAMESPACE, KEY);
    final String result = DeviceConfig.getString(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void deletePropertyBoolean() {
    final boolean value = true;
    final boolean defaultValue = false;
    DeviceConfig.setProperty(NAMESPACE, KEY, String.valueOf(value), false);
    DeviceConfig.deleteProperty(NAMESPACE, KEY);
    final boolean result = DeviceConfig.getBoolean(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void deletePropertyInt() {
    final int value = 123;
    final int defaultValue = 999;
    DeviceConfig.setProperty(NAMESPACE, KEY, String.valueOf(value), false);
    DeviceConfig.deleteProperty(NAMESPACE, KEY);
    final int result = DeviceConfig.getInt(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void deletePropertyLong() {
    final long value = 456789;
    final long defaultValue = 123456;
    DeviceConfig.setProperty(NAMESPACE, KEY, String.valueOf(value), false);
    DeviceConfig.deleteProperty(NAMESPACE, KEY);
    final long result = DeviceConfig.getLong(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void deletePropertyFloat() {
    final float value = 456.789f;
    final float defaultValue = 123.456f;
    DeviceConfig.setProperty(NAMESPACE, KEY, String.valueOf(value), false);
    DeviceConfig.deleteProperty(NAMESPACE, KEY);
    final float result = DeviceConfig.getFloat(NAMESPACE, KEY, defaultValue);
    assertThat(result).isEqualTo(defaultValue);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void deleteProperty_empty() {
    assertThat(DeviceConfig.deleteProperty(NAMESPACE, KEY)).isTrue();
    final String result = DeviceConfig.getString(NAMESPACE, KEY, null);
    assertThat(result).isNull();
  }
}
