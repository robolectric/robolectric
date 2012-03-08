package com.xtremelabs.robolectric.shadows;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;

/**
 * Shadow implementation of the {@link DialogFragment}.
 */
@Implements(DialogFragment.class)
public class ShadowDialogFragment extends ShadowFragment {
    @RealObject
    DialogFragment realDialogFragment;

    private Dialog dialog;

    @Implementation
    public void show(FragmentManager manager, String tag) {
        if (dialog == null) {
            manager.beginTransaction().add(realFragment, tag).commit();
            dialog = ((DialogFragment) realFragment).onCreateDialog(null);
        }
        dialog.show();
    }

    @Implementation
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            realDialogFragment.getFragmentManager().beginTransaction().remove(realDialogFragment).commit();
        }
    }

    @Implementation
    public Dialog getDialog() {
        return dialog;
    }

    @Implementation
    public Dialog onCreateDialog(@SuppressWarnings("UnusedParameters") Bundle savedInstanceState) {
        return newInstanceOf(Dialog.class);
    }

}
