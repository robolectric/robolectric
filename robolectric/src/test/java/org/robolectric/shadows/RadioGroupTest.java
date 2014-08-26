package org.robolectric.shadows;

import android.widget.RadioGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.ArrayList;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class RadioGroupTest {
  private static final int BUTTON_ID = 3245;

  @Test
  public void checkedRadioButtonId() throws Exception {
    RadioGroup radioGroup = new RadioGroup(Robolectric.application);
    assertThat(radioGroup.getCheckedRadioButtonId()).isEqualTo(-1);
    radioGroup.check(99);
    assertThat(radioGroup.getCheckedRadioButtonId()).isEqualTo(99);
  }

  @Test
  public void check_shouldCallOnCheckedChangeListener() throws Exception {
    RadioGroup radioGroup = new RadioGroup(Robolectric.application);
    TestOnCheckedChangeListener listener = new TestOnCheckedChangeListener();
    radioGroup.setOnCheckedChangeListener(listener);

    radioGroup.check(BUTTON_ID);

    assertEquals(Arrays.asList(BUTTON_ID), listener.onCheckedChangedCheckedIds);
    assertEquals(Arrays.asList(radioGroup), listener.onCheckedChangedGroups);
  }

  @Test
  public void clearCheck_shouldCallOnCheckedChangeListenerTwice() throws Exception {
    RadioGroup radioGroup = new RadioGroup(Robolectric.application);
    TestOnCheckedChangeListener listener = new TestOnCheckedChangeListener();

    radioGroup.check(BUTTON_ID);
    radioGroup.setOnCheckedChangeListener(listener);
    radioGroup.clearCheck();

    assertEquals(Arrays.asList(-1), listener.onCheckedChangedCheckedIds);
    assertEquals(Arrays.asList(radioGroup), listener.onCheckedChangedGroups);
  }

  private static class TestOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
    public ArrayList<RadioGroup> onCheckedChangedGroups = new ArrayList<RadioGroup>();
    public ArrayList<Integer> onCheckedChangedCheckedIds = new ArrayList<Integer>();

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
      onCheckedChangedGroups.add(group);
      onCheckedChangedCheckedIds.add(checkedId);
    }
  }
}
