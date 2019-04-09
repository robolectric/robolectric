package org.robolectric.shadows;

import android.content.AsyncTaskLoader;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;

/**
 * The shadow {@link AsyncTaskLoader} for {@link LooperMode.Mode.PAUSED}.
 *
 * In {@link LooperMode.Mode.PAUSED} mode, Robolectric just uses the real AsyncTaskLoader for now.
 */
@Implements(
    value = AsyncTaskLoader.class,
    shadowPicker = ShadowAsyncTaskLoader.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowPausedAsyncTaskLoader<D> extends ShadowAsyncTaskLoader {}
