package com.zyavarow.wanderingwords;

import android.content.Context;
import org.json.JSONArray;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** The bundled word list and the day-by-day schedule (2 words Mon–Sat, Sunday is review). */
public final class Words {
    public static final LocalDate START = LocalDate.of(2026, 10, 28);
    public static final int PER_DAY = 2;
    public static final String LEXICON_URL = "https://claude.ai/artifact/1EqQ5R7SUWft5zLDoopNLp";

    public static final class Word {
        public final String word, pron, pos, def, example, theme;
        Word(JSONArray a) throws Exception {
            word = a.getString(0); pron = a.getString(1); pos = a.getString(2);
            def = a.getString(3); example = a.getString(4); theme = a.getString(5);
        }
    }

    private static List<Word> cache, bonusCache;

    private static List<Word> load(Context c, String file) {
        List<Word> list = new ArrayList<>();
        try (InputStream in = c.getAssets().open(file)) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[16384];
            int n;
            while ((n = in.read(chunk)) != -1) out.write(chunk, 0, n);
            JSONArray arr = new JSONArray(new String(out.toByteArray(), StandardCharsets.UTF_8));
            for (int i = 0; i < arr.length(); i++) list.add(new Word(arr.getJSONArray(i)));
        } catch (Exception e) {
            // leave list empty; UI shows a fallback message
        }
        return list;
    }

    public static synchronized List<Word> all(Context c) {
        if (cache == null) cache = load(c, "words.json");
        return cache;
    }

    public static synchronized List<Word> bonus(Context c) {
        if (bonusCache == null) bonusCache = load(c, "bonus.json");
        return bonusCache;
    }

    /* ---------- extra words ("One more word") ---------- */
    // Stored as "index:yyyy-mm-dd" entries separated by ";", in the order they were taken.

    public static int bonusTaken(Context c) {
        String raw = Scheduler.prefs(c).getString("bonus", "");
        return raw.isEmpty() ? 0 : raw.split(";").length;
    }

    public static List<Word> bonusOn(Context c, LocalDate date) {
        List<Word> out = new ArrayList<>();
        String raw = Scheduler.prefs(c).getString("bonus", "");
        if (raw.isEmpty()) return out;
        List<Word> pool = bonus(c);
        for (String e : raw.split(";")) {
            String[] kv = e.split(":");
            if (kv.length == 2 && kv[1].equals(date.toString())) {
                int i = Integer.parseInt(kv[0]);
                if (i < pool.size()) out.add(pool.get(i));
            }
        }
        return out;
    }

    /** Takes the next extra word, or returns null when the collection is used up. */
    public static Word takeBonus(Context c) {
        List<Word> pool = bonus(c);
        int n = bonusTaken(c);
        if (n >= pool.size()) return null;
        String raw = Scheduler.prefs(c).getString("bonus", "");
        String entry = n + ":" + LocalDate.now();
        Scheduler.prefs(c).edit().putString("bonus", raw.isEmpty() ? entry : raw + ";" + entry).apply();
        return pool.get(n);
    }

    /** Days from START up to, but not including, date. Two new words every day. */
    static long slotIndex(LocalDate date) {
        return ChronoUnit.DAYS.between(START, date);
    }

    public static boolean isReviewDay(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /** Today's words: empty before the start date or after the list ends. Sundays get words too. */
    public static List<Word> forDate(Context c, LocalDate date) {
        List<Word> out = new ArrayList<>();
        if (date.isBefore(START)) return out;
        List<Word> all = all(c);
        long s = slotIndex(date) * PER_DAY;
        for (long i = s; i < s + PER_DAY && i < all.size(); i++) out.add(all.get((int) i));
        return out;
    }
}
