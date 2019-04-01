package org.robolectric.shadows.support.v4;


import android.support.v4.content.AsyncTaskLoader;
import org.robolectric.annotation.Implements;

@Implements(
    value = AsyncTaskLoader.class,
    shadowPicker = ShadowBaseAsyncTaskLoader.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowRealisticAsyncTaskLoader<D> extends ShadowBaseAsyncTaskLoader {}
