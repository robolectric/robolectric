package com.xtremelabs.robolectric.shadows;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Notification.class)
public class ShadowNotification {

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
}
