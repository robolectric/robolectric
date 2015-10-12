package org.robolectric.shadows;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.EditText;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowKeyCharacterMapTest {

  // This is an end-to-end test of key event dispatch which relies on
  // the ShadowKeyCharacterMap at several points in the process.
  @Test

  public void test_dispatchKeyEvents() throws Exception {
    test_dispatchKeyEvents( "string" );

    /* TODO make these pass
    test_dispatchKeyEvents( "STRing" );
    test_dispatchKeyEvents( "A full sentence with punctuation.!?-+=&\"");
    */
  }

  private void test_dispatchKeyEvents(String toEnter) throws Exception {

    // Setup
    EditText text = new EditText( RuntimeEnvironment.application );
    KeyCharacterMap keyMap = ShadowKeyCharacterMap.load( 0 );
    text.requestFocus();

    // Get key events and dispatch them to a view
    for(KeyEvent evt : keyMap.getEvents( toEnter.toCharArray() )){
      text.dispatchKeyEvent( evt );
    }

    Thread.sleep( 500 );
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    // Assert that the textview contains the correct text for the inputs
    assertEquals( text.getText(), toEnter );
  }
}
