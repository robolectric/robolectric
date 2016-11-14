import android.util.AttributeSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

@RunWith(TestRunners.WithDefaults.class)
public class PerfTest {
  @Rule public PerfMetrics perfMetrics = new PerfMetrics();
  private AttributeSet attributeSet;

  @Before
  public void setUp() throws Exception {
    System.out.println("setUp");
    attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.layout_height, "127dp")
        .addAttribute(android.R.attr.text, "@")
        .build();
  }

  @Test public void obtainStyledAttributes() throws Exception {
    RuntimeEnvironment.application.obtainStyledAttributes(
        attributeSet, new int[]{android.R.attr.layout_height});
  }
}
