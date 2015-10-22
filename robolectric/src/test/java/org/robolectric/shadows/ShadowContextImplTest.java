package org.robolectric.shadows;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowContextImplTest {
    private final Context context = RuntimeEnvironment.application;

    @Test
    @Config(sdk = {
        Build.VERSION_CODES.JELLY_BEAN_MR2,
        Build.VERSION_CODES.KITKAT,
        Build.VERSION_CODES.LOLLIPOP,
        Build.VERSION_CODES.LOLLIPOP_MR1
    })
    public void getSystemService_shouldReturnBluetoothAdapter() {
        assertThat(context.getSystemService(Context.BLUETOOTH_SERVICE)).isInstanceOf(BluetoothManager.class);
    }
}

