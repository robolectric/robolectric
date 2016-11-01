import android.app.Activity;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.android.internal.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PerfTest {
  @Rule public PerfRule perfRule = new PerfRule();

  private Activity activity;
  private AttributeSet attributeSet;

  @Test
  public void obtainStyledAttributes() throws Exception {
    perfRule.setUp(new Runnable() {
      @Override
      public void run() {
        attributeSet = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.layout_width, "" + ViewGroup.LayoutParams.MATCH_PARENT)
            .addAttribute(android.R.attr.layout_height, "" + ViewGroup.LayoutParams.MATCH_PARENT)
            .build();

        activity = Robolectric.setupActivity(Activity.class);
      }
    }).execute(new Runnable() {
      @Override
      public void run() {
        activity.obtainStyledAttributes(attributeSet, R.styleable.ListView);
      }
    });
  }

  @Test
  public void setupActivity() throws Exception {
    perfRule.execute(new Runnable() {
      @Override
      public void run() {
        Robolectric.setupActivity(Activity.class);
      }
    });
  }
}