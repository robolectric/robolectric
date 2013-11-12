package android.content;

public class TestIntentSender extends IntentSender {
  public Intent intent;

  public TestIntentSender() {
    super((IIntentSender)null);
  }
}
