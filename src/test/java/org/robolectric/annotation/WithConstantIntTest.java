package org.robolectric.annotation;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class WithConstantIntTest {

    private static final int NEW_VALUE = 9;

    @Test
    @WithConstantInt(classWithField = android.os.Build.VERSION.class, fieldName = "SDK_INT", newValue = NEW_VALUE)
    public void testWithConstantInt() {
        assertThat(Build.VERSION.SDK_INT).isEqualTo(NEW_VALUE);
    }

    @Test
    public void testWithoutConstantInt() {
        assertThat(Build.VERSION.SDK_INT).isEqualTo(8); // todo pull this from some other config spot? see shadow system properties
    }

}
