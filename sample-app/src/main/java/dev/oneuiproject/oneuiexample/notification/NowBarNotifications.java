package dev.oneuiproject.oneuiexample.notification;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.sec.sesl.tester.R;

import dev.oneuiproject.oneuiexample.activity.MainActivity;

public final class NowBarNotifications {
    public static final String CHANNEL_ID = "now_bar_live_updates";
    public static final int NORMAL_ID = 9001;
    public static final int TIMER_ID = 9002;
    private static final String TAG = "NowBarTest";
    // Public Notification.EXTRA_REQUEST_PROMOTED_ONGOING value (introduced in API 36.1).
    // Using the documented extra also works when compiling against the base API 36 SDK.
    private static final String REQUEST_PROMOTION = "android.requestPromotedOngoing";

    private NowBarNotifications() { }

    public static NotificationManager manager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager manager = manager(context);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Live updates",
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("One UI Now Bar notification tests");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setSound(null, null);
        channel.enableVibration(false);
        manager.createNotificationChannel(channel);
    }

    public static boolean canNotify(Context context) {
        NotificationManager manager = manager(context);
        return manager != null && (Build.VERSION.SDK_INT < 24 || manager.areNotificationsEnabled())
                && (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                && (Build.VERSION.SDK_INT < 26 || manager.getNotificationChannel(CHANNEL_ID) == null
                || manager.getNotificationChannel(CHANNEL_ID).getImportance() != NotificationManager.IMPORTANCE_NONE);
    }

    public static boolean canPromote(Context context) {
        NotificationManager manager = manager(context);
        return Build.VERSION.SDK_INT >= 36 && manager != null
                && manager.canPostPromotedNotifications();
    }

    public static void openSettings(Context context) {
        String[] actions = Build.VERSION.SDK_INT >= 36
                ? new String[]{Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS,
                Settings.ACTION_APP_NOTIFICATION_SETTINGS, Settings.ACTION_APPLICATION_DETAILS_SETTINGS}
                : Build.VERSION.SDK_INT >= 26
                ? new String[]{Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS}
                : new String[]{Settings.ACTION_APPLICATION_DETAILS_SETTINGS};
        for (String action : actions) {
            Intent intent = new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Settings.ACTION_APPLICATION_DETAILS_SETTINGS.equals(action)) {
                intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
            } else if (Build.VERSION.SDK_INT >= 26) {
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            }
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return;
            }
        }
    }

    public static Notification.Builder builder(Context context, String title, String body) {
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID) : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_now_bar_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class)
                                .putExtra("open_now_bar", true)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true);
        if (Build.VERSION.SDK_INT >= 36) {
            Bundle extras = new Bundle();
            extras.putBoolean(REQUEST_PROMOTION, true);
            builder.addExtras(extras);
        }
        return builder;
    }

    public static Notification normal(Context context) {
        Notification notification = builder(context, "One UI Live Update",
                "Now Bar notification is active")
                .setSubText("One UI 9 Test")
                .setOngoing(true)
                .setShowWhen(false)
                .build();
        logPromotability(notification);
        return notification;
    }

    public static Notification timer(Context context, int elapsed, int remaining, long endWallTime,
                                     boolean complete) {
        Notification.Builder builder = builder(context, complete ? "Timer Complete" : "Timer Running",
                complete ? "60-second test finished" : remaining + " seconds remaining")
                .setSubText("One UI 9 Timer")
                .setOngoing(!complete)
                .setWhen(endWallTime)
                .setShowWhen(!complete)
                .setUsesChronometer(!complete);
        if (Build.VERSION.SDK_INT >= 24) builder.setChronometerCountDown(!complete);
        if (Build.VERSION.SDK_INT >= 36) {
            Notification.ProgressStyle style = new Notification.ProgressStyle()
                    .setProgress(Math.min(100, elapsed * 100 / 60));
            builder.setStyle(style);
            builder.setShortCriticalText(complete ? "Done" : remaining + "s");
        } else {
            builder.setProgress(60, elapsed, false);
        }
        Notification notification = builder.build();
        if (elapsed == 0) logPromotability(notification);
        return notification;
    }

    public static boolean isPosted(Context context, int id) {
        NotificationManager manager = manager(context);
        if (manager == null) return false;
        for (android.service.notification.StatusBarNotification item : manager.getActiveNotifications()) {
            if (item.getId() == id) return true;
        }
        return false;
    }

    public static boolean isPromoted(Context context, int id) {
        NotificationManager manager = manager(context);
        if (manager == null || Build.VERSION.SDK_INT < 36) return false;
        for (android.service.notification.StatusBarNotification item : manager.getActiveNotifications()) {
            if (item.getId() == id)
                return (item.getNotification().flags & Notification.FLAG_PROMOTED_ONGOING) != 0;
        }
        return false;
    }

    private static void logPromotability(Notification notification) {
        if (Build.VERSION.SDK_INT >= 36)
            Log.d(TAG, "Promotable characteristics: " + notification.hasPromotableCharacteristics());
    }
}
