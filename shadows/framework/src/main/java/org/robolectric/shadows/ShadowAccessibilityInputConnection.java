package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.accessibilityservice.InputMethod.AccessibilityInputConnection;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.inputmethod.SurroundingText;
import android.view.inputmethod.TextAttribute;
import com.android.internal.inputmethod.RemoteAccessibilityInputConnection;
import com.google.auto.value.AutoValue;
import java.util.List;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow of AccessibilityInputConnection that provides a mechanism to simulate text input for
 * Accessibility services using InputConnection APIs. You can get an AccessibilityInputConnection to
 * shadow by calling {@code ShadowAccessibilityInputMethod#setIsConnected(true)} and then calling
 * {@code InputMethod#getCurrentInputConnection()} on the shadowed InputMethod.
 *
 * <p>All AccessibilityInputConnection objects which share the same remote will return the same
 * values from their shadow methods. This is equivalent to the behavior of the real
 * AccessibilityInputConnections.
 */
@Implements(value = AccessibilityInputConnection.class, minSdk = TIRAMISU, isInAndroidSdk = false)
public class ShadowAccessibilityInputConnection {

  @RealObject private AccessibilityInputConnection realInputConnection;

  /** A class that holds the arguments passed to {@link #getSurroundingText(int, int, int)}. */
  @AutoValue
  public abstract static class SurroundingTextArgs {
    static SurroundingTextArgs create(int beforeLength, int afterLength, int flags) {
      return new AutoValue_ShadowAccessibilityInputConnection_SurroundingTextArgs(
          beforeLength, afterLength, flags);
    }

    public abstract int beforeLength();

    public abstract int afterLength();

    public abstract int flags();
  }

  /**
   * A class that holds the arguments passed to {@link #commitText(CharSequence, int,
   * TextAttribute)}.
   */
  @AutoValue
  public abstract static class CommitTextArgs {
    static CommitTextArgs create(
        CharSequence text, int newCursorPosition, TextAttribute textAttribute) {
      return new AutoValue_ShadowAccessibilityInputConnection_CommitTextArgs(
          text, newCursorPosition, textAttribute);
    }

    public abstract CharSequence text();

    public abstract int newCursorPosition();

    public abstract TextAttribute textAttribute();
  }

  /**
   * Returns the list of arguments passed to {@link #commitText(CharSequence, int, TextAttribute)}.
   */
  public List<CommitTextArgs> getCommitTextArguments() {
    return getRemote().getCommitTextArguments();
  }

  /** Sets the surrounding text to be returned by {@link #getSurroundingText(int, int, int)}. */
  public void setSurroundingText(SurroundingText surroundingText) {
    getRemote().setSurroundingText(surroundingText);
  }

  /** Returns the list of arguments passed to {@link #getSurroundingText(int, int, int)}. */
  public List<SurroundingTextArgs> getSurroundingTextArguments() {
    return getRemote().getSurroundingTextArguments();
  }

  /**
   * Returns the list of context menu actions performed on this input connection in the order they
   * were received..
   */
  public List<Integer> getContextMenuActions() {
    return getRemote().getContextMenuActions();
  }

  /**
   * Returns the list of editor actions performed on this input connection in the order they were
   * received.
   */
  public List<Integer> getEditorActions() {
    return getRemote().getEditorActions();
  }

  /**
   * Returns the list of key events sent to this input connection in the order they were received.
   */
  public List<KeyEvent> getKeyEvents() {
    return getRemote().getKeyEvents();
  }

  /**
   * Returns the list of selections sent to this input connection in the order they were received.
   */
  public List<Pair<Integer, Integer>> getSetSelections() {
    return getRemote().getSetSelections();
  }

  private ShadowRemoteAccessibilityInputConnection getRemote() {
    return Shadow.extract(
        reflector(AccessibilityInputConnectionReflector.class, realInputConnection).getIc());
  }

  @ForType(AccessibilityInputConnection.class)
  interface AccessibilityInputConnectionReflector {
    @Accessor("mIc")
    RemoteAccessibilityInputConnection getIc();
  }
}
