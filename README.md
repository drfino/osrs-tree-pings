# Tree Pings

**Get a calendar notification on your phone when it's time to do a tree run. Even with your PC off.**

Tree Pings turns your tree timers into **events on your Google Calendar**. Each event starts
the moment your trees are ready and has a notification set for that time, so your phone
shows an ordinary Google Calendar notification, just like a meeting reminder.

- No new app on your phone. It uses the Google Calendar app you probably already have.
- The events go on their own private calendar, **OSRS timers**, separate from your real one.
- No 3 AM notifications. Anything that finishes overnight waits for one morning event.
- About 2 events a day, not one per tree.
- **Works with mobile runs.** Play on mobile for days and the reminders keep coming.
  Log in on PC now and then and they snap back to your exact timers.

---

## How it works

1. **You farm like normal, on PC or mobile.** On PC, Tree Pings reads the timers RuneLite's
   Time Tracking plugin already keeps. It doesn't touch the game.
2. **When your run is done, it updates your calendar.** A few minutes after your last patch
   (or right away when you log out), chat says:
   > Tree Pings: calendar updated. Next ding: 🌳 Yew at 2:40 PM.
3. **At that time, your phone shows a Google Calendar notification.** It works the same
   whether your PC is on, off, or asleep, because the events now live in Google Calendar.
   Tap the notification to open the event and see which patches it's for.

## What you'll see

On a normal day, your **OSRS timers** calendar has events like these. Each one is a short
event at the time shown, and its notification uses the event title:

| Time | Calendar event (and notification) | Inside the event |
| --- | --- | --- |
| 8:00 AM | **🌳 Yew, Palm, Calquat** | Yew: Falador, Lumbridge, Varrock…<br>Palm: Brimhaven, Catherby…<br>Calquat: Kastori, Tai Bwo Wannai |
| 2:40 PM | **🌳 Yew** | Yew (if replanted): Falador, Lumbridge, Varrock… |

- **Your regular trees (yews, magics, etc.) decide when events are created.** Fruit trees,
  calquats and hardwoods don't get events of their own. They're listed on the next
  regular-tree event instead.
- **It keeps a daily rhythm going for the next 5 days.** It assumes you replant at every
  notification: a morning run, plus one more run a day one grow cycle later (change this with
  **Follow-on runs**). So mobile runs, even several days of them, still get reminders. When
  RuneLite sees a patch again on PC, its exact timer takes over. Trees you only plant once
  (like a spirit tree) aren't assumed.
- **Titles are just the trees.** The calendar already shows the time, and after mobile runs
  RuneLite can't know which patches you did, so there are no counts. Tap an event for the
  patch list.
- **The calendar keeps itself tidy.** Every update replaces upcoming Tree Pings events with
  the current plan, and events older than 12 hours are deleted.
- **Events are private** and only ever written to the OSRS timers calendar. Your other
  calendars aren't touched.

---

## Setup (about 3 minutes, once)

You need a Google account and the Google Calendar app on your phone. Install **Tree Pings**
from RuneLite's Plugin Hub first.

### 1. Get your own copy of the script

The script lives in **your own** Google account. It's the thing that writes to your calendar.

