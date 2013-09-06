package org.robolectric.shadows;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;
import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Notification.class)
public class ShadowNotification {
  private CharSequence contentTitle;
  private CharSequence contentText;
  private int smallIcon;
  private long when;

  public Notification getRealNotification() {
    return realNotification;
  }

  @RealObject
  Notification realNotification;

  private LatestEventInfo latestEventInfo;

  public void __constructor__(int icon, CharSequence tickerText, long when) {
    realNotification.icon = icon;
    realNotification.tickerText = tickerText;
    realNotification.when = when;
  }

  public CharSequence getContentTitle() {
    return contentTitle;
  }

  public CharSequence getContentText() {
    return contentText;
  }

  public int getSmallIcon() {
    return smallIcon;
  }

  public long getWhen() {
    return when;
  }

  public void setContentTitle(CharSequence contentTitle) {
    this.contentTitle = contentTitle;
  }

  public void setContentText(CharSequence contentText) {
    this.contentText = contentText;
  }

  public void setSmallIcon(int icon) {
    this.smallIcon = icon;
  }

  public void setWhen(long when) {
    this.when = when;
  }

  @Implementation
  public void setLatestEventInfo(Context context, CharSequence contentTitle,
                   CharSequence contentText, PendingIntent contentIntent) {
    latestEventInfo = new LatestEventInfo(contentTitle, contentText, contentIntent);
    realNotification.contentIntent = contentIntent;
  }

  public LatestEventInfo getLatestEventInfo() {
    return latestEventInfo;
  }

  public static class LatestEventInfo {
    private final CharSequence contentTitle;
    private final CharSequence contentText;
    private final PendingIntent contentIntent;

    private LatestEventInfo(CharSequence contentTitle, CharSequence contentText, PendingIntent contentIntent) {
      this.contentTitle = contentTitle;
      this.contentText = contentText;
      this.contentIntent = contentIntent;
    }

    public CharSequence getContentTitle() {
      return contentTitle;
    }

    public CharSequence getContentText() {
      return contentText;
    }

    public PendingIntent getContentIntent() {
      return contentIntent;
    }
  }

  @Implements(Notification.Builder.class)
  public static class ShadowBuilder {

    @RealObject private Notification.Builder realBuilder;
    private String contentTitle;
    private String contentText;
    private int icon;
    private long when;

    @Implementation
    public Notification build() {
      Notification result = (Notification) directlyOn(realBuilder, Notification.Builder.class, "build").invoke();
      ShadowNotification shadowResult = shadowOf(result);
      shadowResult.setContentTitle(contentTitle);
      shadowResult.setContentText(contentText);
      shadowResult.setSmallIcon(icon);
      shadowResult.setWhen(when);
      return result;
    }

    @Implementation
    public Notification.Builder setContentTitle(CharSequence title) {
      contentTitle = title.toString();
      directlyOn(realBuilder, Notification.Builder.class, "setContentTitle", CharSequence.class).invoke(title);

      return realBuilder;
    }

    @Implementation
    public Notification.Builder setContentText(CharSequence text) {
      contentText = text.toString();
      directlyOn(realBuilder, Notification.Builder.class, "setContentText", CharSequence.class).invoke(text);

      return realBuilder;
    }

    @Implementation
    public Notification.Builder setSmallIcon(int smallIcon) {
      this.icon = smallIcon;
      directlyOn(realBuilder, Notification.Builder.class, "setSmallIcon", int.class).invoke(smallIcon);

      return realBuilder;
    }

    @Implementation
    public Notification.Builder setWhen(long when) {
      this.when = when;
      directlyOn(realBuilder, Notification.Builder.class, "setWhen", long.class).invoke(when);

      return realBuilder;
    }
  }
}
