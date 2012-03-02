package com.xtremelabs.robolectric.shadows;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;

/**
 * Shadow implementation of the {@link DialogFragment}.
 */
@Implements(DialogFragment.class)
public class ShadowDialogFragment extends ShadowFragment {

    private Dialog dialog;

    @Implementation
    public void show(FragmentManager manager, String tag) {
        if (dialog == null) {
            dialog = ((DialogFragment) realFragment).onCreateDialog(null);
        }
        dialog.show();
    }

    @Implementation
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Implementation
    public Dialog getDialog() {
        return dialog;
    }

    @Implementation
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return newInstanceOf(Dialog.class);
    }

}
