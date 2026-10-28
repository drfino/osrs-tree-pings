# Tree Pings

**Your phone dings when it's time to do a tree run. Even with your PC off.**

You plant your trees with RuneLite open, like normal. Tree Pings puts the finish times on a
private Google Calendar, and the Google Calendar app on your phone dings you when it's time to go.

- No new app on your phone. It uses Google Calendar, which you probably already have.
- No 3 AM dings. Anything that finishes overnight waits for one morning ding.
- About 3 dings a day, not one per tree.

---

## What you'll get

A normal day looks like this:

| Time | Your phone says |
| --- | --- |
| 8:00 AM | **Morning run: Yew x7, Palm x5, Calquat x2** |
| 2:40 PM | **Yew x7 ready (if replanted)** |
| 9:20 PM | **Yew x7 ready (if replanted)** |

- **Your regular trees (yews, magics, etc.) decide when you get dinged.** Fruit trees,
  calquats and hardwoods don't ding on their own. They're listed on the next ding instead.
- **"(if replanted)"** dings are for runs you do on mobile, where RuneLite can't see you
  replant. They assume you replanted when the last ding went off.

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

### Step 2: Make sure your phone dings

1. Open the **Google Calendar** app → ☰ menu → make sure **OSRS timers** is ticked.
2. Back in the script editor, choose **`sendTestEvent`** in the dropdown and click **▶ Run**.
3. **Wait 2 minutes.** Your phone should ding with "Tree Pings test ding".

**No ding?** Fix this before going on:
- **Android:** phone Settings → Accounts → your Google account → Account sync → turn on **OSRS timers**.
- **iPhone:** use the Google Calendar app (not Apple's Calendar). Allow its notifications in iPhone Settings.
- Check that Do Not Disturb isn't on, then run `sendTestEvent` again.

### Step 3: Turn on the plugin

1. In RuneLite, open the **Plugin Hub** and install **Tree Pings**.
2. Open its settings (🔧 wrench → search "Tree Pings").
3. Paste your **Apps Script URL** (from step 1.5) and your **Token** (the password from step 1.3).
4. Tick **Send test ding**. Chat says *"test event created"*, and your phone dings in about 2 minutes.

**You're done.** Farm like normal.

---

## Using it

- **Do your tree run with RuneLite open.** When you're done (or when you log out), chat says:
  > Tree Pings: calendar updated. Next ding: Yew x7 ready at 2:40 PM.
- **When you log in,** chat tells you what's ready right now and when your next ding is.
- **You can close RuneLite.** The dings are on your calendar and will arrive anyway.
- **Did a run on mobile?** Nothing to do. The "(if replanted)" dings already cover it.
  Next time you play on PC, everything updates.

---

## Settings

Most people only need the first three.

| Setting | What it does |
| --- | --- |
| **Apps Script URL** | The link from setup step 1. |
| **Token** | The password from setup step 1. |
| **Send test ding** | Tick it to test. Your phone dings in 2 minutes. |
| **Awake from / until** | Your waking hours (24-hour clock). Default 8 to 0 (8 AM to midnight). Anything finishing while you sleep waits for one morning ding. Set both the same to get dings around the clock. |
| **Follow-on runs** | How many runs a day you do on mobile. Default 2. Set to 0 if you only farm on PC. |
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

**Events are on the calendar but your phone doesn't ding?** That's a phone setting.
Go back through setup step 2.

**Good to know:**
- RuneLite only knows what it last saw. If you skip a mobile run, its "(if replanted)" ding still comes.
- If a tree gets diseased while you're away, it still dings at the normal time.
- Use one RuneScape account per calendar. Each account would overwrite the other's dings.
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

Files under `src/main/java/com/treepings/farming` are copied from RuneLite's Time
Tracking plugin under the BSD 2-Clause License (see `NOTICE`), because those classes aren't
public API. Tree Pings only reads the `timetracking` config group and never writes to it.
