package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class FragmentTest {

    @Test
    public void testOnCreateOnResumeOnCreateView() throws Exception {
        DummyFragment fragment = new DummyFragment();
        startFragment(fragment);

        assertNotNull(fragment.getActivity());
        assertNotNull(fragment.getView());
        TextView tacos = (TextView) fragment.getView().findViewById(R.id.tacos);
        assertNotNull(tacos);
        assertEquals("TACOS", tacos.getText());

        assertEquals(1, fragment.created);
        assertEquals(2, fragment.viewCreated);
        assertEquals(3, fragment.resumed);

        assertTrue(fragment.isResumed());
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
    public void testHeadlessFragmentOnCreateIsCalled() throws Exception {
        DummyHeadlessFragment fragment = new DummyHeadlessFragment();
        ContainerActivity activity = new ContainerActivity();
        activity.getSupportFragmentManager().beginTransaction().add(fragment, null).commit();
        assertTrue(fragment.created);
    }
    
    @Test
    public void testTargetFragment() throws Exception {
        DummyFragment fragmentTarget = new DummyFragment();
        DummyFragment fragment2 = new DummyFragment();

        fragment2.setTargetFragment(fragmentTarget, 0);

        assertSame(fragmentTarget, fragment2.getTargetFragment());
    }

    private void startFragment(DummyFragment fragment) {
        new ContainerActivity().getSupportFragmentManager().beginTransaction().add(fragment, null).commit();
    }

    private static class DummyFragment extends Fragment {

        public static final String ARG_KEY = "argy";

        private Object argument;
        
        private int serial;
        private int created;
        private int resumed;
        private int viewCreated;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                argument = getArguments().get(ARG_KEY);
            }

            created = ++serial;
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
    }
    
    private static class DummyHeadlessFragment extends Fragment {
        boolean created = false;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            created = true;
        }
    }

    private static class ContainerActivity extends FragmentActivity {
        
    }
}
