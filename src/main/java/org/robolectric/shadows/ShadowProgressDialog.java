package org.robolectric.shadows;


import android.app.ProgressDialog;
import org.robolectric.internal.Implements;

@Implements(ProgressDialog.class)
public class ShadowProgressDialog extends ShadowAlertDialog {
}
