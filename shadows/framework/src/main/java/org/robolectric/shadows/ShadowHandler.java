package org.robolectric.shadows;

import android.os.Handler;
import org.robolectric.annotation.Implements;

@Deprecated
// Even though it doesn't implement anything, some parts of the system will fail if we don't have the
// @Implements tag (ShadowWrangler).
@Implements(Handler.class)
public class ShadowHandler {

}
