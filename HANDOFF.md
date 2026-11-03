# Wandering Words: project handoff

Last updated: November 3, 2026 (app version 1.6, build 7).

This document explains everything another Claude session (or another person) needs to understand, maintain and extend Wandering Words. Hand it to the new account along with access to the GitHub repository `zyavarow/wanderingwords`.

---

## 1. What it is

Wandering Words is Zollie Yavarow's personal vocabulary practice. It has two parts:

1. **An Android app** (sideloaded, not on the Play Store) with:
   - a home-screen widget in a "Night Glass" style showing today's two words, their categories and definitions;
   - a small **+** in the widget's corner that adds one extra word for the day and shows it on the widget;
   - a daily notification at **7:00 am** (changeable in the app) with both words, definitions and example sentences;
   - an app screen with today's words in full, any extra words, a "+ One more word" button, notification settings and a link to the word page.
2. **A word page** (a published claude.ai artifact) with three tabs:
   - **Today**: the day's words, plus any extra words, each with a box to write her own sentence;
   - **Review**: a Sunday recall session (see the word, recall the meaning, reveal it, write a sentence, mark "Got it" or "Shaky");
   - **Library**: every word seen so far, searchable, filterable to Shaky or "My sentences".

Everything works offline in the app. The word page saves ratings and sentences to the artifact's database so they follow her across devices.

## 2. Who it is for, and what she asked for

Zollie holds a PhD in pharmacology and an MA in bioethics, works in rare disease and AI, lives nomadically (often in Ubud, Bali), dances and keeps a reflective journal. She wanted GRE-level words tailored to her life, style and tone, to elevate her writing and speech and sound eloquent and well read.

Requests and decisions so far, in order:
- No subscription app. A free widget or notification on her phone.
- About 1,000 words, enough for at least a year, two words a day.
- Words tailored to her worlds, with example sentences that sound like her life.
- A weekly quiz for memory and recall: remember the word, recall the definition, use it in a sentence.
- Classic literature words added so she sounds well read.
- New words **every day, including Sundays**. Sunday is still review day.
- A simple way to get **one more word at a time**, without reading ahead and without choosing a category.
- A category for embodied, sensual, evocative, visceral and salacious words that can almost be felt. It is named **Embodied & Evocative**.
- The widget **+** button, with the extra word shown on the widget, and a compact layout so three words fit.
- Night Glass chosen over two other widget styles.
- App name chosen by her: **Wandering Words**.
- She does not want it tied to Google Calendar or any other app she uses.

## 3. Where everything lives

| Piece | Location |
|---|---|
| App source code | GitHub, private: `zyavarow/wanderingwords` (branch `main`) |
| App builds | GitHub Actions builds on every push to `main` and attaches `WanderingWords.apk` to a new release (`build-N`) |
| Word page (live) | claude.ai artifact `https://claude.ai/artifact/1EqQ5R7SUWft5zLDoopNLp` (owned by the original account) |
| Word page source | `web/template.html` in the repo (the page with a `__DATA__` placeholder) |
| Built word page | `web/word-page.html` (template plus all words, ready to publish) |
| Master word list | `web/words-data.json` (all daily words and extra words, with ids) |
| App word files | `app/src/main/assets/words.json` and `bonus.json` (generated) |
| Build script | `tools/build.py` regenerates the word page and the app word files from the master list |
| Her saved progress | `web/progress-backup.json` (ratings and sentences exported November 3, 2026) |

## 4. The word list

- **1,159 daily words** across ten categories, and **206 extra words** (for the + button) across the same ten.
- Each word has: word, pronunciation (e.g. `VUR-suh-tul`), part of speech, definition, an example sentence written in Zollie's voice, and a category.
- Words were deduplicated and checked so no word appears twice across the daily and extra lists.
- Categories (daily count):
  1. Classic Literature (151)
  2. Reflection & Inner Life (128)
  3. Science & Bioethics (118)
  4. Personal Development (118)
  5. Nomadic Life & Place (115)
  6. Leadership (110)
  7. Movement & the Body (107) (renamed from "Embodiment & Movement")
  8. Embodied & Evocative (106) (sensual words; first called "Sensual & Evocative")
  9. Spirituality (103)
  10. AI & Technology (103)
