package dev.oneuiproject.oneuiexample.notification;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

public class NowBarTimerService extends Service {
    public static final String ACTION_START = "dev.oneuiproject.oneuiexample.START_NOW_BAR_TIMER";
    public static final String ACTION_CANCEL = "dev.oneuiproject.oneuiexample.CANCEL_NOW_BAR_TIMER";
    private static final String TAG = "NowBarTest";
    private static final long DURATION_MS = 60_000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long endElapsedTime;
    private long endWallTime;
    private boolean running;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            long left = Math.max(0, endElapsedTime - SystemClock.elapsedRealtime());
            int remaining = (int) ((left + 999) / 1000);
            int elapsed = 60 - remaining;
            if (left == 0) {
                complete();
                return;
            }
            android.app.NotificationManager manager = NowBarNotifications.manager(NowBarTimerService.this);
            if (manager != null) manager.notify(NowBarNotifications.TIMER_ID,
                    NowBarNotifications.timer(NowBarTimerService.this, elapsed, remaining, endWallTime, false));
            Log.d(TAG, "Timer progress: " + elapsed + "/60");
            handler.postDelayed(this, 1000);
        }
    };

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_CANCEL.equals(intent.getAction())) {
            cancel();
            return START_NOT_STICKY;
        }
        if (running) return START_NOT_STICKY;
        if (!NowBarNotifications.canNotify(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        NowBarNotifications.ensureChannel(this);
        endElapsedTime = SystemClock.elapsedRealtime() + DURATION_MS;
        endWallTime = System.currentTimeMillis() + DURATION_MS;
        try {
            android.app.Notification notification = NowBarNotifications.timer(this, 0, 60, endWallTime, false);
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NowBarNotifications.TIMER_ID, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
            } else {
                startForeground(NowBarNotifications.TIMER_ID, notification);
            }
        } catch (RuntimeException error) {
            Log.e(TAG, "Unable to start timer foreground service", error);
            stopSelf();
            return START_NOT_STICKY;
        }
        running = true;
        getSharedPreferences("now_bar", MODE_PRIVATE).edit()
                .putLong("timer_end_elapsed", endElapsedTime).apply();
        Log.d(TAG, "Starting timer; promoted allowed: " + NowBarNotifications.canPromote(this));
        handler.postDelayed(tick, 1000);
        return START_NOT_STICKY;
    }

    private void complete() {
        running = false;
        handler.removeCallbacks(tick);
        getSharedPreferences("now_bar", MODE_PRIVATE).edit().remove("timer_end_elapsed").apply();
        removeForeground();
        android.app.NotificationManager manager = NowBarNotifications.manager(this);
        if (manager != null) manager.notify(NowBarNotifications.TIMER_ID,
                NowBarNotifications.timer(this, 60, 0, endWallTime, true));
        Log.d(TAG, "Timer completed");
        stopSelf();
    }

    private void cancel() {
        running = false;
        handler.removeCallbacks(tick);
        getSharedPreferences("now_bar", MODE_PRIVATE).edit().remove("timer_end_elapsed").apply();
        removeForeground();
        android.app.NotificationManager manager = NowBarNotifications.manager(this);
        if (manager != null) manager.cancel(NowBarNotifications.TIMER_ID);
        Log.d(TAG, "Timer cancelled");
        stopSelf();
    }

    @Override public void onDestroy() {
        handler.removeCallbacks(tick);
        if (running) cancel();
        super.onDestroy();
    }

    private void removeForeground() {
        if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE);
        else stopForeground(true);
    }
}
