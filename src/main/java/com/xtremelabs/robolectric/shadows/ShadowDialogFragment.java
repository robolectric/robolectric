package com.xtremelabs.robolectric.shadows;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow implementation of the {@link DialogFragment}.
 */
@Implements(DialogFragment.class)
public class ShadowDialogFragment extends ShadowFragment {

    private boolean isShowing;

    @Implementation
    public void show(FragmentManager manager, String tag) {
        isShowing = true;
    }

    @Implementation
    public void dismiss() {
        isShowing = false;
    }

    @Implementation
    public Dialog getDialog() {
        Dialog dialog = newInstanceOf(Dialog.class);
        ShadowDialog shadowDialog = shadowOf(dialog);
        if (isShowing) {
            shadowDialog.show();
        }
        return dialog;
    }

}
