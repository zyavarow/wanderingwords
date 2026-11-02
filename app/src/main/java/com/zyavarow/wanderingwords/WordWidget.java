package com.zyavarow.wanderingwords;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.util.TypedValue;
import android.widget.RemoteViews;
import java.time.LocalDate;
import java.util.List;

/** Home-screen widget: today's two words in the Night Glass style. */
public class WordWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context c, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, build(c));
        Scheduler.scheduleAll(c);
    }

    public static final String ACTION_MORE = "com.zyavarow.wanderingwords.MORE";

    @Override
    public void onReceive(Context c, Intent intent) {
        if (ACTION_MORE.equals(intent.getAction())) {
            Words.takeBonus(c);
            refreshAll(c);
            return;
        }
        super.onReceive(c, intent);
    }

    @Override
    public void onEnabled(Context c) {
        Scheduler.scheduleAll(c);
    }

    public static void refreshAll(Context c) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(c);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(c, WordWidget.class));
        if (ids.length == 0) return;
        RemoteViews v = build(c);
        for (int id : ids) mgr.updateAppWidget(id, v);
    }

    /** Compact mode: smaller type and tighter spacing so three words fit without resizing. */
    static void applySizes(Context c, RemoteViews v, boolean compact) {
        float word = compact ? 18f : 25f, theme = compact ? 8.5f : 9.5f, def = compact ? 11.5f : 12.5f;
        int defLines = compact ? 1 : 2;
        int gap = dp(c, compact ? 6 : 12), pad = dp(c, compact ? 10 : 14);
        int[] words = {R.id.word1, R.id.word2, R.id.word3};
        int[] themes = {R.id.theme1, R.id.theme2, R.id.theme3};
        int[] defs = {R.id.def1, R.id.def2, R.id.def3};
        for (int i = 0; i < 3; i++) {
            v.setTextViewTextSize(words[i], TypedValue.COMPLEX_UNIT_SP, word);
            v.setTextViewTextSize(themes[i], TypedValue.COMPLEX_UNIT_SP, theme);
            v.setTextViewTextSize(defs[i], TypedValue.COMPLEX_UNIT_SP, def);
            v.setInt(defs[i], "setMaxLines", defLines);
        }
        v.setViewPadding(R.id.block2, 0, gap, 0, 0);
        v.setViewPadding(R.id.block3, 0, gap, 0, 0);
        v.setViewPadding(R.id.root, dp(c, 18), pad, dp(c, 18), pad);
    }

    private static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static RemoteViews build(Context c) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        LocalDate today = LocalDate.now();
        List<Words.Word> ws = Words.forDate(c, today);

        if (ws.isEmpty()) {
            v.setTextViewText(R.id.theme1, "WANDERING WORDS");
            v.setTextViewText(R.id.word1, "All words complete");
            v.setTextViewText(R.id.def1, "Tap to revisit your library");
            v.setViewVisibility(R.id.block2, View.GONE);
        } else {
            Words.Word a = ws.get(0);
            String t1 = a.theme.toUpperCase();
            if (Words.isReviewDay(today)) t1 = "REVIEW DAY  ·  " + t1;
            v.setTextViewText(R.id.theme1, t1);
            v.setTextViewText(R.id.word1, a.word);
            v.setTextViewText(R.id.def1, a.def);
            if (ws.size() > 1) {
                Words.Word b = ws.get(1);
                v.setViewVisibility(R.id.block2, View.VISIBLE);
                v.setTextViewText(R.id.theme2, b.theme.toUpperCase());
                v.setTextViewText(R.id.word2, b.word);
                v.setTextViewText(R.id.def2, b.def);
            } else {
                v.setViewVisibility(R.id.block2, View.GONE);
            }
        }

        // Latest extra word taken today, shown in amber below the daily pair.
        List<Words.Word> extras = Words.bonusOn(c, today);
        if (!extras.isEmpty()) {
            Words.Word x = extras.get(extras.size() - 1);
            String label = extras.size() > 1 ? "EXTRA " + extras.size() + "  ·  " : "EXTRA  ·  ";
            v.setViewVisibility(R.id.block3, View.VISIBLE);
            v.setTextViewText(R.id.theme3, label + x.theme.toUpperCase());
            v.setTextViewText(R.id.word3, x.word);
            v.setTextViewText(R.id.def3, x.def);
        } else {
            v.setViewVisibility(R.id.block3, View.GONE);
        }

        applySizes(c, v, !extras.isEmpty());

        Intent more = new Intent(c, WordWidget.class).setAction(ACTION_MORE);
        PendingIntent morePi = PendingIntent.getBroadcast(c, 3, more,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.more, morePi);

        // Tapping the widget opens the word page; on days with extra words it also records them there.
        String hash = !extras.isEmpty() ? "#bonus" + Words.bonusTaken(c)
                : (Words.isReviewDay(today) ? "#review" : "");
        String url = Words.LEXICON_URL + hash;
        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(c, 1, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.root, pi);
        return v;
    }
}
