package org.robolectric.shadows;

import android.view.WindowManager;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link WindowManager}.
 *
 * <p>This shadow is deprecated, and only exists for simplified shadowOf generation.
 *
 * @deprecated Use {@link ShadowWindowManagerImpl} instead. Use <code>
 * ((ShadowWindowManagerImpl) Shadow.extract(WindowManager))</code> instead.
 */
@Deprecated
@Implements(WindowManager.class)
public class ShadowWindowManager {}
