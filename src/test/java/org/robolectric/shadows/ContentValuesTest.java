package org.robolectric.shadows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public final class ContentValuesTest {

    private static final String KEY = "key";

    private ShadowContentValues contentValues;

    @Before
    public void setUp() {
        contentValues = new ShadowContentValues();
    }

    @Test
    public void getAsBoolean_zero() {
        contentValues.put(KEY, 0);
        assertThat(contentValues.getAsBoolean(KEY)).isFalse();
    }

    @Test
    public void getAsBoolean_one() {
        contentValues.put(KEY, 1);
        assertThat(contentValues.getAsBoolean(KEY)).isTrue();
    }

    @Test
    public void getAsBoolean_false() {
        contentValues.put(KEY, false);
        assertThat(contentValues.getAsBoolean(KEY)).isFalse();
    }

    @Test
    public void getAsBoolean_true() {
        contentValues.put(KEY, true);
        assertThat(contentValues.getAsBoolean(KEY)).isTrue();
    }

    @Test
    public void getAsBoolean_falseString() {
        contentValues.put(KEY, "false");
        assertThat(contentValues.getAsBoolean(KEY)).isFalse();
    }

    @Test
    public void getAsBoolean_trueString() {
        contentValues.put(KEY, "true");
        assertThat(contentValues.getAsBoolean(KEY)).isTrue();
    }
}
