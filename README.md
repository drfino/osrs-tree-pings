# Tree Pings

Your phone dings when your farming trees are ready, even with your PC off.

RuneLite's Time Tracking plugin already knows when each tree patch finishes. Tree Pings
reads those saved finish times, groups them ("Yew x7 ready"), and sends them to a small
Google Apps Script running in your own Google account. The script keeps a private
**OSRS timers** calendar up to date, and Google Calendar dings your phone.

- No new phone app: the Google Calendar app you already have does the notifying.
- Private: events live on your own calendar, marked private, and the script only accepts
  requests that carry your token.
- No login screen left open: once a run is synced, RuneLite can be closed.
- No 3 AM dings: anything that finishes overnight waits for one "Morning run" ding.
- Mobile replants: regular trees (yews, magics, etc.) also get dings one and two grow
  cycles later, for the runs you do on mobile where RuneLite can't see you replant.

## Setup

### 1. The Google side (10 minutes, do this first)

1. Go to script.google.com and click **New project**. Name it "Tree Pings".
2. Replace the contents of `Code.gs` with `apps-script/Code.gs` from this repo. Save.
3. **Project Settings** (gear icon) → **Script properties** → **Add script property**:
   `TOKEN` = a long random string (e.g. 30+ random letters and digits). Save.
4. Back in the editor, pick `setup` in the function dropdown and click **Run**.
   Approve the Calendar permission (Google will warn the app is unverified; it's your own
   script, so choose Advanced → Go to Tree Pings). This creates the **OSRS timers** calendar.
5. Pick `sendTestEvent` and click **Run**. In about 2 minutes, your phone should ding.
   If it doesn't, see "Making the ding reliable" below and fix that before going further.
6. **Deploy** → **New deployment** → type **Web app**.
   Execute as: **Me**. Who has access: **Anyone**. Click Deploy and copy the Web app URL
   (it ends in `/exec`).

"Anyone" means anyone with the URL can reach the script, but it does nothing without the
token. The URL and token are secrets: don't post them.

### 2. Making the ding reliable (phone)

- In the Google Calendar app: Settings → **OSRS timers** → make sure the calendar is
  shown and its notifications are on.
- Android: Settings → Apps → Calendar → Notifications → on, with sound. Exempt Calendar
  from battery optimization.
- iPhone: Settings → Notifications → Google Calendar → Allow, Sounds on, Time Sensitive on.
- If you use Do Not Disturb or Focus, allow Google Calendar through it.

### 3. The RuneLite side

Until Tree Pings is on the Plugin Hub, you can run it for testing with RuneLite's
developer launcher (requires JDK 11 or newer):

```
./gradlew run
```

In the plugin's settings, paste the **Apps Script URL** and **Token**, then tick
**Send test ding**. A console message confirms it, and your phone dings in about 2 minutes.

Do a tree run as usual. Five minutes after your last patch change (or as soon as you log
out or close RuneLite), Tree Pings syncs the full set of upcoming dings. Open the Google Calendar app to see them.

## Settings

| Setting | Default | What it does |
| --- | --- | --- |
| Apps Script URL | | Your web app URL, ending in `/exec`. |
| Token | | Must match the `TOKEN` script property. |
| Send test ding | off | Tick to create a test event 2 minutes out. Unticks itself. |
| Awake from (hour) | 8 | Anything finishing while you sleep goes into one "Morning run" ding at this hour. |
| Awake until (hour) | 0 (midnight) | Set it equal to "Awake from" for dings around the clock. |
| Follow-on runs | 2 | Regular-tree runs you do each day that RuneLite can't see, e.g. on mobile. Each gets an "(if replanted)" ding. 2 matches one PC run plus two mobile runs. |
| Chat messages | on | A chat message when a run is detected, and another when your calendar is updated, naming the next ding. |

**Advanced** (collapsed): Sync delay (5 min, how long after your last patch change it waits before syncing; logging out or closing RuneLite syncs right away), Group window (45 min, patches finishing this close together share a ding), Only belled patches (off).

## How it behaves

- Every sync replaces all future Tree Pings events, so harvesting or replanting on PC
  never leaves a stale ding.
- Follow-on dings say "(if replanted)" and assume you replant when the previous ding goes
  off. Replant later than that and the next one can be early by up to the delay.
- RuneLite only knows what it last saw. If you skip a mobile run, the follow-on still
  fires. The next PC run corrects everything.
- If Time Tracking doesn't yet know your farming tick offset (fresh install), there's no
  confident finish time, so nothing is scheduled until it has seen a patch change.

## Plugin Hub notes

- Tree Pings reads, and never writes, the `timetracking` config group.
- `com.drfino.treepings.farming` contains code copied from RuneLite's Time Tracking
  plugin under the BSD 2-Clause License (see `NOTICE`), because those classes are not
  public API.
- The plugin sends data only to the user-configured Google Apps Script URL, and only
  accepts https Google URLs. Expect the hub to show its standard "submits data to a
  third-party server" warning.
