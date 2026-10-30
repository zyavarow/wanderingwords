package com.zyavarow.wanderingwords;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
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

    static RemoteViews build(Context c) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        LocalDate today = LocalDate.now();
        List<Words.Word> ws = Words.forDate(c, today);

        if (Words.isReviewDay(today)) {
            v.setTextViewText(R.id.theme1, "SUNDAY");
            v.setTextViewText(R.id.word1, "Review day");
            v.setTextViewText(R.id.def1, "Tap to recall this week's words");
            v.setViewVisibility(R.id.block2, View.GONE);
        } else if (ws.isEmpty()) {
            v.setTextViewText(R.id.theme1, "WANDERING WORDS");
            v.setTextViewText(R.id.word1, "All words complete");
            v.setTextViewText(R.id.def1, "Tap to revisit your library");
            v.setViewVisibility(R.id.block2, View.GONE);
        } else {
            Words.Word a = ws.get(0);
            v.setTextViewText(R.id.theme1, a.theme.toUpperCase());
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

        String url = Words.LEXICON_URL + (Words.isReviewDay(today) ? "#review" : "");
        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(c, 1, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.root, pi);
        return v;
    }
}
