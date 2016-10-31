import android.app.Activity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class SomeTest {
  ShadowApplication shadowApplication;

  @Test
  public void thing() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    System.out.println("activity = " + activity);
  }
}