package com.xtremelabs.robolectric.shadows;

import android.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow for a FragmentActivity.  Note that this is for the support package v4 version of "FragmentActivity", not the
 * android.app one.
 */
@Implements(FragmentActivity.class)
public class ShadowFragmentActivity extends ShadowActivity {

    FragmentManager fragmentManager = new FragmentManagerImpl();
    private boolean destroyed;

    @Implementation
    public FragmentManager getSupportFragmentManager() {
        return fragmentManager;
    }

    @Override @Implementation
    public void onDestroy() {
        super.onDestroy();
        destroyed = true;
    }

    private class FragmentManagerImpl extends FragmentManager {
        Map<String, Fragment> tagLookup = new HashMap<String, Fragment>();
        Map<Integer, Fragment> intLookup = new HashMap<Integer, Fragment>();

        Stack<BackStackEntry> backStack = new Stack<BackStackEntry>();

        @Override
        public FragmentTransaction beginTransaction() {
            return new FragmentTransactionImpl();
        }

        @Override
        public boolean executePendingTransactions() {
            return false;
        }

        @Override
        public Fragment findFragmentById(int i) {
            return intLookup.get(i);
        }

        @Override
        public Fragment findFragmentByTag(String s) {
            return tagLookup.get(s);
        }

        @Override
        public void popBackStack() {
            popBackStackImmediate();
        }

        @Override
        public boolean popBackStackImmediate() {
            if (backStack.size() == 0) return false;
            backStack.pop();
            return true;
        }

        @Override
        public void popBackStack(String s, int i) {
            popBackStackImmediate(s, i);
        }

        @Override
        public boolean popBackStackImmediate(String s, int flags) {
            for (int i = backStack.size() - 1; i >= 0; i--) {
                BackStackEntry entry = backStack.get(i);
                if (s.equals(entry.getName())) {
                    int j = backStack.size() - 1;
                    while (j-- > i) backStack.pop();
                    if (flags == POP_BACK_STACK_INCLUSIVE) backStack.pop();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void popBackStack(int i, int i1) {
        }

        @Override
        public boolean popBackStackImmediate(int i, int i1) {
            return false;
        }

        @Override
        public int getBackStackEntryCount() {
            return backStack.size();
        }

        @Override
        public BackStackEntry getBackStackEntryAt(int i) {
            return backStack.get(i);
        }

        @Override
        public void addOnBackStackChangedListener(OnBackStackChangedListener onBackStackChangedListener) {
        }

        @Override
        public void removeOnBackStackChangedListener(OnBackStackChangedListener onBackStackChangedListener) {
        }

        @Override
        public void putFragment(Bundle bundle, String s, Fragment fragment) {
        }

        @Override
        public Fragment getFragment(Bundle bundle, String s) {
            return null;
        }

        @Override
        public Fragment.SavedState saveFragmentInstanceState(Fragment fragment) {
            return null;
        }

        @Override
        public void dump(String s, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strings) {
        }

        private class FragmentTransactionImpl extends FragmentTransaction {
            private BackStackRecord backStackEntry;

            @Override
            public FragmentTransaction add(Fragment fragment, String s) {
                return add(0, fragment, s);
            }

            @Override
            public FragmentTransaction add(int containerViewId, Fragment fragment) {
                return add(containerViewId, fragment, null);
            }

            @Override
            public FragmentTransaction add(int containerViewId, Fragment fragment, String tag) {
                ShadowFragment shadowFragment = shadowOf(fragment);
                if (containerViewId != 0) {
                    shadowFragment.setFragmentId(containerViewId);
                    intLookup.put(containerViewId, fragment);
                }
                if (tag != null) {
                    shadowFragment.setTag(tag);
                    tagLookup.put(tag, fragment);
                }
                FragmentActivity activity = (FragmentActivity) getRealActivity();
                shadowFragment.setActivity(activity);
                fragment.onAttach(activity);
                fragment.onCreate(null);
                shadowFragment.createView();
                if (containerViewId == R.id.content) setContentView(fragment.getView());
                fragment.onActivityCreated(null);
                shadowFragment.resume();
                return this;
            }

            @Override
            public FragmentTransaction replace(int i, Fragment fragment) {
                remove(fragment);
                add(i, fragment, null);
                return this;
            }

            @Override
            public FragmentTransaction replace(int i, Fragment fragment, String tag) {
                remove(fragment);
                add(i, fragment, tag);
                return this;
            }

            @Override
            public FragmentTransaction remove(Fragment fragment) {
                intLookup.remove(fragment.getId());
                tagLookup.remove(fragment.getTag());
                return this;
            }

            @Override
            public FragmentTransaction hide(Fragment fragment) {
                return this;
            }

            @Override
            public FragmentTransaction show(Fragment fragment) {
                return this;
            }

            @Override
            public FragmentTransaction detach(Fragment fragment) {
                return this;
            }

            @Override
            public FragmentTransaction attach(Fragment fragment) {
                return this;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public FragmentTransaction setCustomAnimations(int i, int i1) {
                return this;
            }

            @Override
            public FragmentTransaction setCustomAnimations(int i, int i1, int i2, int i3) {
                return this;
            }

            @Override
            public FragmentTransaction setTransition(int i) {
                return this;
            }

            @Override
            public FragmentTransaction setTransitionStyle(int i) {
                return this;
            }

            @Override
            public FragmentTransaction addToBackStack(final String s) {
                backStackEntry = new BackStackRecord(s);

                return this;
            }

            @Override
            public boolean isAddToBackStackAllowed() {
                return true;
            }

            @Override
            public FragmentTransaction disallowAddToBackStack() {
                return this;
            }

            @Override
            public FragmentTransaction setBreadCrumbTitle(int i) {
                return this;
            }

            @Override
            public FragmentTransaction setBreadCrumbTitle(CharSequence charSequence) {
                return this;
            }

            @Override
            public FragmentTransaction setBreadCrumbShortTitle(int i) {
                return this;
            }

            @Override
            public FragmentTransaction setBreadCrumbShortTitle(CharSequence charSequence) {
                return this;
            }

            @Override
            public int commit() {
                if (destroyed) throw new IllegalStateException("Can't commit transaction on destroyed activity");
                if (backStackEntry == null) return -1;

                BackStackRecord entry = backStackEntry;
                backStack.push(entry);
                backStackEntry = null;
                return entry.getId();
            }

            @Override
            public int commitAllowingStateLoss() {
                return commit();
            }

            private class BackStackRecord implements BackStackEntry {
                private final String s;

                public BackStackRecord(String s) {
                    this.s = s;
                }

                @Override
                public int getId() {
                    if (backStack.contains(this)) return backStack.indexOf(this);
                    return -1;
                }

                @Override
                public String getName() {
                    return s;
                }

                @Override
                public int getBreadCrumbTitleRes() {
                    return -1;
                }

                @Override
                public int getBreadCrumbShortTitleRes() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public CharSequence getBreadCrumbTitle() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public CharSequence getBreadCrumbShortTitle() {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }
}
