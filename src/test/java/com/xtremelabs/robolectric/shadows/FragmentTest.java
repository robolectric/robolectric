package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.matchers.StartedMatcher;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(WithTestDefaultsRunner.class)
public class FragmentTest {

    @Test
    public void testOnCreateOnResumeOnCreateView() throws Exception {
        DummyFragment fragment = new DummyFragment();
        ContainerActivity activity = new ContainerActivity();
        startFragment(activity, fragment);

        assertNotNull(fragment.getActivity());
        assertNotNull(fragment.getView());
        TextView tacos = (TextView) fragment.getView().findViewById(R.id.tacos);
        assertNotNull(tacos);
        assertEquals("TACOS", tacos.getText());

        assertEquals(1, fragment.created);
        assertEquals(2, fragment.viewCreated);
        assertEquals(3, fragment.onViewCreated);
        assertEquals(4, fragment.activityCreated);
        assertEquals(5, fragment.resumed);

        assertTrue(fragment.isResumed());

        assertSame(fragment.getView(), shadowOf(activity).getContentView());
    }

    @Test
    public void testArguments() {
        DummyFragment fragment = new DummyFragment();

        final Bundle bundle = new Bundle();
        final int bundleVal = 15;
        bundle.putInt(DummyFragment.ARG_KEY, bundleVal);
        fragment.setArguments(bundle);

        startFragment(fragment);

        assertEquals(bundleVal, fragment.argument);
    }

    @Test
    public void testGetFragmentManager() {
        DummyFragment fragment = new DummyFragment();
        final ShadowFragment shadow = shadowOf(fragment);
        ContainerActivity activity = new ContainerActivity();
        shadow.setActivity(activity);
        assertNotNull(fragment.getFragmentManager());
        assertSame(activity.getSupportFragmentManager(), fragment.getFragmentManager());
    }

    @Test
    public void testFragmentManagerBeginWithNoTagOrId() {
        DummyFragment fragment = new DummyFragment();
        ContainerActivity activity = new ContainerActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.beginTransaction().add(fragment, null).commit();
        assertSame(activity, fragment.getActivity());
        assertSame(fragmentManager, fragment.getFragmentManager());

        assertEquals(0, fragment.getId());
        assertNull(fragment.getTag());

        assertNull(fragmentManager.findFragmentById(0));
        assertNull(fragmentManager.findFragmentByTag(null));

        // Just don't blow up
        fragmentManager.beginTransaction().remove(fragment).commit();
    }

    @Test
    public void testFragmentManagerBeginWithTagAndId() {
        DummyFragment fragment = new DummyFragment();
        ContainerActivity activity = new ContainerActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.beginTransaction().add(24, fragment, "fred").commit();

        assertEquals(24, fragment.getId());
        assertEquals("fred", fragment.getTag());

        Fragment byId = fragmentManager.findFragmentById(24);
        Fragment byTag = fragmentManager.findFragmentByTag("fred");

        assertSame(fragment, byId);
        assertSame(fragment, byTag);

        fragmentManager.beginTransaction().remove(fragment).commit();
        assertNull(fragmentManager.findFragmentById(24));
        assertNull(fragmentManager.findFragmentByTag("fred"));
    }

    @Test
    public void testFragmentManagerPopNothing() {
        DummyFragment fragment = new DummyFragment();
        ContainerActivity activity = new ContainerActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.beginTransaction().add(24, fragment, "fred").commit();

      assertEmptyBackStack(fragmentManager);
    }

    private void assertEmptyBackStack(FragmentManager fragmentManager) {
      assertEquals(0, fragmentManager.getBackStackEntryCount());
      assertFalse(fragmentManager.popBackStackImmediate());
      assertFalse(fragmentManager.popBackStackImmediate("fred", 0));
      assertFalse(fragmentManager.popBackStackImmediate("fred", FragmentManager.POP_BACK_STACK_INCLUSIVE));
    }

    @Test
    public void testFragmentManagerPopSimple() {
        ContainerActivity activity = new ContainerActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.beginTransaction().add(24, new DummyFragment(), "fred").addToBackStack("fred").commit();

        assertEquals(1, fragmentManager.getBackStackEntryCount());
        assertTrue(fragmentManager.popBackStackImmediate());

        assertEmptyBackStack(fragmentManager);
    }

    @Test
    public void testFragmentManagerPopByName() {
        ContainerActivity activity = new ContainerActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        assertEquals(0,
            fragmentManager.beginTransaction().add(24, new DummyFragment(), "fred").addToBackStack("fred").commit());
        assertEquals(1,
            fragmentManager.beginTransaction().add(24, new DummyFragment(), "barney").addToBackStack("barney").commit());
        assertEquals(2,
            fragmentManager.beginTransaction().add(24, new DummyFragment(), "wilma").addToBackStack("wilma").commit());

        assertEquals(3, fragmentManager.getBackStackEntryCount());
        assertTrue(fragmentManager.popBackStackImmediate("fred", 0));

        assertEquals(1, fragmentManager.getBackStackEntryCount());
    }

    @Test
    public void testFragmentManagerPopByNameInclusive() {
        ContainerActivity activity = new ContainerActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        assertEquals(0,
            fragmentManager.beginTransaction().add(24, new DummyFragment(), "fred").addToBackStack("fred").commit());
        assertEquals(1,
            fragmentManager.beginTransaction().add(24, new DummyFragment(), "barney").addToBackStack("barney").commit());
        assertEquals(2,
            fragmentManager.beginTransaction().add(24, new DummyFragment(), "wilma").addToBackStack("wilma").commit());

        assertEquals(3, fragmentManager.getBackStackEntryCount());
        assertTrue(fragmentManager.popBackStackImmediate("fred", FragmentManager.POP_BACK_STACK_INCLUSIVE));

        assertEmptyBackStack(fragmentManager);
    }