- Categories are interleaved so each day's two words usually come from different categories. Embodied & Evocative words appear roughly once every ten words.

### Master list format (`web/words-data.json`)

```json
{
  "words": [ {"id": 0, "w": "distributive", "p": "dih-STRIB-yoo-tiv", "pos": "adj",
              "d": "concerning how resources or benefits are shared",
              "e": "Distributive justice asks who gets access when a therapy costs millions.",
              "t": "Science & Bioethics"}, ... ],
  "bonus": [ {"id": 1159, "w": "...", ..., "b": 1}, ... ]
}
```

Daily word ids run 0 to 1158; extra word ids continue from 1159. **Ids matter**: the word page stores ratings and sentences by id, so never reorder or remove words that have already been shown. Add new words only at future positions or at the end.

## 5. The schedule

- Start date: **Wednesday, October 28, 2026**.
- Two new words **every day** (Sundays included). Word index for a date = (days since start) × 2.
- The list lasts until about **May 29, 2028**.
- Uses the phone's local date, so it follows her as she travels.
- **Sunday is review day.** The review covers the seven days before that Sunday (that Sunday's own new words wait for the next review). It also brings back any word she marked Shaky, plus refreshers from 4 and 12 weeks earlier (up to 12 extras).
- On other days the Review tab offers an optional warm-up with the week so far.
- History note: the first version had no new words on Sundays. That changed on November 1, 2026. No Sunday had passed yet, so the simple "days × 2" rule gives the same words for every day already shown.

## 6. Extra words (the + button)

- Taking an extra word never touches the daily schedule. Extra words come from the separate 206-word collection, in a fixed order.
- In the app, extra words taken are stored as `index:yyyy-mm-dd` entries in the app's shared preferences (key `bonus`).
- The widget shows the newest extra word of the day in amber below the daily pair, labeled "EXTRA · CATEGORY" (or "EXTRA 2 · ..." when there are several). When an extra word is showing, the widget switches to a compact layout (smaller type, one-line definitions, tighter spacing). The next day it returns to normal.
- **Syncing to the word page:** the app cannot write to the artifact's database directly. Instead, when extra words exist, tapping the widget (or "Write sentences for extra words" in the app) opens the word page at `#bonusN`, where N is the total number of extra words taken. The page marks extra words 0 to N−1 as collected (with today's date if new), saves that to the database, and then shows them on Today, in the Library and in Sunday's review.

## 7. The Android app

- Package: `com.zyavarow.wanderingwords`. Plain Java, no AndroidX. minSdk 26, targetSdk 34, compileSdk 34.
- Built with Gradle 8.7 and Android Gradle Plugin 8.5.2 on GitHub Actions (`.github/workflows/build.yml`). Builds take about 2 to 3 minutes and use the free private-repo minutes.
- Signed with a keystore committed in the repo: `app/lexicon.keystore`, alias `lexicon`, store and key password `lexicon123`. **Keep using this same keystore.** Android only allows an update to install over the existing app if the signature matches. A new key would force her to uninstall first, which erases her extra-word history in the app.
- Raise `versionCode` and `versionName` in `app/build.gradle` with every release.
- Source files (`app/src/main/java/com/zyavarow/wanderingwords/`):
  - `Words.java`: loads the word files, computes today's words, handles extra words, holds `LEXICON_URL` (the word page link).
  - `WordWidget.java`: builds the widget, the + action, the compact layout and the tap-to-open link.
  - `Scheduler.java`: schedules the morning notification (inexact 5-minute window, no exact-alarm permission needed) and a just-after-midnight widget refresh.
  - `AlarmReceiver.java`: posts the notification; reschedules after reboot, time change, time zone change and app update.
  - `MainActivity.java`: the app screen, built in code.
