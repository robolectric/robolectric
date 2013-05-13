package org.robolectric.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(TestRunners.WithDefaults.class)
public class FragmentTestUtilTest {
  private LoginFragment fragment;

  @Before
  public void setUp() {
    fragment = new LoginFragment();
    startFragment(fragment);

    assertThat(fragment.getActivity(), notNullValue());
    assertThat(fragment.getView(), notNullValue());
  }

  @Test
  public void tacos_should_be_found() {
    assertThat(fragment.getView().findViewById(R.id.tacos), notNullValue());
  }
}

class LoginFragment extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_contents, container, false);
  }
}
