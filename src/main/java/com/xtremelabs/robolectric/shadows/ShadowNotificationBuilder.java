package com.xtremelabs.robolectric.shadows;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(Notification.Builder.class)
public class ShadowNotificationBuilder {
	@RealObject private Notification.Builder realNotificationBuilder;
	private Context context;
	private boolean autoCancel;
	private RemoteViews contentView;
	private CharSequence contentInfo;
	private PendingIntent contentIntent;
	private CharSequence contentText;
	private CharSequence contentTitle;
	private int defaults;
	private PendingIntent deleteIntent;
	private PendingIntent fullScreenIntent;
	private boolean highPriority;
	private Bitmap largeIcon;
	private int ledArgb;
	private int ledOnMs;
	private int ledOffMs;
	private int number;
	private boolean ongoing;
	private boolean onlyAlertOnce;
	private int progressMax;
	private int progress;
	private boolean progressIndeterminate;
	private int smallIcon;
	private int smallIconLevel;
	private Uri sound;
	private int audioStreamType;
	private CharSequence tickerText;
	private RemoteViews tickerViews;
	private long[] vibrate;
	private long when;
	private int flags;

	public void __constructor__(Context context) {
		this.context = context;
		when = System.currentTimeMillis();
		audioStreamType = Notification.STREAM_DEFAULT;
	}

	@Implementation
	public Notification build() {
		Notification n = new Notification();
		n.when = when;
		n.icon = smallIcon;
		n.iconLevel = smallIconLevel;
		n.number = number;
		n.contentView = contentView;
		n.contentIntent = contentIntent;
		n.deleteIntent = deleteIntent;
		n.fullScreenIntent = fullScreenIntent;
		n.tickerText = tickerText;
		n.tickerView = tickerViews;
		n.largeIcon = largeIcon;
		n.sound = sound;
		n.audioStreamType = audioStreamType;
		n.vibrate = vibrate;
		n.ledARGB = ledArgb;
		n.ledOnMS = ledOnMs;
		n.ledOffMS = ledOffMs;
		n.defaults = defaults;
		n.flags = flags;
		if (ledOnMs != 0 && ledOffMs != 0) {
			n.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
		if ((defaults & Notification.DEFAULT_LIGHTS) != 0) {
			n.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
		ShadowNotification shadowNotification = Robolectric.shadowOf(n);
		shadowNotification.setContentTitle(contentTitle);
		shadowNotification.setContentText(contentText);
		shadowNotification.setAutoCancel(autoCancel);
		shadowNotification.setContentInfo(contentInfo);
		shadowNotification.setOngoing(ongoing);
		shadowNotification.setOnlyAlertOnce(onlyAlertOnce);
		shadowNotification.setProgressMax(progressMax);
		shadowNotification.setProgress(progress);
		shadowNotification.setProgressIndeterminate(progressIndeterminate);
		return n;
	}

	@Implementation
	public Notification getNotification() {
		return build();
	}

	@Implementation
	public Notification.Builder setAutoCancel(boolean autoCancel) {
		this.autoCancel = autoCancel;
		setFlag(Notification.FLAG_AUTO_CANCEL, autoCancel);
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setContent(RemoteViews views) {
		this.contentView = views;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setContentInfo(CharSequence info) {
		this.contentInfo = info;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setContentIntent(PendingIntent intent) {
		this.contentIntent = intent;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setContentText(CharSequence text) {
		this.contentText = text;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setContentTitle(CharSequence title) {
		this.contentTitle = title;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setDefaults(int defaults) {
		this.defaults = defaults;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setDeleteIntent(PendingIntent intent) {
		this.deleteIntent = intent;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setFullScreenIntent(PendingIntent intent, boolean highPriority) {
		this.fullScreenIntent = intent;
		this.highPriority = highPriority;
		setFlag(Notification.FLAG_HIGH_PRIORITY, highPriority);
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setLargeIcon(Bitmap icon) {
		this.largeIcon = icon;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setLights(int argb, int onMs, int offMs) {
		this.ledArgb = argb;
		this.ledOnMs = onMs;
		this.ledOffMs = offMs;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setNumber(int number) {
		this.number = number;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setOngoing(boolean ongoing) {
		this.ongoing = ongoing;
		setFlag(Notification.FLAG_ONGOING_EVENT, ongoing);
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setOnlyAlertOnce(boolean onlyAlertOnce) {
		this.onlyAlertOnce = onlyAlertOnce;
		setFlag(Notification.FLAG_ONLY_ALERT_ONCE, onlyAlertOnce);
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setProgress(int max, int progress, boolean indeterminate) {
		this.progressMax = max;
		this.progress = progress;
		this.progressIndeterminate = indeterminate;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setSmallIcon(int icon, int level) {
		this.smallIcon = icon;
		this.smallIconLevel = level;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setSmallIcon(int icon) {
		this.smallIcon = icon;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setSound(Uri sound) {
		this.sound = sound;
		audioStreamType = Notification.STREAM_DEFAULT;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setSound(Uri sound, int streamType) {
		this.sound = sound;
		this.audioStreamType = streamType;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setTicker(CharSequence tickerText, RemoteViews views) {
		this.tickerText = tickerText;
		this.tickerViews = views;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setTicker(CharSequence tickerText) {
		this.tickerText = tickerText;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setVibrate(long[] pattern) {
		this.vibrate = pattern;
		return realNotificationBuilder;
	}

	@Implementation
	public Notification.Builder setWhen(long when) {
		this.when = when;
		return realNotificationBuilder;
	}

	public boolean isAutoCancel() {
		return autoCancel;
	}

	public RemoteViews getContentView() {
		return contentView;
	}

	public CharSequence getContentInfo() {
		return contentInfo;
	}

	public PendingIntent getContentIntent() {
		return contentIntent;
	}

	public CharSequence getContentText() {
		return contentText;
	}

	public CharSequence getContentTitle() {
		return contentTitle;
	}

	public int getDefaults() {
		return defaults;
	}

	public PendingIntent getDeleteIntent() {
		return deleteIntent;
	}

	public PendingIntent getFullScreenIntent() {
		return fullScreenIntent;
	}

	public boolean isHighPriority() {
		return highPriority;
	}

	public Bitmap getLargeIcon() {
		return largeIcon;
	}

	public int getLedArgb() {
		return ledArgb;
	}

	public int getLedOnMs() {
		return ledOnMs;
	}

	public int getLedOffMs() {
		return ledOffMs;
	}

	public int getNumber() {
		return number;
	}

	public boolean isOngoing() {
		return ongoing;
	}

	public boolean isOnlyAlertOnce() {
		return onlyAlertOnce;
	}

	public int getProgressMax() {
		return progressMax;
	}

	public int getProgress() {
		return progress;
	}

	public boolean isProgressIndeterminate() {
		return progressIndeterminate;
	}

	public int getSmallIcon() {
		return smallIcon;
	}

	public int getSmallIconLevel() {
		return smallIconLevel;
	}

	public Uri getSound() {
		return sound;
	}

	public int getAudioStreamType() {
		return audioStreamType;
	}

	public CharSequence getTickerText() {
		return tickerText;
	}

	public RemoteViews getTickerViews() {
		return tickerViews;
	}

	public long[] getVibrate() {
		return vibrate;
	}

	public long getWhen() {
		return when;
	}

	public int getFlags() {
		return flags;
	}

	private void setFlag(int mask, boolean value) {
		if (value) {
			flags |= mask;
		} else {
			flags &= ~mask;
		}
	}
}