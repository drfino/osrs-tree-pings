# Tree Pings

**Get a calendar notification on your phone when it's time to do a tree run. Even with your PC off.**

Tree Pings turns your tree timers into **events on your Google Calendar**. Each event starts
the moment your trees are ready and has a notification set for that time, so your phone
shows an ordinary Google Calendar notification, just like a meeting reminder.

- No new app on your phone. It uses the Google Calendar app.
- The events go on their own private calendar, **OSRS timers**, separate from your real one.
- No 3 AM notifications. Anything that finishes overnight waits for one morning event (can be changed in settings)
- About ~2 events a day, trees are batched for efficient runs.
- **Exact times when on PC, fuzzy matching on mobile runs.** RuneLite can't see what you do on mobile, so
  after a mobile run Tree Pings assumes you replanted when it reminded you and keeps the
  reminders coming. Log in on PC now and then and they snap back to your exact timers.

---

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

## How it works

1. **You farm like normal, on PC or mobile.** On PC, Tree Pings reads the timers RuneLite's
   Time Tracking plugin already keeps. It doesn't touch the game.
2. **When your run is done, it updates your calendar.** A few minutes after your last patch
   (or right away when you log out), chat says:
   > Tree Pings: calendar updated. Next ding: 🌳 Yew at 2:40 PM.
3. **At that time, your phone shows a Google Calendar notification.** It works the same
   whether your PC is on, off, or asleep, because the events now live in Google Calendar.
   Tap the notification to open the event and see which patches it's for.

---

## Setup (about 3 minutes, once)

**Before you start, you need:**
- Tree Pings installed from RuneLite's Plugin Hub.
- A Google account.
- The Google Calendar app on your phone, signed in to that same Google account.

Keep this page open while you set up. When a step says "new tab", right-click the link and
choose **Open link in new tab**, so you can come back here.

### Part 1: Copy the script

1. Open **[the Tree Pings script](https://script.google.com/d/1sDLFpKl0nzbDjGBqqHxnH1PVNPpTsI0ekrgv96VX_rL39DK4JuevPp4u/edit)** in a new tab.
   Sign in to Google if it asks. You'll see some code. You don't need to read or change it.
2. Click the **ⓘ** icon at the top of the left sidebar. This opens the Overview page.
3. Click the **Make a copy** icon at the top right. It looks like two pages, next to the star:

   <img src="docs/copy-icon.png" alt="The Make a copy icon, circled, next to the star at the top right of the Overview page" width="200">

   Your own copy opens. Its name at the top of the page is **Copy of OSRS Tree Pings**.
   Do all the next steps in your copy.

### Part 2: Turn it on

4. Click the blue **Deploy** button at the top right, then **New deployment**.
5. Click the ⚙️ gear next to "Select type", then **Web app**.
6. Under **Who has access**, choose **Anyone**. Leave **Execute as** set to **Me**.
   Click **Deploy**.
7. Click **Authorize access**, then choose your Google account.
8. Google shows **"Google hasn't verified this app."** This is expected, because the script
   is your own copy and Google hasn't reviewed it. Click **Advanced** (a small link at the
   bottom left), then **Go to Copy of OSRS Tree Pings (unsafe)**, then **Allow**. If you see
   checkboxes instead of Allow, tick **Select all** and click **Continue**.
9. Under **Web app URL**, click **Copy**. Then click **Done**.

### Part 3: Connect Tree Pings

10. In RuneLite, click the 🔧 wrench, search **Tree Pings**, and open its settings.
11. Paste into **Script URL**, then click anywhere outside the box.
    Chat says *"Tree Pings: connected."*
12. Wait about 2 minutes. Your phone shows a Google Calendar notification called
    **Tree Pings test ding**.

**You're done.** Farm like normal. You never need to open the script again.

<details>
<summary><b>Chat said "connected" but no notification arrived</b></summary>

The plugin and script are working. This is a phone setting.

