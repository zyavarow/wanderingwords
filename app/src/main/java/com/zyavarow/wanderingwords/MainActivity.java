package com.zyavarow.wanderingwords;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Simple settings screen: today's words, notification time, and links. */
public class MainActivity extends Activity {
    private static final int INK = Color.parseColor("#E5EBE7");
    private static final int MUTED = Color.parseColor("#97A6A0");
    private static final int MOSS = Color.parseColor("#86C3AD");
    private static final int BG = Color.parseColor("#111618");

    private TextView timeLabel;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        Scheduler.scheduleAll(this);
        WordWidget.refreshAll(this);
        setContentView(buildUi());
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private TextView text(String s, float sp, int color, Typeface tf) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (tf != null) t.setTypeface(tf);
        t.setLineSpacing(0, 1.15f);
        return t;
    }

    private TextView label(String s) {
        TextView t = text(s.toUpperCase(Locale.ROOT), 11, MOSS, Typeface.DEFAULT_BOLD);
        t.setLetterSpacing(0.14f);
        return t;
    }

    private LinearLayout.LayoutParams gapTop(int d) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(d);
        return lp;
    }

    private View buildUi() {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(22), dp(36), dp(22), dp(36));
        sv.addView(col);

        LocalDate today = LocalDate.now();
        col.addView(text(today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), 24, INK, Typeface.SERIF));

        List<Words.Word> ws = Words.forDate(this, today);
        if (Words.isReviewDay(today)) {
            col.addView(text("Sunday is review day. Recall this past week's words on your word page, then enjoy today's new ones.", 15, MUTED, null), gapTop(16));
        } else if (ws.isEmpty()) {
            col.addView(text("You've finished all the words in this list.", 15, MUTED, null), gapTop(16));
        }
        for (Words.Word w : ws) {
            col.addView(label(w.theme), gapTop(26));
            col.addView(text(w.word, 34, INK, Typeface.create(Typeface.SERIF, Typeface.NORMAL)), gapTop(2));
            col.addView(text(w.pron + "  ·  " + w.pos, 13, MUTED, Typeface.MONOSPACE), gapTop(2));
            col.addView(text(w.def, 16, INK, null), gapTop(8));
            col.addView(text("“" + w.example + "”", 17, MUTED, Typeface.create(Typeface.SERIF, Typeface.ITALIC)), gapTop(8));
        }

        List<Words.Word> extras = Words.bonusOn(this, today);
        if (!extras.isEmpty()) {
            col.addView(text("Extra words today", 13, MUTED, Typeface.DEFAULT_BOLD), gapTop(32));
        }
        for (Words.Word w : extras) {
            col.addView(label(w.theme), gapTop(18));
            col.addView(text(w.word, 30, INK, Typeface.create(Typeface.SERIF, Typeface.NORMAL)), gapTop(2));
            col.addView(text(w.pron + "  ·  " + w.pos, 13, MUTED, Typeface.MONOSPACE), gapTop(2));
            col.addView(text(w.def, 16, INK, null), gapTop(8));
            col.addView(text("\u201C" + w.example + "\u201D", 17, MUTED, Typeface.create(Typeface.SERIF, Typeface.ITALIC)), gapTop(8));
        }
        if (!extras.isEmpty()) {
            Button write = new Button(this);
            write.setText("Write sentences for extra words");
            write.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Words.LEXICON_URL + "#bonus" + Words.bonusTaken(this)))));
            col.addView(write, gapTop(10));
        }

        Button more = new Button(this);
        int left = Words.bonus(this).size() - Words.bonusTaken(this);
        more.setText(left > 0 ? "+ One more word" : "No extra words left");
        more.setEnabled(left > 0);
        more.setOnClickListener(v -> {
            if (Words.takeBonus(this) != null) {
                WordWidget.refreshAll(this);
                setContentView(buildUi());
            }
        });
        col.addView(more, gapTop(24));

        col.addView(label("Morning notification"), gapTop(40));
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        timeLabel = text("", 18, INK, null);
        row.addView(timeLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        Switch sw = new Switch(this);
        sw.setChecked(Scheduler.notifyOn(this));
        sw.setOnCheckedChangeListener((v, on) -> {
            Scheduler.prefs(this).edit().putBoolean("notify", on).apply();
            Scheduler.scheduleNotification(this);
            updateTimeLabel();
        });
        row.addView(sw);
        col.addView(row, gapTop(8));
        updateTimeLabel();

        Button change = new Button(this);
        change.setText("Change time");
        change.setOnClickListener(v -> new TimePickerDialog(this, (tp, h, m) -> {
            Scheduler.prefs(this).edit().putInt("hour", h).putInt("minute", m).apply();
            Scheduler.scheduleNotification(this);
            updateTimeLabel();
        }, Scheduler.notifyHour(this), Scheduler.notifyMinute(this), false).show());
        col.addView(change, gapTop(8));

        Button test = new Button(this);
        test.setText("Send a test notification");
        test.setOnClickListener(v -> {
            AlarmReceiver.showNotification(this);
            Toast.makeText(this, "Sent. Check your notifications.", Toast.LENGTH_SHORT).show();
        });
        col.addView(test, gapTop(8));

        col.addView(label("Your sentences and reviews"), gapTop(32));
        Button open = new Button(this);
        open.setText("Open sentences and weekly review");
        open.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Words.LEXICON_URL))));
        col.addView(open, gapTop(8));

        col.addView(label("Add the widget"), gapTop(32));
        col.addView(text("Long-press your home screen, tap Widgets, find Wandering Words, and drag it into place. It updates on its own just after midnight.", 14, MUTED, null), gapTop(8));
        return sv;
    }

    private void updateTimeLabel() {
        if (!Scheduler.notifyOn(this)) { timeLabel.setText("Off"); return; }
        LocalTime t = LocalTime.of(Scheduler.notifyHour(this), Scheduler.notifyMinute(this));
        timeLabel.setText("Every day at " + t.format(DateTimeFormatter.ofPattern("h:mm a")));
    }
}
