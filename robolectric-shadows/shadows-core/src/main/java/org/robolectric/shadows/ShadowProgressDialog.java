package org.robolectric.shadows;

import android.app.ProgressDialog;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.app.ProgressDialog}.
 */
@Implements(ProgressDialog.class)
public class ShadowProgressDialog extends ShadowAlertDialog {
}
