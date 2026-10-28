# Tree Pings

**Get a calendar notification on your phone when it's time to do a tree run. Even with your PC off.**

Tree Pings turns your tree timers into **events on your Google Calendar**. Each event starts
the moment your trees are ready and has a notification set for that time, so your phone
shows an ordinary Google Calendar notification, just like a meeting reminder.

- No new app on your phone. It uses the Google Calendar app you probably already have.
- The events go on their own private calendar, **OSRS timers**, separate from your real one.
- No 3 AM notifications. Anything that finishes overnight waits for one morning event.
- About 2 events a day, not one per tree.

---

## How it works

1. **You farm like normal, with RuneLite open.** Tree Pings reads the timers RuneLite's
   Time Tracking plugin already keeps. It doesn't touch the game.
2. **When your run is done, it updates your calendar.** A few minutes after your last patch
   (or right away when you log out), chat says:
   > Tree Pings: calendar updated. Next ding: Yew x7 ready at 2:40 PM.
3. **At that time, your phone shows a Google Calendar notification.** It works the same
   whether your PC is on, off, or asleep, because the events now live in Google Calendar.
   Tap the notification to open the event and see which patches it's for.

## What you'll see

On a normal day, your **OSRS timers** calendar has events like these. Each one is a short
event at the time shown, and its notification uses the event title:

| Time | Calendar event (and notification) | Inside the event |
| --- | --- | --- |
| 8:00 AM | **Morning run: Yew x7, Palm x5, Calquat x2** | Yew: Falador, Lumbridge, Varrock…<br>Palm: Brimhaven, Catherby…<br>Calquat: Kastori, Tai Bwo Wannai |
| 2:40 PM | **Yew x7 ready (if replanted)** | Yew: Falador, Lumbridge, Varrock… |

- **Your regular trees (yews, magics, etc.) decide when events are created.** Fruit trees,
  calquats and hardwoods don't get events of their own. They're listed on the next
  regular-tree event instead.
- **"(if replanted)"** events are for runs you do on mobile, where RuneLite can't see you
  replant. They assume you replanted when the last notification went off.
- **The calendar keeps itself tidy.** Every update replaces upcoming Tree Pings events with
  the current plan, and events older than 12 hours are deleted.
- **Events are private** and only ever written to the OSRS timers calendar. Your other
  calendars aren't touched.

---

## Setup (about 10 minutes, once)

You need: a Google account, and the Google Calendar app on your phone.

### Step 1: Make your private Google script

This little script lives in **your own** Google account. It's the thing that writes to your calendar.

