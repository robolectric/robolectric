package org.robolectric.shadows;


import android.app.ProgressDialog;
import org.robolectric.annotation.Implements;

@Implements(ProgressDialog.class)
public class ShadowProgressDialog extends ShadowAlertDialog {

}