1. Open **[the Tree Pings script](https://script.google.com/d/1sDLFpKl0nzbDjGBqqHxnH1PVNPpTsI0ekrgv96VX_rL39DK4JuevPp4u/edit)**
   (sign in to Google if it asks).
2. Click ⓘ **Overview** on the left, then the **Make a copy** icon at the top right.
   You're now in your own copy, called "Copy of Tree Pings".

*No copy button? Go to [script.google.com](https://script.google.com) → **New project**, delete
everything in the editor, paste in [this code](apps-script/Code.gs) and click 💾 **Save**.*

### 2. Deploy it

1. Click **Deploy** (top right) → **New deployment**.
2. Click the ⚙️ next to "Select type" → **Web app**.
3. Set **Execute as:** Me and **Who has access:** Anyone, then click **Deploy**.
4. Google asks for permission. Click **Authorize access** and pick your account. It says
   **"Google hasn't verified this app."** That's normal: it's your own script. Click
   **Advanced** → **Go to (your project) (unsafe)** → **Allow**.
5. **Copy the Web app URL.** It ends in `/exec`.

> "Anyone" just means the plugin can reach the script. The first time the plugin connects, the
> script locks itself to that plugin and ignores everyone else.

### 3. Paste the URL into Tree Pings

In RuneLite, open the Tree Pings settings (🔧 wrench → search "Tree Pings") and paste the URL
into **Script URL**. Chat says *"Tree Pings: connected."*

### 4. Check your phone

About 2 minutes later your phone shows a Google Calendar notification called
"Tree Pings test ding". **You're done.** Farm like normal.

**Didn't get it?** The plugin is fine if chat said "connected"; this is a phone setting:
- Open the **Google Calendar** app → ☰ menu → make sure **OSRS timers** is ticked.
- **Android:** phone Settings → Accounts → your Google account → Account sync → turn on **OSRS timers**.
- **iPhone:** use the Google Calendar app (not Apple's Calendar) and allow its notifications in iPhone Settings.
- Check that Do Not Disturb isn't on, then tick **Send test ding** in the plugin settings to try again.

---

## Day to day

- **When you log in,** chat tells you what's ready right now and when your next notification is.
- **You can close RuneLite any time** after a run. The events are already on your calendar.
- **Did a run on mobile?** Nothing to do. The daily rhythm already covers it, for days at a time.
- **Want exact times again after mobile runs?** Walk past any one patch of each kind on PC (the
  Farming Guild has most kinds). If RuneLite sees one tree of a kind freshly planted, every patch
  of that kind it still thinks is "Done" gets the same timer.
  Next time you play on PC, everything updates.

---

## Settings

Most people only need the Script URL.

| Setting | What it does |
| --- | --- |
| **Open setup guide** | Tick it to open the setup steps above in your browser. |
| **Script URL** | The Web app URL from setup step 2. Pasting it connects the plugin and sends a test ding. |
| **Send test ding** | Tick it to put a test event on your calendar. Your phone gets its notification 2 minutes later. |
| **Awake from / until** | Your waking hours (24-hour clock). Default 8 to 0 (8 AM to midnight). Anything finishing while you sleep waits for one morning event. Set both the same to get notifications around the clock. |
| **Follow-on runs** | Runs a day after your morning run. Default 1 (morning + afternoon). Set 2 for three runs a day, or 0 if you only farm on PC and want reminders only for what RuneLite has seen. |
| **Chat messages** | The "next ding" messages in chat. Problems always show, even if this is off. |
| *Advanced* | Sync delay, group window and "only belled patches". You don't need to touch these. |

---

## Something wrong?

Tree Pings tells you in chat when something goes wrong:

| Chat says | Do this |
| --- | --- |
| "not set up yet" | Follow the setup steps above and paste your Script URL in the plugin settings. |
| "(bad token)" | The script is linked to a different RuneLite install. In the script, open ⚙️ **Project Settings**, delete `TOKEN` under **Script properties**, then paste the Script URL into the plugin again. |
| "(HTTP …)" or "unexpected reply" | Check the URL ends in `/exec`, and the deployment's **Who has access** is **Anyone**. |
| "(couldn't reach Google)" | Internet problem. It keeps trying on its own. |
| "N trees can't be timed yet" | RuneLite is still learning the farming clock. Stand near a growing tree until it grows a stage. |
| "Time Tracking plugin is turned off" | Turn RuneLite's built-in **Time Tracking** plugin back on. |

**Events are on the calendar but no notification shows up?** That's a phone setting.
See "Didn't get it?" in setup step 4.

**Good to know:**
- RuneLite only knows what it last saw. If you skip a run, the rhythm assumes you didn't and keeps reminding you on schedule; log in on PC to resync.
- A patch RuneLite hasn't seen finish for 2 weeks is treated as abandoned and gets no more reminders until you visit it again.
- If a tree gets diseased while you're away, you still get its notification at the normal time.
- Use one RuneScape account per calendar. Each account would overwrite the other's events.
- You can rename the calendar or change its colour. Don't delete it.

---

## Privacy

- Your tree times go to **your own** Google script and **your own** calendar. Nothing else.
- The events are marked **private**.
- The script only listens to the plugin that connected to it first, and ignores everything else.
- Keep your Script URL to yourself.

---

## For developers

Build and run locally with `./gradlew run` (JDK 11+).

The `farming` folder contains code copied from RuneLite's Time Tracking plugin (BSD 2-Clause
License, see `NOTICE`), because RuneLite doesn't make those classes public. Tree Pings only
reads Time Tracking's saved data and never changes it.
