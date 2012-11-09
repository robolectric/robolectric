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
	private CharSequence contentTitle;
	private CharSequence contentText;
	private CharSequence contentInfo;
	private boolean isAutoCancel;
	private boolean ongoing;
	private boolean onlyAlertOnce;
	private int progressMax;
	private int progress;
	private boolean progressIndeterminate;
    @RealObject Notification realNotification;
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

	public Notification getRealNotification() {
		return realNotification;
	}

	public CharSequence getContentTitle() {
		return contentTitle;
	}

	public void setContentTitle(CharSequence contentTitle) {
		this.contentTitle = contentTitle;
	}

	public CharSequence getContentText() {
		return contentText;
	}

	public void setContentText(CharSequence contentText) {
		this.contentText = contentText;
	}

	public CharSequence getContentInfo() {
		return contentInfo;
	}

	public void setContentInfo(CharSequence contentInfo) {
		this.contentInfo = contentInfo;
	}

	public boolean isAutoCancel() {
		return isAutoCancel;
	}

	public void setAutoCancel(boolean autoCancel) {
		isAutoCancel = autoCancel;
	}

	public boolean isOngoing() {
		return ongoing;
	}

	public void setOngoing(boolean ongoing) {
		this.ongoing = ongoing;
	}

	public boolean isOnlyAlertOnce() {
		return onlyAlertOnce;
	}

	public void setOnlyAlertOnce(boolean onlyAlertOnce) {
		this.onlyAlertOnce = onlyAlertOnce;
	}

	public int getProgressMax() {
		return progressMax;
	}

	public void setProgressMax(int progressMax) {
		this.progressMax = progressMax;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public boolean isProgressIndeterminate() {
		return progressIndeterminate;
	}

	public void setProgressIndeterminate(boolean progressIndeterminate) {
		this.progressIndeterminate = progressIndeterminate;
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
