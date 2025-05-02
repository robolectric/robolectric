package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.accessibilityservice.InputMethod.AccessibilityInputConnection;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.inputmethod.SurroundingText;
import android.view.inputmethod.TextAttribute;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of AccessibilityInputConnection that provides a mechanism to simulate text input for
 * Accessibility services using InputConnection APIs. You can get an AccessibilityInputConnection to
 * shadow by calling {@code ShadowAccessibilityInputMethod#setIsConnected(true)} and then calling
 * {@code InputMethod#getCurrentInputConnection()} on the shadowed InputMethod.
 */
@Implements(value = AccessibilityInputConnection.class, minSdk = TIRAMISU, isInAndroidSdk = false)
public class ShadowAccessibilityInputConnection {

  private SurroundingText surroundingTextToReturn = null;

  private final List<Integer> contextMenuActions = new ArrayList<>();
  private final List<Integer> editorActions = new ArrayList<>();
  private final List<KeyEvent> keyEvents = new ArrayList<>();
  private final List<Pair<Integer, Integer>> setSelections = new ArrayList<>();
  private final List<SurroundingTextArgs> surroundingTextArguments = new ArrayList<>();
  private final List<CommitTextArgs> commitTextArguments = new ArrayList<>();

  /** A class that holds the arguments passed to {@link #getSurroundingText(int, int, int)}. */
  @AutoValue
  public abstract static class SurroundingTextArgs {
    static SurroundingTextArgs create(int beforeLength, int afterLength, int flags) {
      return new AutoValue_ShadowAccessibilityInputConnection_SurroundingTextArgs(
          beforeLength, afterLength, flags);
    }

    abstract int beforeLength();

    abstract int afterLength();

    abstract int flags();
  }

  /**
   * A class that holds the arguments passed to {@link #commitText(CharSequence, int,
   * TextAttribute)}.
   */
  public static class CommitTextArgs {
    final CharSequence text;
    final int newCursorPosition;
    final TextAttribute textAttribute;

    CommitTextArgs(CharSequence text, int newCursorPosition, TextAttribute textAttribute) {
      this.text = text;
      this.newCursorPosition = newCursorPosition;
      this.textAttribute = textAttribute;
    }
  }

  @Implementation(minSdk = TIRAMISU)
  protected void commitText(CharSequence text, int newCursorPosition, TextAttribute textAttribute) {
    commitTextArguments.add(new CommitTextArgs(text, newCursorPosition, textAttribute));
  }

  @Implementation(minSdk = TIRAMISU)
  protected SurroundingText getSurroundingText(int beforeLength, int afterLength, int flags) {
    surroundingTextArguments.add(SurroundingTextArgs.create(beforeLength, afterLength, flags));
    return surroundingTextToReturn;
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

  /**
   * Returns the list of arguments passed to {@link #commitText(CharSequence, int, TextAttribute)}.
   */
  public List<CommitTextArgs> getCommitTextArguments() {
    return ImmutableList.copyOf(commitTextArguments);
  }

  /** Sets the surrounding text to be returned by {@link #getSurroundingText(int, int, int)}. */
  public void setSurroundingText(SurroundingText surroundingText) {
    this.surroundingTextToReturn = surroundingText;
  }

  /** Returns the list of arguments passed to {@link #getSurroundingText(int, int, int)}. */
  public List<SurroundingTextArgs> getSurroundingTextArguments() {
    return ImmutableList.copyOf(surroundingTextArguments);
  }

  /**
   * Returns the list of context menu actions performed on this input connection in the order they
   * were received..
   */
  public List<Integer> getContextMenuActions() {
    return ImmutableList.copyOf(contextMenuActions);
  }

  /**
   * Returns the list of editor actions performed on this input connection in the order they were
   * received..
   */
  public List<Integer> getEditorActions() {
    return ImmutableList.copyOf(editorActions);
  }

  /**
   * Returns the list of key events sent to this input connection in the order they were received.
   */
  public List<KeyEvent> getKeyEvents() {
    return ImmutableList.copyOf(keyEvents);
  }

  /**
   * Returns the list of selections sent to this input connection in the order they were received.
   */
  public List<Pair<Integer, Integer>> getSetSelections() {
    return ImmutableList.copyOf(setSelections);
  }
}