1. Go to **[script.google.com](https://script.google.com)** and click **New project**.
   Click **Untitled project** at the top and rename it **Tree Pings**.
2. Delete everything in the editor. Paste in **[this code](apps-script/Code.gs)** instead
   (open it, click the copy button). Click 💾 **Save**.
3. Click ⚙️ **Project Settings** on the left. Scroll to **Script properties** → **Add script property**.
   - Property: `TOKEN`
   - Value: make up a long password, like `pineapple-tornado-4471-mustard`.
     **Write it down**, you'll need it in step 3.
   - Click **Save script properties**.
4. Click **< > Editor** on the left. In the dropdown at the top, choose **`setup`**, then click **▶ Run**.
   - Google asks for permission. Click **Review permissions** and pick your account.
   - It will say **"Google hasn't verified this app."** That's normal: it's your own script.
     Click **Advanced** → **Go to Tree Pings (unsafe)** → **Allow**.
   - This creates a new calendar called **OSRS timers**.
5. Click **Deploy** (top right) → **New deployment**.
   - Click the ⚙️ next to "Select type" → **Web app**.
   - **Execute as:** Me
   - **Who has access:** Anyone
   - Click **Deploy**, then **copy the Web app URL**. It ends in `/exec`.

> "Anyone" just means the plugin can reach the script. Nobody can do anything with it
> without your password from step 3.

### Step 2: Make sure your phone gets the notification

1. Open the **Google Calendar** app → ☰ menu → make sure **OSRS timers** is ticked.
2. Back in the script editor, choose **`sendTestEvent`** in the dropdown and click **▶ Run**.
3. **Wait 2 minutes.** Your phone should show a Google Calendar notification called "Tree Pings test ding".

**No notification?** Fix this before going on:
- **Android:** phone Settings → Accounts → your Google account → Account sync → turn on **OSRS timers**.
- **iPhone:** use the Google Calendar app (not Apple's Calendar). Allow its notifications in iPhone Settings.
- Check that Do Not Disturb isn't on, then run `sendTestEvent` again.

### Step 3: Turn on the plugin

1. In RuneLite, open the **Plugin Hub** and install **Tree Pings**.
2. Open its settings (🔧 wrench → search "Tree Pings").
3. Paste your **Apps Script URL** (from step 1.5) and your **Token** (the password from step 1.3).
4. Tick **Send test ding**. Chat says *"test event created"*, and about 2 minutes later your phone shows the calendar notification.

**You're done.** Farm like normal.

---

## Day to day

- **When you log in,** chat tells you what's ready right now and when your next notification is.
- **You can close RuneLite any time** after a run. The events are already on your calendar.
- **Did a run on mobile?** Nothing to do. The "(if replanted)" events already cover it.
  Next time you play on PC, everything updates.

---

## Settings

Most people only need the first three.

| Setting | What it does |
| --- | --- |
| **Apps Script URL** | The link from setup step 1. |
| **Token** | The password from setup step 1. |
| **Send test ding** | Tick it to put a test event on your calendar. Your phone gets its notification 2 minutes later. |
| **Awake from / until** | Your waking hours (24-hour clock). Default 8 to 0 (8 AM to midnight). Anything finishing while you sleep waits for one morning event. Set both the same to get notifications around the clock. |
| **Follow-on runs** | How many runs a day you do on mobile. Default 1. Set to 2 if you do two, or 0 if you only farm on PC. |
| **Chat messages** | The "ready now / next ding" messages in chat. Problems always show, even if this is off. |
| *Advanced* | Sync delay, group window and "only belled patches". You don't need to touch these. |

---

## Something wrong?

Tree Pings tells you in chat when something goes wrong:

| Chat says | Do this |
| --- | --- |
| "it isn't set up yet" | Paste your URL and token in the plugin settings. |
| "(bad token)" | The token in RuneLite must match `TOKEN` in the script **exactly**. |
| "(HTTP …)" or "unexpected reply" | Check the URL ends in `/exec`, and the deployment's **Who has access** is **Anyone**. |
| "(couldn't reach Google)" | Internet problem. It keeps trying on its own. |
| "N trees can't be timed yet" | RuneLite is still learning the farming clock. Stand near a growing tree until it grows a stage. |
| "Time Tracking plugin is turned off" | Turn RuneLite's built-in **Time Tracking** plugin back on. |

**Events are on the calendar but no notification shows up?** That's a phone setting.
Go back through setup step 2.

**Good to know:**
- RuneLite only knows what it last saw. If you skip a mobile run, its "(if replanted)" event still notifies you.
- If a tree gets diseased while you're away, you still get its notification at the normal time.
- Use one RuneScape account per calendar. Each account would overwrite the other's events.
- You can rename the calendar or change its colour. Don't delete it.

---

## Privacy

- Your tree times go to **your own** Google script and **your own** calendar. Nothing else.
- The events are marked **private**.
- The script ignores anything that doesn't carry your token.
- Keep your URL and token to yourself.

---

## For developers

Build and run locally with `./gradlew run` (JDK 11+).

The `farming` folder contains code copied from RuneLite's Time Tracking plugin (BSD 2-Clause
License, see `NOTICE`), because RuneLite doesn't make those classes public. Tree Pings only
reads Time Tracking's saved data and never changes it.
