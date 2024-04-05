package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.wifi.ScanResult.InformationElement;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link InformationElementBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.R)
public class InformationElementBuilderTest {
  @Test
  public void build_informationElement() {
    byte[] bytes = new byte[] {1, 2, 3, 4};
    InformationElement informationElement =
        InformationElementBuilder.newBuilder().setId(1).setIdExt(2).setBytes(bytes).build();

    assertThat(informationElement).isNotNull();
    assertThat(informationElement.getId()).isEqualTo(1);
    assertThat(informationElement.getIdExt()).isEqualTo(2);
    assertThat(informationElement.getBytes()).isEqualTo(ByteBuffer.wrap(bytes));
  }
}
