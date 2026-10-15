package dev.oneuiproject.oneuiexample.fragment;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sec.sesl.tester.R;

import dev.oneuiproject.oneuiexample.base.BaseFragment;
import dev.oneuiproject.oneuiexample.notification.NowBarNotifications;
import dev.oneuiproject.oneuiexample.notification.NowBarTimerService;

public class NowBarFragment extends BaseFragment {
    private static final int NOTIFICATION_PERMISSION_REQUEST = 901;
    private static final String TAG = "NowBarTest";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView status;
    private int pendingAction;
    private final Runnable refresh = new Runnable() {
        @Override public void run() {
            updateStatus();
            handler.postDelayed(this, 1000);
        }
    };

    @Override public int getLayoutResId() { return R.layout.sample3_fragment_now_bar; }
    @Override public int getIconResId() { return R.drawable.ic_now_bar_notification; }
    @Override public CharSequence getTitle() { return "Now Bar"; }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        status = view.findViewById(R.id.now_bar_status);
        NowBarNotifications.ensureChannel(requireContext());
        view.findViewById(R.id.now_bar_normal).setOnClickListener(v -> perform(1));
        view.findViewById(R.id.now_bar_timer).setOnClickListener(v -> perform(2));
        updateStatus();
    }

    @Override public void onResume() {
        super.onResume();
        handler.removeCallbacks(refresh);
        handler.post(refresh);
    }

    @Override public void onPause() {
        handler.removeCallbacks(refresh);
        super.onPause();
    }

    @Override public void onDestroyView() {
        status = null;
        super.onDestroyView();
    }

    private void perform(int action) {
        boolean stopping = action == 1
                ? NowBarNotifications.isPosted(requireContext(), NowBarNotifications.NORMAL_ID)
                : timerRemaining() > 0;
        if (!stopping && Build.VERSION.SDK_INT >= 33
                && requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            pendingAction = action;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST);
            return;
        }
        if (action == 1) toggleNormal(); else toggleTimer();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                       @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != NOTIFICATION_PERMISSION_REQUEST) return;
        int action = pendingAction;
        pendingAction = 0;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            perform(action);
        else updateStatus();
    }

    private void toggleNormal() {
        NotificationManager manager = NowBarNotifications.manager(requireContext());
        if (manager == null) return;
        if (NowBarNotifications.isPosted(requireContext(), NowBarNotifications.NORMAL_ID)) {
            manager.cancel(NowBarNotifications.NORMAL_ID);
            Log.d(TAG, "Normal notification cancelled");
        } else if (NowBarNotifications.canNotify(requireContext())) {
            manager.notify(NowBarNotifications.NORMAL_ID, NowBarNotifications.normal(requireContext()));
            Log.d(TAG, "Posting normal Live Update; promoted allowed: "
                    + NowBarNotifications.canPromote(requireContext()));
        } else {
            NowBarNotifications.openSettings(requireContext());
        }
        updateStatus();
    }

    private void toggleTimer() {
        Intent intent = new Intent(requireContext(), NowBarTimerService.class);
        if (timerRemaining() > 0) {
            intent.setAction(NowBarTimerService.ACTION_CANCEL);
            requireContext().startService(intent);
        } else if (NowBarNotifications.canNotify(requireContext())) {
            intent.setAction(NowBarTimerService.ACTION_START);
            try {
                if (Build.VERSION.SDK_INT >= 26) requireContext().startForegroundService(intent);
                else requireContext().startService(intent);
            } catch (RuntimeException error) {
                Log.e(TAG, "Unable to start timer", error);
                status.setText("Timer could not start. Check notification settings.");
            }
        } else {
            NowBarNotifications.openSettings(requireContext());
        }
        handler.postDelayed(this::updateStatus, 250);
    }

    private int timerRemaining() {
        long end = requireContext().getSharedPreferences("now_bar", 0)
                .getLong("timer_end_elapsed", 0);
        if (end <= 0) return 0;
        return Math.max(0, (int) ((end - SystemClock.elapsedRealtime() + 999) / 1000));
    }

    private void updateStatus() {
        if (status == null || !isAdded()) return;
        boolean normal = NowBarNotifications.isPosted(requireContext(), NowBarNotifications.NORMAL_ID);
        int remaining = timerRemaining();
        String permission = NowBarNotifications.canNotify(requireContext()) ? "Granted" : "Blocked";
        String api = Build.VERSION.SDK_INT >= 36 ? "Supported" : "Unavailable (standard notification fallback)";
        String promoted = Build.VERSION.SDK_INT < 36 ? "Unavailable"
                : NowBarNotifications.canPromote(requireContext()) ? "Allowed" : "Disabled for this app";
        String normalState = normal ? "Active" : "Inactive";
        if (normal && Build.VERSION.SDK_INT >= 36)
            normalState += NowBarNotifications.isPromoted(requireContext(), NowBarNotifications.NORMAL_ID)
                    ? " · system promoted" : " · promotion not confirmed";
        String timerState = remaining > 0 ? remaining + " seconds remaining"
                : NowBarNotifications.isPosted(requireContext(), NowBarNotifications.TIMER_ID)
                ? "Complete" : "Inactive";
        if (remaining > 0 && Build.VERSION.SDK_INT >= 36)
            timerState += NowBarNotifications.isPromoted(requireContext(), NowBarNotifications.TIMER_ID)
                    ? " · system promoted" : " · promotion not confirmed";
        status.setText("Notification permission: " + permission
                + "\nLive Updates API: " + api
                + "\nPromoted notifications: " + promoted
                + "\nNormal test: " + normalState
                + "\nTimer: " + timerState
                + "\n\nTap an active test again to stop it. Samsung controls Now Bar display."
                + (Build.VERSION.SDK_INT >= 36 && !NowBarNotifications.canPromote(requireContext())
                    ? "\nEnable Live Updates for this app in notification settings." : ""));
    }
}
