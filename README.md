# Tree Pings

**Get a calendar notification on your phone when it's time to do a tree run. Even with your PC off.**

Tree Pings turns your tree timers into **events on your Google Calendar**. Each event starts
the moment your trees are ready and has a notification set for that time, so your phone
shows an ordinary Google Calendar notification, just like a meeting reminder.

- No new app on your phone. It uses the Google Calendar app you probably already have.
- The events go on their own private calendar, **OSRS timers**, separate from your real one.
- No 3 AM notifications. Anything that finishes overnight waits for one morning event.
- About 2 events a day, not one per tree.
- **Exact on PC, a best guess on mobile.** RuneLite can't see what you do on mobile, so
  after a mobile run Tree Pings assumes you replanted when it reminded you and keeps the
  reminders coming. Log in on PC now and then and they snap back to your exact timers.

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

<img src="docs/event.png" alt="A Tree Pings event in Google Calendar: Yew, Celastrus, Camphor, Palm at 9:51 PM, with the patches listed inside" width="480">

| Time | Calendar event (and notification) | Inside the event |
| --- | --- | --- |
| 8:00 AM | **🌳 Yew, Palm, Calquat** | Yew: Falador, Lumbridge, Varrock…<br>Palm: Brimhaven, Catherby…<br>Calquat: Kastori, Tai Bwo Wannai |
| 2:40 PM | **🌳 Yew** | Yew (if replanted): Falador, Lumbridge, Varrock… |

- **Your regular trees (yews, magics, etc.) decide when events are created.** Fruit trees,
  calquats and hardwoods don't get events of their own. They're listed on the next
  regular-tree event instead.
- **After a mobile run, the times are a best guess.** RuneLite can't see mobile, so Tree
  Pings assumes you replanted when it reminded you and keeps a daily rhythm going for 5 days.
  These events say "(if replanted)". If you ran late or skipped a run, the guess is off until
  RuneLite next sees the patch on PC, and then the real timer takes over.
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

- **When you log in,** chat tells you when your next notification is.
- **You can close RuneLite any time** after a run. The events are already on your calendar.
- **Did a run on mobile?** Nothing to do. The estimated rhythm covers it, for days at a time.
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

## FAQ

**Why Google Calendar, and not a phone app or a push service?**
Almost everyone already has it, it can ring your phone at an exact time with your PC off,
and events can be moved or removed later when your timers change. It also means there's
nothing new to install on your phone.

**Why do I have to set up my own Google script?**
So that nothing sits between you and your calendar. There's no Tree Pings server and no
account: the plugin talks to a small script in your own Google account, and that script
writes to your own calendar. Google doesn't allow anyone else to deploy a script for you,
which is why setup has a Deploy step.

**Google says "this app isn't verified". Is that safe?**
That warning appears for every personal script, because nobody at Google has reviewed it.
Here the "app" is your own copy of the script, running in your account. The code is short
and you can read all of it in `apps-script/Code.gs`.

**How does it know about my mobile runs?**
It doesn't. It guesses that you replanted when it reminded you, and corrects itself the
next time RuneLite sees the patch on PC.

**It said my trees were ready and they weren't. Why?**
That reminder was an estimate (the event says "(if replanted)") and you planted later than
it assumed, or skipped a run. Log in on PC and walk past the patch: the real timer takes over.

**Why isn't there a notification for every tree?**
Because nobody does a run for one tree. Patches that finish close together share one
notification, so you get about two a day instead of twenty.

**Why don't fruit trees, calquats and hardwoods get their own notifications?**
They take most of a day, and you'd normally do them on a tree run anyway. So they wait and
are listed on your next regular-tree notification. If you have no regular trees growing,
they do get their own.

**Why are the titles just tree names, with no counts?**
The calendar already shows the time, and after a mobile run RuneLite can't know how many
patches you actually did. A count would often be wrong. Tap the event for the patch list.

**Why nothing at night?**
Anything that finishes while you're asleep waits for one notification when you wake up. Set
your hours with **Awake from / until**, or set both the same to be notified around the clock.

**Why does the calendar update a few minutes after my run, not straight away?**
So a run in progress isn't sent half-finished. It waits until you've stopped touching
patches for a few minutes, or sends right away when you log out or close RuneLite.

**Why only trees? What about herbs and allotments?**
No deep reason: I built it for my own tree runs and didn't need the others. Nothing stops
herbs or allotments from being added later.

**Does it cost anything?**
No. Google Apps Script and Google Calendar are free for personal use, and Tree Pings uses
a tiny fraction of the free limits.

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
