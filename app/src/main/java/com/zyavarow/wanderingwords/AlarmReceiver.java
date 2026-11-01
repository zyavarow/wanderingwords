package com.zyavarow.wanderingwords;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.app.Notification;
import java.time.LocalDate;
import java.util.List;

/** Handles the morning notification, the midnight refresh, and rescheduling after reboot or time changes. */
public class AlarmReceiver extends BroadcastReceiver {
    static final String CHANNEL = "daily_words";

    @Override
    public void onReceive(Context c, Intent intent) {
        String a = intent.getAction();
        if (Scheduler.ACTION_NOTIFY.equals(a)) {
            showNotification(c);
        }
        WordWidget.refreshAll(c);
        Scheduler.scheduleAll(c);
    }

    static void ensureChannel(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL) == null) {
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Daily words",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Your two words each morning");
            nm.createNotificationChannel(ch);
        }
    }

    static void showNotification(Context c) {
        if (Build.VERSION.SDK_INT >= 33 &&
                c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ensureChannel(c);
        LocalDate today = LocalDate.now();
        String title, body;
        String url = Words.LEXICON_URL;
        List<Words.Word> ws = Words.forDate(c, today);
        if (ws.isEmpty()) return;
        StringBuilder t = new StringBuilder(), b = new StringBuilder();
        for (Words.Word w : ws) {
            if (t.length() > 0) { t.append(" · "); b.append("\n\n"); }
            t.append(w.word);
            b.append(w.word).append(" (").append(w.pos).append("): ").append(w.def)
             .append("\n\u201C").append(w.example).append("\u201D");
        }
        if (Words.isReviewDay(today)) {
            b.append("\n\nIt's Sunday review day too. Tap to recall this past week's words.");
            url += "#review";
        }
        title = t.toString();
        body = b.toString();
        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PendingIntent pi = PendingIntent.getActivity(c, 2, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(c, CHANNEL)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(title)
                .setContentText(body.split("\n")[0])
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        c.getSystemService(NotificationManager.class).notify(1, n);
    }
}