- Widget layout: `res/layout/widget.xml` (frosted dark panel `#9E0E161A`, white 14% border, 22dp corners; category labels moss `#86C3AD`; words in serif `#E5EBE7`; definitions `#B8C6C0`; extra word label amber `#E2B262`). Default size 4×2, resizable.
- Permissions: notifications (asked on first open) and boot-completed. No internet permission is needed.

### How Zollie installs an update

1. Download the newest `WanderingWords.apk` on her phone (from the chat, or from the repo's latest release while signed in to GitHub).
2. Open it and tap **Update** (and "Update anyway" if Play Protect asks).
3. Settings, extra words and the notification time carry over. If she sees "App not installed", she tapped an older file; delete old copies from Downloads.

## 8. The word page (artifact)

- Single HTML file. Fonts: Cormorant Garamond (words), Instrument Sans (body), IBM Plex Mono (pronunciation), from Google Fonts. Light and dark themes (paper `#F2F5F1` / ink `#1C232B`, dark `#111618`; moss accent `#2E5E4E` light, `#86C3AD` dark; amber for Shaky).
- Declares the `db` and `user` capabilities. Her data is one private document at `data/users/<her id>/progress` with fields `r` (ratings by id: `got` or `shaky`), `s` (sentences by id), `b` (extra words collected: index → date). It also mirrors to the browser's localStorage key `lexicon-state` as a fallback.
- Routes: `#today` (default), `#review`, `#library`, and `#bonusN` (records extra words, then shows Today).

## 9. Moving to a new account

An artifact belongs to the account that published it, so the new account cannot edit the existing word page. To move:

1. Give the new account access to the GitHub repo (connect GitHub in claude.ai settings and install the Claude GitHub app on `wanderingwords`).
2. Publish `web/word-page.html` as a new artifact with capabilities `{"db": {}, "user": {}}`.
3. Restore her progress: write `web/progress-backup.json`'s `r`, `s` and `b` fields to `data/users/me/progress` in the new artifact's database. If she has used the old page since November 3, 2026, export the old document first (it holds anything newer).
4. Put the new artifact's link in `LEXICON_URL` in `Words.java`, raise the version, push, and send her the new APK. Until she updates, the widget still opens the old page.
5. The old artifact keeps working until it is deleted.

## 10. Common changes

- **Add words:** add entries to `web/words-data.json` at future positions (or the end), keep ids sequential, run `python3 tools/build.py`, republish the word page, raise the app version, push.
- **Rename a category:** change the `t` value everywhere in `web/words-data.json`, rebuild, republish, push.
- **Change the default notification time:** `Scheduler.notifyHour` default (currently 7). She can also change it in the app.
- **Style changes to the widget:** `res/layout/widget.xml`, `res/drawable/glass.xml` and `applySizes()` in `WordWidget.java`.

## 11. What was tried and why it was dropped

- **KWGT (Kustom widget app):** the planned free widget. It could not read the word file from her Downloads folder (Android storage restrictions; KWGT did not appear under "All files access"), and on her phone it could not download anything from the internet, not even a test page from Google, even with battery set to Unrestricted. Abandoned in favor of building her own app.
- **Google Drive hosting** of the word file: worked as a public link, but KWGT still could not fetch it.
- **Google Calendar import** (an .ics file with each day's words as events): she declined because she uses Google Calendar for other things and wants this independent.
- **Building the app inside the Claude workspace:** not possible there (Android build tools cannot be downloaded), so GitHub Actions does the build.

## 12. Working preferences

- Zollie is on Android. She is comfortable following step-by-step instructions but prefers things that just work. Keep explanations plain and short.
- Give her the APK as a file in chat after each build, with one or two lines on what changed and how to update.
- She likes being shown design options visually before choosing.
- Frame things positively. Avoid heavy em-dash use in anything written for her to send.

## 13. Ideas not yet built

- A choose-a-category extra word (she said she did not need it).
- Showing her own sentences in the app (they live only on the word page today).
- A home-screen widget for Sunday review progress.