- Open the **Google Calendar** app → ☰ menu → make sure **OSRS timers** is ticked.
- **Android:** phone Settings → Accounts → your Google account → Account sync → turn on **OSRS timers**.
- **iPhone:** use the Google Calendar app (not Apple's Calendar), and allow its notifications in iPhone Settings.
- Check that Do Not Disturb is off. Then tick **Send test ding** in the Tree Pings settings to try again.
</details>

<details>
<summary><b>Chat said something else</b></summary>

- **"doesn't look like the Web app URL"**: you copied a different link. Go back to step 9. The right one starts with `https://script.google.com/macros/` and ends in `/exec`.
- **"(HTTP …)" or "unexpected reply"**: in step 6, **Who has access** wasn't set to **Anyone**. In your copy, click **Deploy** → **Manage deployments** → ✏️ edit, change it to **Anyone**, and click **Deploy**.
- **"(bad token)"**: this copy is already linked to another RuneLite install. Make a new copy (step 1), or see "Something wrong?" below.
</details>

<details>
<summary><b>No "Make a copy" icon in step 3</b></summary>

Go to [script.google.com](https://script.google.com) → **New project**. Delete everything in
the editor, paste in [this code](apps-script/Code.gs), and click 💾 **Save**. Then carry on
from step 4.
</details>

---

## Privacy

- Your tree times go to **your own** Google script and **your own** calendar. Nothing else.
- The events are marked **private**.
- The script only listens to the plugin that connected to it first, and ignores everything else.
- Keep your Script URL to yourself.

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
See the setup section, under "Chat said connected but no notification arrived".

**Good to know:**
- RuneLite only knows what it last saw. If you skip a run, the rhythm assumes you didn't and keeps reminding you on schedule; log in on PC to resync.
- A patch RuneLite hasn't seen finish for 2 weeks is treated as abandoned and gets no more reminders until you visit it again.
- If a tree gets diseased while you're away, you still get its notification at the normal time.
- Use one RuneScape account per calendar. Each account would overwrite the other's events.
- You can rename the calendar or change its colour. Don't delete it.

---

## FAQ

**Why do I need my own Google script?**
The RL plugin can't write to your calendar directly, so it sends your tree times to a small
script in your own Google account, and the script updates your calendar. There is no Tree
Pings server and no account to create.

**Google says "this app isn't verified". Is that a problem?**
No. Google shows that warning for any personal script it hasn't reviewed. The script is your
own copy, running in your own account, and you can read all of it in `apps-script/Code.gs`.

**Does it cost anything?**
No! Google Apps Script and Google Calendar are free.

**Why Google Calendar?**
I built this for myself, and Google Calendar is what I use. I'm not sure if this would work on any other calendar, if Apple calendar has a google scripts equivalent, for example.

**How does it know about runs I do on mobile?**
It doesn't...RuneLite can't see mobile play, so the plugin assumes you replanted at the time
of each reminder and schedules the next ones from there. The times are corrected the next
time RuneLite sees the patch on PC.

**A reminder said my trees were ready and they weren't. Why?**
That reminder was one of those assumed times (the event says "if replanted"), and you
planted later than it assumed or skipped a run.

**My fruit trees (or calquats, celastrus, hardwoods) finished hours ago and I got no notification. Is it broken?**
No. Only regular trees (oak, willow, maple, yew, magic) create notifications. Other trees
that finish within 45 minutes of a regular-tree notification are included in it. Ones that
finish later than that are listed on your next regular-tree notification, which can be
several hours later, or the next morning. If you have no regular trees growing, the other
trees get their own notifications.

**Why don't I get a notification for each tree?**
Trees that finish within 45 minutes of each other are batched in one cal event. Fruit trees,
calquats, celastrus and hardwoods don't get their own: they are listed on the next
regular-tree notification.
If you plant different regular trees, for example oaks in some patches and willows in
others, they finish at different times and get separate notifications. To combine them,
raise **Group window** in the Advanced settings, for example to 90 minutes.

**Why are there no notifications at night?**
Anything that finishes outside your awake hours waits until the hour you wake up. You can
change the hours in the settings, or set both to the same hour to turn this off.

**Why does the calendar update a few minutes after my run?**
The plugin waits until you haven't changed a patch for 5 minutes, so it doesn't send a run
that's still in progress. Logging out or closing RuneLite sends it immediately.

**Why do the titles have no patch counts?**
After a mobile run the plugin doesn't know how many patches you did, so the counts were
often wrong. The patch list is inside the event, but I prefered to see "do your yews" rather than "do your 6x yews", because the first one is usually always right, and the number could often be wrong (even though all your trees are usually done at the same time)

**Why only trees?**
Trees are all I wanted reminders for. Herbs and allotments could be added, but I haven't
needed them.

---

## Settings

Most people only need the Script URL.

| Setting | What it does |
| --- | --- |
| **Open setup guide** | Tick it to open the setup steps above in your browser. |
| **Script URL** | The Web app URL from setup step 9. Pasting it connects the plugin and sends a test ding. |
| **Send test ding** | Tick it to put a test event on your calendar. Your phone gets its notification 2 minutes later. |
| **Awake from / until** | Your waking hours (24-hour clock). Default 8 to 0 (8 AM to midnight). Anything finishing while you sleep waits for one morning event. Set both the same to get notifications around the clock. |
| **Follow-on runs** | Runs a day after your morning run. Default 1 (morning + afternoon). Set 2 for three runs a day, or 0 if you only farm on PC and want reminders only for what RuneLite has seen. |
| **Chat messages** | The "next ding" messages in chat. Problems always show, even if this is off. |
| *Advanced* | Sync delay, group window and "only belled patches". You don't need to touch these. |

---

## For developers

Build and run locally with `./gradlew run` (JDK 11+).

The `farming` folder contains code copied from RuneLite's Time Tracking plugin (BSD 2-Clause
License, see `NOTICE`), because RuneLite doesn't make those classes public. Tree Pings only
reads Time Tracking's saved data and never changes it.
