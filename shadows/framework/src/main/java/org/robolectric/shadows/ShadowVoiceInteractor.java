package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.VoiceInteractor;
import android.app.VoiceInteractor.AbortVoiceRequest;
import android.app.VoiceInteractor.CommandRequest;
import android.app.VoiceInteractor.CompleteVoiceRequest;
import android.app.VoiceInteractor.ConfirmationRequest;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.Prompt;
import android.app.VoiceInteractor.Request;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow implementation of {@link android.app.VoiceInteractor}. */
@Implements(value = VoiceInteractor.class, minSdk = M)
public class ShadowVoiceInteractor {

  private int directActionsInvalidationCount = 0;
  private final List<String> voiceInteractions = new CopyOnWriteArrayList<>();
  public static String assistantPackageName = "test_package";

  @Implementation(minSdk = TIRAMISU)
  protected String getPackageName() {
    return assistantPackageName;
  }

  public void setPackageName(String packageName) {
    assistantPackageName = packageName;
  }

  @Implementation(minSdk = Q)
  protected void notifyDirectActionsChanged() {
    directActionsInvalidationCount += 1;
  }

  @Implementation(minSdk = Q)
  protected boolean submitRequest(Request request, String name) {
    if (request instanceof ConfirmationRequest) {
      processPrompt(reflector(ReflectorConfirmationRequest.class, request).getPrompt());
    } else if (request instanceof CompleteVoiceRequest) {
      processPrompt(reflector(ReflectorCompleteVoiceRequest.class, request).getPrompt());
    } else if (request instanceof AbortVoiceRequest) {
      processPrompt(reflector(ReflectorAbortVoiceRequest.class, request).getPrompt());
    } else if (request instanceof CommandRequest) {
      voiceInteractions.add(reflector(ReflectorCommandRequest.class, request).getCommand());
    } else if (request instanceof PickOptionRequest) {
      processPrompt(reflector(ReflectorPickOptionRequest.class, request).getPrompt());
    }
    return true;
  }

  @Implementation(minSdk = Q)
  protected boolean submitRequest(Request request) {
    return submitRequest(request, null);
  }

  /**
   * Returns the number of times {@code notifyDirectActionsChanged} was called on the {@link
   * android.app.VoiceInteractor} instance associated with this shadow
   */
  public int getDirectActionsInvalidationCount() {
    return directActionsInvalidationCount;
  }

  /**
   * Returns the voice interactions called on {@link VoiceInteractor} instance associated with this
   * shadow.
   */
  public List<String> getVoiceInteractions() {
    return voiceInteractions;
  }

  private void processPrompt(Prompt prompt) {
    if (prompt.countVoicePrompts() <= 0) {
      return;
    }
    for (int i = 0; i < prompt.countVoicePrompts(); i++) {
      voiceInteractions.add(prompt.getVoicePromptAt(i).toString());
    }
  }

  /** Accessor interface for {@link CompleteVoiceRequest}'s internal. */
  @ForType(CompleteVoiceRequest.class)
  interface ReflectorCompleteVoiceRequest {
    @Accessor("mPrompt")
    Prompt getPrompt();
  }

  /** Accessor interface for {@link ConfirmationRequest}'s internal. */
  @ForType(ConfirmationRequest.class)
  interface ReflectorConfirmationRequest {
    @Accessor("mPrompt")
    Prompt getPrompt();
  }

  /** Accessor interface for {@link AbortVoiceRequest}'s internal. */
  @ForType(AbortVoiceRequest.class)
  interface ReflectorAbortVoiceRequest {
    @Accessor("mPrompt")
    Prompt getPrompt();
  }

  /** Accessor interface for {@link CommandRequest}'s internal. */
  @ForType(CommandRequest.class)
  interface ReflectorCommandRequest {
    @Accessor("mCommand")
    String getCommand();
  }

  /** Accessor interface for {@link PickOptionRequest}'s internal. */
  @ForType(PickOptionRequest.class)
  interface ReflectorPickOptionRequest {
    @Accessor("mPrompt")
    Prompt getPrompt();
  }
}
