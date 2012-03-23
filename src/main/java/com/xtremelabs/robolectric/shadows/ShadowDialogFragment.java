package com.xtremelabs.robolectric.shadows;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.tester.android.util.TestFragmentManager;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow implementation of the {@link DialogFragment}.
 */
@Implements(DialogFragment.class)
public class ShadowDialogFragment extends ShadowFragment {
    private Dialog dialog;

    @RealObject
    private DialogFragment realDialogFragment;

    @Implementation
    public void show(FragmentManager manager, String tag) {
        TestFragmentManager testFragmentManager = (TestFragmentManager) manager;
        FragmentActivity activityFromManager = testFragmentManager.getActivity();

        shadowOf(realDialogFragment).setActivity(activityFromManager);

        realDialogFragment.onAttach(activity);
        realDialogFragment.onCreate(null);
        dialog = realDialogFragment.onCreateDialog(null);
        view = realDialogFragment.onCreateView(ShadowLayoutInflater.from(activity), null, null);
        if (dialog == null) {
            dialog = new Dialog(activityFromManager);
            dialog.setContentView(view);
        }
        testFragmentManager.addDialogFragment(tag, realDialogFragment);
        realDialogFragment.onViewCreated(view, null);
        realDialogFragment.onActivityCreated(null);
        realDialogFragment.onStart();
        realDialogFragment.onResume();
    }

    @Implementation
    public void onStart() {
        if (dialog != null) {
            dialog.show();
        }
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
}
