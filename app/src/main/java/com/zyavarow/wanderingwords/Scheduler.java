package com.zyavarow.wanderingwords;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/** Schedules the morning notification and the just-after-midnight widget refresh. */
public final class Scheduler {
    public static final String ACTION_NOTIFY = "com.zyavarow.wanderingwords.NOTIFY";
    public static final String ACTION_REFRESH = "com.zyavarow.wanderingwords.REFRESH";
    private static final String PREFS = "wanderingwords";

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    public static int notifyHour(Context c) { return prefs(c).getInt("hour", 7); }
    public static int notifyMinute(Context c) { return prefs(c).getInt("minute", 0); }
    public static boolean notifyOn(Context c) { return prefs(c).getBoolean("notify", true); }

    public static void scheduleAll(Context c) {
        scheduleNotification(c);
        scheduleRefresh(c);
    }

    private static PendingIntent pending(Context c, String action, int code) {
        Intent i = new Intent(c, AlarmReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(c, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static long millis(LocalDateTime t) {
        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static void scheduleNotification(Context c) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        PendingIntent pi = pending(c, ACTION_NOTIFY, 10);
        am.cancel(pi);
        if (!notifyOn(c)) return;
        LocalDateTime next = LocalDate.now().atTime(LocalTime.of(notifyHour(c), notifyMinute(c)));
        if (!next.isAfter(LocalDateTime.now())) next = next.plusDays(1);
        // A 5-minute window lets Android batch the alarm without needing exact-alarm permission.
        am.setWindow(AlarmManager.RTC_WAKEUP, millis(next), 5 * 60 * 1000L, pi);
    }

    public static void scheduleRefresh(Context c) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        PendingIntent pi = pending(c, ACTION_REFRESH, 11);
        am.cancel(pi);
        LocalDateTime next = LocalDate.now().plusDays(1).atTime(0, 1);
        am.setWindow(AlarmManager.RTC, millis(next), 10 * 60 * 1000L, pi);
    }
}
