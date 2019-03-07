package org.robolectric.shadows;

import android.content.AsyncTaskLoader;
import android.os.AsyncTask;
import org.robolectric.annotation.Implements;

@Implements(
    value = AsyncTaskLoader.class,
    shadowPicker = ShadowBaseAsyncTaskLoader.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowRealisticAsyncTaskLoader<D>  extends ShadowBaseAsyncTaskLoader {

}
