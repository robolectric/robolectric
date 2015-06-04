package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowNotification.ShadowBigTextStyle;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowNotificationBigTextStyleTest {

  private final Builder builder = new Builder(application);
  private final BigTextStyle style = new BigTextStyle(builder);
  private final ShadowBigTextStyle sStyle = shadowOf(style);
  private Notification notification;
  private ShadowNotification s;

  @Test
  public void build_setsBigTextStyleOnNotification() throws Exception {
    build();
    assertThat(s.getStyle()).isSameAs(style);
  }

  @Test
  public void build_callsThroughTo_ShadowBuilder_build() throws Exception {
    builder.setSmallIcon(76);
    build();
    assertThat(s.getSmallIcon()).isEqualTo(76);
  }

  @Test
  public void bigText_setsBigText() {
    final String BIG_TEXT = "This is the big text";
    style.bigText(BIG_TEXT);
    assertThat(sStyle.getBigText().toString()).isEqualTo(BIG_TEXT);
  }

  @Test
  public void setBigContentTitle_setsBigContentTitle() {
    final String BIG_TITLE = "Big Title";
    style.setBigContentTitle(BIG_TITLE);
    assertThat(sStyle.getBigContentTitle().toString()).isEqualTo(BIG_TITLE);
  }

  @Test
  public void setSummaryText_setsSummaryText() {
    final String SUMMARY = "summary";
    style.setSummaryText(SUMMARY);
    assertThat(sStyle.getSummaryText().toString()).isEqualTo(SUMMARY);
  }

  @Test
  public void bigText_handlesNull() {
    style.bigText(null);
    assertThat(sStyle.getBigText()).isNull();
  }

  @Test
  public void setBigContentTitle_handlesNull() {
    style.setBigContentTitle(null);
    assertThat(sStyle.getBigContentTitle()).isNull();
  }

  @Test
  public void setSummaryText_handlesNull() {
    style.setSummaryText(null);
    assertThat(sStyle.getSummaryText()).isNull();
  }

  private void build() {
    notification = style.build();
    s = shadowOf(notification);
  }
}
