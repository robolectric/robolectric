package org.robolectric.shadows;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class SpinnerTest {

  private Spinner spinner;

  @Before
  public void beforeTests() {
    spinner = new Spinner(Robolectric.application);
  }

  @Test
  public void testPrompt() {
    spinner.setPrompt("foo");

    assertThat(spinner.getPrompt().toString()).isEqualTo("foo");
  }

  @Test
  public void selectItemWithText_callsOnItemSelectedListener() throws Exception {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Robolectric.application, android.R.layout.simple_spinner_dropdown_item);
    adapter.add("Your Item");
    adapter.add("My Item");
    spinner.setAdapter(adapter);
    AdapterView.OnItemSelectedListener itemSelectedListener = mock(AdapterView.OnItemSelectedListener.class);
    spinner.setOnItemSelectedListener(itemSelectedListener);

    shadowOf(spinner).selectItemWithText("My Item");

    assertThat(spinner.getSelectedItem()).isEqualTo("My Item");
    verify(itemSelectedListener).onItemSelected(eq(spinner), (View) eq(null), eq(1), eq(1L));
  }
}
