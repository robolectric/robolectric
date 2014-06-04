package org.robolectric.shadows;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import java.util.ArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;
import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Notification.class)
public class ShadowNotification {
  private static final int MAX_ACTIONS = 3;

  private CharSequence contentTitle;
  private CharSequence contentText;
  private CharSequence ticker;
  private CharSequence contentInfo;
  private int smallIcon;
  private long when;
  private ArrayList<Notification.Action> actions = new ArrayList<Notification.Action>(MAX_ACTIONS);

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

  public CharSequence getTicker() {
   return ticker;
  }

  public void setTicker(CharSequence ticker) {
   this.ticker = ticker;
  }

  public CharSequence getContentInfo() {
   return contentInfo;
  }

  public void setContentInfo(CharSequence contentInfo) {
   this.contentInfo = contentInfo;
  }

  public void setActions(ArrayList<Notification.Action> actions) {
    this.actions = actions;
  }

  public ArrayList<Notification.Action> getActions() {
    return actions;
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
    private CharSequence contentTitle;
    private CharSequence contentInfo;
    private CharSequence contentText;
    private CharSequence ticker;
    private int smallIcon;
    private long when;
    private ArrayList<Notification.Action> actions =
        new ArrayList<Notification.Action>(MAX_ACTIONS);

    @Implementation
    public Notification build() {
      Notification result = (Notification) directlyOn(realBuilder, Notification.Builder.class, "build").invoke();
      ShadowNotification shadowResult = shadowOf(result);
      shadowResult.setContentTitle(contentTitle);
      shadowResult.setContentText(contentText);
      shadowResult.setSmallIcon(smallIcon);
      shadowResult.setTicker(ticker);
      shadowResult.setWhen(when);
      shadowResult.setContentInfo(contentInfo);
      shadowResult.setActions(actions);
      return result;
    }

    @Implementation
    public Notification.Builder setContentTitle(CharSequence contentTitle) {
      this.contentTitle = contentTitle;
      directlyOn(realBuilder, Notification.Builder.class, "setContentTitle", CharSequence.class).invoke(contentTitle);
      return realBuilder;
    }

    @Implementation
    public Notification.Builder setContentText(CharSequence text) {
      this.contentText = text;
      directlyOn(realBuilder, Notification.Builder.class, "setContentText", CharSequence.class).invoke(text);
      return realBuilder;
    }

    @Implementation
    public Notification.Builder setSmallIcon(int smallIcon) {
      this.smallIcon = smallIcon;
      directlyOn(realBuilder, Notification.Builder.class, "setSmallIcon", int.class).invoke(smallIcon);
      return realBuilder;
    }

    @Implementation
    public Notification.Builder setWhen(long when) {
      this.when = when;
      directlyOn(realBuilder, Notification.Builder.class, "setWhen", long.class).invoke(when);
      return realBuilder;
    }

    @Implementation
    public Notification.Builder setTicker(CharSequence ticker) {
      this.ticker = ticker;
      directlyOn(realBuilder, Notification.Builder.class, "setTicker", CharSequence.class).invoke(ticker);
      return realBuilder;
    }

    @Implementation
    public Notification.Builder setContentInfo(CharSequence contentInfo) {
      this.contentInfo = contentInfo;
      directlyOn(realBuilder, Notification.Builder.class, "setContentInfo", CharSequence.class).invoke(contentInfo);
      return realBuilder;
    }

    @Implementation
    public Notification.Builder addAction(int icon, CharSequence title, PendingIntent intent) {
      this.actions.add(new Notification.Action(icon, title, intent));
      // TODO: Call addAction on real builder after resolving issue with RemoteViews bitmap cache.
      // directlyOn(realBuilder, Notification.Builder.class, "addAction", int.class,
      //     CharSequence.class, PendingIntent.class).invoke(icon, title, intent);
      return realBuilder;
    }
  }
}
