package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowArrayAdapterTest {
  private ArrayAdapter<Integer> arrayAdapter;

  @Before public void setUp() throws Exception {
    List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);

    arrayAdapter = new ArrayAdapter<>(RuntimeEnvironment.application, 0, list);
  }

  @Test
  public void verifyContext() {
    assertThat(arrayAdapter.getContext()).isSameAs(RuntimeEnvironment.application);
  }

  @Test
  @SuppressWarnings("BoxedPrimitiveConstructor")
  public void verifyListContent() {
    assertEquals(3, arrayAdapter.getCount());
    assertEquals(new Integer(1), arrayAdapter.getItem(0));
    assertEquals(new Integer(2), arrayAdapter.getItem(1));
    assertEquals(new Integer(3), arrayAdapter.getItem(2));
  }

  @Test
  public void usesTextViewResourceIdToSetTextWithinListItemView() throws Exception {
    ListView parent = new ListView(RuntimeEnvironment.application);
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RuntimeEnvironment.application, R.layout.main, R.id.title, new String[] { "first value" });
    View listItemView = arrayAdapter.getView(0, null, parent);
    TextView titleTextView = listItemView.findViewById(R.id.title);
    assertEquals("first value", titleTextView.getText().toString());
  }

  @Test
  public void hasTheCorrectConstructorResourceIDs() {
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(RuntimeEnvironment.application, R.id.title, new String[] { "first value" });

    //this assertion may look a little backwards since R.id.title is labeled
    //textViewResourceId in the constructor parameter list, but the output is correct.
    assertThat(Shadows.shadowOf(arrayAdapter).getResourceId()).isEqualTo(R.id.title);
    assertThat(Shadows.shadowOf(arrayAdapter).getTextViewResourceId()).isNotEqualTo(R.id.title);
    assertThat(Shadows.shadowOf(arrayAdapter).getTextViewResourceId()).isEqualTo(0);

    ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(RuntimeEnvironment.application, R.id.title);

    //this assertion may look a little backwards since R.id.title is labeled
    //textViewResourceId in the constructor parameter list, but the output is correct.
    assertThat(Shadows.shadowOf(arrayAdapter2).getResourceId()).isEqualTo(R.id.title);
    assertThat(Shadows.shadowOf(arrayAdapter2).getTextViewResourceId()).isNotEqualTo(R.id.title);
    assertThat(Shadows.shadowOf(arrayAdapter2).getTextViewResourceId()).isEqualTo(0);

    ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(RuntimeEnvironment.application, R.id.title, Arrays.asList(new String[] { "first value" }));

    //this assertion may look a little backwards since R.id.title is labeled
    //textViewResourceId in the constructor parameter list, but the output is correct.
    assertThat(Shadows.shadowOf(arrayAdapter3).getResourceId()).isEqualTo(R.id.title);
    assertThat(Shadows.shadowOf(arrayAdapter3).getTextViewResourceId()).isNotEqualTo(R.id.title);
    assertThat(Shadows.shadowOf(arrayAdapter3).getTextViewResourceId()).isEqualTo(0);
  }

  @Test
  public void shouldClear() throws Exception {
    arrayAdapter.clear();
    assertEquals(0, arrayAdapter.getCount());
  }

  @Test
  @SuppressWarnings("BoxedPrimitiveConstructor")
  public void test_remove() throws Exception {
    Integer firstItem = arrayAdapter.getItem(0);
    assertEquals(3, arrayAdapter.getCount());
    assertEquals(new Integer(1), firstItem);

    arrayAdapter.remove(firstItem);

    assertEquals(2, arrayAdapter.getCount());
    assertEquals(new Integer(2), arrayAdapter.getItem(0));
    assertEquals(new Integer(3), arrayAdapter.getItem(1));
  }
}
