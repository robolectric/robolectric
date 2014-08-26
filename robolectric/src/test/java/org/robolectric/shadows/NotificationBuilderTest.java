package org.robolectric.shadows;

import android.app.Notification.Style;
import android.app.PendingIntent;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Notification;

import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowNotification.Progress;

import static org.robolectric.Robolectric.*;
import static org.fest.assertions.api.Assertions.*;

@RunWith(TestRunners.WithDefaults.class)
public class NotificationBuilderTest {
  private Notification notification;
  private ShadowNotification s;
  private final Notification.Builder builder = new Notification.Builder(application);

  @Test
  public void build_setsContentTitleOnNotification() throws Exception {
    builder.setContentTitle("Hello");
    build();
    assertThat(s.getContentTitle().toString()).isEqualTo("Hello");
  }

  @Test
  public void build_setsContentTextOnNotification() throws Exception {
    builder.setContentText("Hello Text");
    build();
    assertThat(s.getContentText().toString()).isEqualTo("Hello Text");
  }

  @Test
  public void build_setsTickerOnNotification() throws Exception {
    builder.setTicker("My ticker");
    build();
    assertThat(s.getTicker().toString()).isEqualTo("My ticker");
  }

  @Test
  public void build_setsContentInfoOnNotification() throws Exception {
    builder.setContentInfo("11");
    build();
    assertThat(s.getContentInfo().toString()).isEqualTo("11");
  }

  @Test
  public void build_setsIconOnNotification() throws Exception {
    builder.setSmallIcon(9001);
    build();
    assertThat(s.getSmallIcon()).isEqualTo(9001);
  }

  @Test
  public void build_setsWhenOnNotification() throws Exception {
    builder.setWhen(11L);
    build();
    assertThat(s.getWhen()).isEqualTo(11L);
  }

  @Test
  public void build_setsProgressOnNotification_true() throws Exception {
    builder.setProgress(36, 57, true);
    build();
    Progress p = s.getProgress();
    assertThat(p.max).isEqualTo(36);
    assertThat(p.progress).isEqualTo(57);
    assertThat(p.indeterminate).isTrue();
  }

  @Test
  public void build_setsProgressOnNotification_false() throws Exception {
    builder.setProgress(34, 56, false);
    build();
    Progress p = s.getProgress();
    assertThat(p.max).isEqualTo(34);
    assertThat(p.progress).isEqualTo(56);
    assertThat(p.indeterminate).isFalse();
  }

  @Test
  public void build_setsStyleOnNotification() throws Exception {
    Style style = new Style() {
      @Override
      public Notification build() {
        return new Notification();
      }
    };
    builder.setStyle(style);
    build();
    assertThat(s.getStyle()).isSameAs(style);
  }

  @Test
  public void build_setsUsesChronometerOnNotification_true() throws Exception {
    builder.setUsesChronometer(true);
    build();
    assertThat(s.usesChronometer()).isTrue();
  }

  @Test
  public void build_setsUsesChronometerOnNotification_false() throws Exception {
    builder.setUsesChronometer(false);
    build();
    assertThat(s.usesChronometer()).isFalse();
  }

  @Test
  public void build_handlesNullContentTitle() {
    builder.setContentTitle(null);
    build();
    assertThat(s.getContentTitle()).isNull();
  }

  @Test
  public void build_handlesNullContentText() {
    builder.setContentText(null);
    build();
    assertThat(s.getContentText()).isNull();
  }

  @Test
  public void build_handlesNullTicker() {
    builder.setTicker(null);
    build();
    assertThat(s.getTicker()).isNull();
  }

  @Test
  public void build_handlesNullContentInfo() {
    builder.setContentInfo(null);
    build();
    assertThat(s.getContentInfo()).isNull();
  }

  @Test
  public void build_handlesNullStyle() {
    builder.setStyle(null);
    build();
    assertThat(s.getStyle()).isNull();
  }

  @Test
  public void build_addsActionToNotification() throws Exception {
    PendingIntent action = PendingIntent.getBroadcast(application, 0, null, 0);
    builder.addAction(0, "Action", action);
    build();
    assertThat(s.getActions().get(0).actionIntent)
        .isEqualsToByComparingFields(action);
  }

  private void build() {
    notification = builder.build();
    s = shadowOf(notification);
  }
}
