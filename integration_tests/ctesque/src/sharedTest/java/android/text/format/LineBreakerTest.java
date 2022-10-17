package android.text.format;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Paint;
import android.graphics.text.LineBreaker;
import android.graphics.text.LineBreaker.ParagraphConstraints;
import android.graphics.text.LineBreaker.Result;
import android.graphics.text.MeasuredText;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests that Robolectric's android.graphics.text.LineBreaker support is consistent with device. */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class LineBreakerTest {
  @Test
  @Config(minSdk = Q)
  public void breakLines() {
    String text = "Hello, Android.";
    MeasuredText mt =
        new MeasuredText.Builder(text.toCharArray())
            .appendStyleRun(new Paint(), text.length(), false)
            .build();

    LineBreaker lb =
        new LineBreaker.Builder()
            .setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE)
            .setHyphenationFrequency(LineBreaker.HYPHENATION_FREQUENCY_NONE)
            .build();

    ParagraphConstraints c = new ParagraphConstraints();
    c.setWidth(240);
    Result r = lb.computeLineBreaks(mt, c, 0);
    assertThat(r.getLineCount()).isEqualTo(1);
    assertThat(r.getLineBreakOffset(0)).isEqualTo(15);
  }
}
