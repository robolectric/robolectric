package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.util.Pair;
import android.view.KeyEvent;
import android.view.inputmethod.SurroundingText;
import android.view.inputmethod.TextAttribute;
import com.android.internal.inputmethod.RemoteAccessibilityInputConnection;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAccessibilityInputConnection.CommitTextArgs;
import org.robolectric.shadows.ShadowAccessibilityInputConnection.SurroundingTextArgs;
import org.robolectric.shadows.ShadowAccessibilityInputConnection.SurroundingTextCallback;

/** Shadow for RemoteAccessibilityInputConnection, used by AccessibilityInputConnection. */
@Implements(
    value = RemoteAccessibilityInputConnection.class,
    minSdk = TIRAMISU,
    isInAndroidSdk = false)
public class ShadowRemoteAccessibilityInputConnection {

  private SurroundingTextCallback surroundingTextCallback;

  private final List<Integer> contextMenuActions = new ArrayList<>();
  private final List<Integer> editorActions = new ArrayList<>();
  private final List<KeyEvent> keyEvents = new ArrayList<>();
  private final List<Pair<Integer, Integer>> setSelections = new ArrayList<>();
  private final List<SurroundingTextArgs> surroundingTextArguments = new ArrayList<>();
  private final List<CommitTextArgs> commitTextArguments = new ArrayList<>();
  private final List<Pair<Integer, Integer>> deleteSurroundingTextArguments = new ArrayList<>();

  @Implementation(minSdk = TIRAMISU)
  protected void commitText(CharSequence text, int newCursorPosition, TextAttribute textAttribute) {
    commitTextArguments.add(CommitTextArgs.create(text, newCursorPosition, textAttribute));
  }

  @Implementation(minSdk = TIRAMISU)
  protected SurroundingText getSurroundingText(int beforeLength, int afterLength, int flags) {
    surroundingTextArguments.add(SurroundingTextArgs.create(beforeLength, afterLength, flags));
    if (surroundingTextCallback != null) {
      return surroundingTextCallback.get(beforeLength, afterLength, flags);
    }
    return null;
  }

  @Implementation(minSdk = TIRAMISU)
  protected void performContextMenuAction(int id) {
    contextMenuActions.add(id);
  }

  @Implementation(minSdk = TIRAMISU)
  protected void performEditorAction(int editorAction) {
    editorActions.add(editorAction);
  }

  @Implementation(minSdk = TIRAMISU)
  protected void sendKeyEvent(KeyEvent event) {
    keyEvents.add(event);
  }

  @Implementation(minSdk = TIRAMISU)
  protected void setSelection(int start, int end) {
    setSelections.add(Pair.create(start, end));
  }

  @Implementation(minSdk = TIRAMISU)
  protected void deleteSurroundingText(int beforeLength, int afterLength) {
    deleteSurroundingTextArguments.add(Pair.create(beforeLength, afterLength));
  }

  /**
   * Returns the list of arguments passed to {@link #commitText(CharSequence, int, TextAttribute)}.
   */
  List<CommitTextArgs> getCommitTextArguments() {
    return ImmutableList.copyOf(commitTextArguments);
  }

  /** Sets a custom callback to define the behavior of getSurroundingText. */
  void setSurroundingTextCallback(SurroundingTextCallback callback) {
    this.surroundingTextCallback = callback;
  }

  /** Returns the list of arguments passed to {@link #getSurroundingText(int, int, int)}. */
  List<SurroundingTextArgs> getSurroundingTextArguments() {
    return ImmutableList.copyOf(surroundingTextArguments);
  }

  /**
   * Returns the list of context menu actions performed on this input connection in the order they
   * were received..
   */
  List<Integer> getContextMenuActions() {
    return ImmutableList.copyOf(contextMenuActions);
  }

  /**
   * Returns the list of editor actions performed on this input connection in the order they were
   * received..
   */
  List<Integer> getEditorActions() {
    return ImmutableList.copyOf(editorActions);
  }

  /**
   * Returns the list of key events sent to this input connection in the order they were received.
   */
  List<KeyEvent> getKeyEvents() {
    return ImmutableList.copyOf(keyEvents);
  }

  /**
   * Returns the list of selections sent to this input connection in the order they were received.
   */
  List<Pair<Integer, Integer>> getSetSelections() {
    return ImmutableList.copyOf(setSelections);
  }

  /**
   * Returns the list of deleteSurroundingText arguments sent to this input connection in the order
   * they were received.
   */
  List<Pair<Integer, Integer>> getDeleteSurroundingTextArguments() {
    return ImmutableList.copyOf(deleteSurroundingTextArguments);
  }
}