    @Test
    public void testFragmentManagerGetBackStackEntry() {
        ContainerActivity activity = new ContainerActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.beginTransaction().add(24, new DummyFragment(), "fred").addToBackStack("fred").commit();
        fragmentManager.beginTransaction().add(24, new DummyFragment(), "barney").addToBackStack("barney").commit();
        fragmentManager.beginTransaction().add(24, new DummyFragment(), "wilma").addToBackStack("wilma").commit();

        assertEquals("wilma", fragmentManager.getBackStackEntryAt(2).getName());
        assertEquals("barney", fragmentManager.getBackStackEntryAt(1).getName());
        assertEquals("fred", fragmentManager.getBackStackEntryAt(0).getName());
    }

    @Test
    public void testVisibleAndAdded() throws Exception {
        DummyFragment fragment = new DummyFragment();
        assertFalse(fragment.isAdded());
        assertFalse(fragment.isVisible());

        final ShadowFragment shadow = shadowOf(fragment);
        shadow.setActivity(new ContainerActivity());
        assertTrue(fragment.isAdded());
        assertTrue(fragment.isVisible());
    }

    @Test
    public void testHeadlessFragmentOnAttachAndOnCreateAreCalled() throws Exception {
        DummyHeadlessFragment fragment = new DummyHeadlessFragment();
        ContainerActivity activity = new ContainerActivity();
        activity.getSupportFragmentManager().beginTransaction().add(fragment, null).commit();
        assertEquals(asList("onAttach", "onCreate"), fragment.log);
    }

    @Test
    public void testTargetFragment() throws Exception {
        DummyFragment fragmentTarget = new DummyFragment();
        DummyFragment fragment2 = new DummyFragment();

        fragment2.setTargetFragment(fragmentTarget, 0);

        assertSame(fragmentTarget, fragment2.getTargetFragment());
    }

    @Test
    public void testReplaceFragmentOnDestroyedActivity() throws Exception {
        DummyHeadlessFragment fragment = new DummyHeadlessFragment();
        ContainerActivity activity = new ContainerActivity();
        activity.destroy();
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction().add(fragment, null);
        try {
            transaction.commit();
            fail("Should not be able to commit fragment on destroyed activity");
        } catch (IllegalStateException e) {
            // Expected.
        }

        try {
            transaction.commitAllowingStateLoss();
            fail("Should not be able to commit fragment on destroyed activity");
        } catch (IllegalStateException e) {
            // Expected.
        }
    }

    @Test
    public void startActivity_shouldPassThroughToContainerActivity() throws Exception {
        DummyFragment fragment = new DummyFragment();
        ContainerActivity activity = new ContainerActivity();
        startFragment(activity, fragment);
        Intent intent = new Intent(activity, DummyStartedActivity.class);
        fragment.startActivity(intent);
        assertThat(activity, new StartedMatcher(intent));
    }

    @Test(expected = IllegalStateException.class)
    public void startActivity_shouldThrowExceptionIfContainerIsNull() throws Exception {
        DummyFragment fragment = new DummyFragment();
        ContainerActivity activity = new ContainerActivity();
        startFragment(activity, fragment);
        Intent intent = new Intent(activity, DummyStartedActivity.class);
        Robolectric.shadowOf(fragment).setActivity(null);
        fragment.startActivity(intent);
    }

    @Test
    public void startActivityForResult_shouldPassThroughToContainerActivity() throws Exception {
        DummyFragment fragment = new DummyFragment();
        ContainerActivity activity = new ContainerActivity();
        startFragment(activity, fragment);
        Intent intent = new Intent(activity, DummyStartedActivity.class);
        fragment.startActivityForResult(intent, 0);
        assertThat(activity, new StartedMatcher(intent));
    }

    @Test(expected = IllegalStateException.class)
    public void startActivityForResult_shouldThrowExceptionIfContainerIsNull() throws Exception {
        DummyFragment fragment = new DummyFragment();
        ContainerActivity activity = new ContainerActivity();
        startFragment(activity, fragment);
        Intent intent = new Intent(activity, DummyStartedActivity.class);
        Robolectric.shadowOf(fragment).setActivity(null);
        fragment.startActivityForResult(intent, 0);
    }

    private void startFragment(DummyFragment fragment) {
        ContainerActivity containerActivity = new ContainerActivity();
        startFragment(containerActivity, fragment);
    }

    private void startFragment(ContainerActivity containerActivity, DummyFragment fragment) {
        containerActivity.getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fragment, null).commit();
    }

    private static class DummyFragment extends Fragment {

        public static final String ARG_KEY = "argy";

        private Object argument;

        private int serial;
        private int created;
        private int activityCreated;
        private int resumed;
        private int viewCreated;
        private int onViewCreated;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                argument = getArguments().get(ARG_KEY);
            }

            created = ++serial;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            activityCreated = ++serial;
        }

        @Override
        public void onResume() {
            super.onResume();
            resumed = ++serial;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            viewCreated = ++serial;
            return inflater.inflate(R.layout.fragment_contents, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            onViewCreated = ++serial;
        }
    }

    private static class DummyHeadlessFragment extends Fragment {
        List<String> log = new ArrayList<String>();

        @Override public void onAttach(Activity activity) {
            super.onAttach(activity);
            log.add("onAttach");
        }

      @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            log.add("onCreate");
        }
    }

    private static class ContainerActivity extends FragmentActivity {
        public void destroy() {
            onDestroy();
        }
    }

    private static class DummyStartedActivity extends Activity {
        // Intentionally empty. Used to test starting a new activity from a fragment.
    }
}
