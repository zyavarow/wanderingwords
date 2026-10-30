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

    private static List<Word> cache;

    public static synchronized List<Word> all(Context c) {
        if (cache != null) return cache;
        List<Word> list = new ArrayList<>();
        try (InputStream in = c.getAssets().open("words.json")) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[16384];
            int n;
            while ((n = in.read(chunk)) != -1) out.write(chunk, 0, n);
            JSONArray arr = new JSONArray(new String(out.toByteArray(), StandardCharsets.UTF_8));
            for (int i = 0; i < arr.length(); i++) list.add(new Word(arr.getJSONArray(i)));
        } catch (Exception e) {
            // leave list empty; UI shows a fallback message
        }
        cache = list;
        return list;
    }

    /** Number of word-days (Mon–Sat) from START up to, but not including, date. */
    static long slotIndex(LocalDate date) {
        long days = ChronoUnit.DAYS.between(START, date);
        if (days < 0) return -1;
        long full = days / 7;
        long n = full * 6;
        LocalDate x = START.plusDays(full * 7);
        while (x.isBefore(date)) {
            if (x.getDayOfWeek() != DayOfWeek.SUNDAY) n++;
            x = x.plusDays(1);
        }
        return n;
    }

    public static boolean isReviewDay(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /** Today's words: empty on Sundays, before the start date, or after the list ends. */
    public static List<Word> forDate(Context c, LocalDate date) {
        List<Word> out = new ArrayList<>();
        if (isReviewDay(date) || date.isBefore(START)) return out;
        List<Word> all = all(c);
        long s = slotIndex(date) * PER_DAY;
        for (long i = s; i < s + PER_DAY && i < all.size(); i++) out.add(all.get((int) i));
        return out;
    }
}
