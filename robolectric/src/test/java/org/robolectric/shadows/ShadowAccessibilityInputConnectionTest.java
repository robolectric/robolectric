package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.InputMethod;
import android.accessibilityservice.InputMethod.AccessibilityInputConnection;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.SurroundingText;
import android.view.inputmethod.TextAttribute;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.testing.TestAccessibilityService;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = TIRAMISU)
public final class ShadowAccessibilityInputConnectionTest {
  private AccessibilityInputConnection inputConnection;
  private ShadowAccessibilityInputConnection shadow;

  @Before
  public void setUp() {
    TestAccessibilityService service = Robolectric.setupService(TestAccessibilityService.class);
    AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
    serviceInfo.flags |= AccessibilityServiceInfo.FLAG_INPUT_METHOD_EDITOR;
    service.setServiceInfo(serviceInfo);
    shadowOf(service).startInput(new EditorInfo());
    InputMethod inputMethod = service.getInputMethod();
    inputConnection = inputMethod.getCurrentInputConnection();
    shadow = Shadow.extract(inputConnection);
  }

  @Test
  public void commitText_recordsArguments() {
    assertThat(shadow.getCommitTextArguments()).isEmpty();

    TextAttribute textAttribute = new TextAttribute.Builder().build();
    inputConnection.commitText("Hello", 0, textAttribute);
    assertThat(shadow.getCommitTextArguments()).hasSize(1);
    ShadowAccessibilityInputConnection.CommitTextArgs args = shadow.getCommitTextArguments().get(0);
    assertThat(args.text.toString()).isEqualTo("Hello");
    assertThat(args.newCursorPosition).isEqualTo(0);
    assertThat(args.textAttribute).isEqualTo(textAttribute);

    List<String> suggestions = Arrays.asList("Suggestion 1", "Suggestion 2", "Suggestion 3");
    TextAttribute textAttribute2 =
        new TextAttribute.Builder().setTextConversionSuggestions(suggestions).build();
    inputConnection.commitText("World", 1, textAttribute2);
    assertThat(shadow.getCommitTextArguments()).hasSize(2);
    args = shadow.getCommitTextArguments().get(1);
    assertThat(args.text.toString()).isEqualTo("World");
    assertThat(args.newCursorPosition).isEqualTo(1);
    assertThat(args.textAttribute).isEqualTo(textAttribute2);
  }

  @Test
  public void getSurroundingText_returnsNullIfNotSet() {
    assertThat(inputConnection.getSurroundingText(0, 0, 0)).isNull();
  }

  @Test
  public void getSurroundingText_returnsSetSurroundingText() {
    SurroundingText surroundingText = new SurroundingText("And to institute new", 0, 42, 70);
    shadow.setSurroundingText(surroundingText);
    assertThat(inputConnection.getSurroundingText(0, 0, 0)).isEqualTo(surroundingText);
  }

  @Test
  public void getSurroundingText_recordsArguments() {
    assertThat(shadow.getSurroundingTextArguments()).isEmpty();

    inputConnection.getSurroundingText(0, 0, 0);
    assertThat(shadow.getSurroundingTextArguments()).hasSize(1);
    ShadowAccessibilityInputConnection.SurroundingTextArgs args =
        shadow.getSurroundingTextArguments().get(0);
    assertThat(args.beforeLength()).isEqualTo(0);
    assertThat(args.afterLength()).isEqualTo(0);
    assertThat(args.flags()).isEqualTo(0);

    inputConnection.getSurroundingText(1, 22, 1973);
    assertThat(shadow.getSurroundingTextArguments()).hasSize(2);
    args = shadow.getSurroundingTextArguments().get(1);
    assertThat(args.beforeLength()).isEqualTo(1);
    assertThat(args.afterLength()).isEqualTo(22);
    assertThat(args.flags()).isEqualTo(1973);
  }

  @Test
  public void performContextMenuAction_recordsActions() {
    assertThat(shadow.getContextMenuActions()).isEmpty();

    inputConnection.performContextMenuAction(android.R.id.cut);
    assertThat(shadow.getContextMenuActions()).containsExactly(android.R.id.cut);

    inputConnection.performContextMenuAction(android.R.id.paste);
    assertThat(shadow.getContextMenuActions())
        .containsExactly(android.R.id.cut, android.R.id.paste);
  }

  @Test
  public void performEditorAction_recordsActions() {
    assertThat(shadow.getEditorActions()).isEmpty();

    inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH);
    assertThat(shadow.getEditorActions()).containsExactly(EditorInfo.IME_ACTION_SEARCH);

    inputConnection.performEditorAction(EditorInfo.IME_ACTION_NEXT);
    assertThat(shadow.getEditorActions())
        .containsExactly(EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_NEXT);
  }

  @Test
  public void sendKeyEvent_recordsEvents() {
    assertThat(shadow.getKeyEvents()).isEmpty();

    KeyEvent firstEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
    inputConnection.sendKeyEvent(firstEvent);
    assertThat(shadow.getKeyEvents()).containsExactly(firstEvent);

    KeyEvent secondEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
    inputConnection.sendKeyEvent(secondEvent);
    assertThat(shadow.getKeyEvents()).containsExactly(firstEvent, secondEvent);
  }

  @Test
  public void setSelection_recordsSelections() {
    assertThat(shadow.getSetSelections()).isEmpty();

    inputConnection.setSelection(0, 1);
    assertThat(shadow.getSetSelections()).containsExactly(Pair.create(0, 1));

    inputConnection.setSelection(1, 2);
    assertThat(shadow.getSetSelections()).containsExactly(Pair.create(0, 1), Pair.create(1, 2));
  }
}
