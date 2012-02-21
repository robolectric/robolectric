package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class FragmentTest {

    @Test
    public void testOnCreateView() throws Exception {
        DummyFragment fragment = new DummyFragment();
        final ShadowFragment shadow = shadowOf(fragment);
        final ContainerActivity activity = new ContainerActivity();
        shadow.setActivity(activity);
        shadow.createView();
        assertNotNull(fragment.getActivity());
        assertNotNull(fragment.getView());
        TextView tacos = (TextView) fragment.getView().findViewById(R.id.tacos);
        assertNotNull(tacos);
        assertEquals("TACOS", tacos.getText());
    }

    @Test 
    public void testArguments() {
        DummyFragment fragment = new DummyFragment();
        final ShadowFragment shadow = shadowOf(fragment);
        shadow.setActivity(new ContainerActivity());
        final Bundle bundle = new Bundle();
        final int bundleVal = 15;
        bundle.putInt(DummyFragment.ARG_KEY, bundleVal);
        shadow.setArguments(bundle);
        shadow.createView();
        assertEquals(bundleVal, fragment.argument);
    }
    
    private static class DummyFragment extends Fragment {

        public static final String ARG_KEY = "argy";

        private Object argument;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                argument = getArguments().get(ARG_KEY);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_contents, container, false);
        }
    }

    private static class ContainerActivity extends FragmentActivity {
        
    }
}
