/**
 * Tree Pings — Google Apps Script receiver.
 *
 * Receives farming finish times from the Tree Pings RuneLite plugin and keeps a
 * dedicated, private "OSRS timers" calendar in sync. Google then delivers the
 * notifications to your phone, even when your PC is off.
 *
 * Setup:
 *   1. script.google.com -> New project -> paste this file.
 *   2. Project Settings -> Script properties -> add TOKEN = <a long random string>.
 *   3. Run `setup` once from the editor and approve the Calendar permission.
 *   4. Deploy -> New deployment -> Web app
 *        Execute as: Me      Who has access: Anyone
 *      Copy the Web app URL into the plugin settings, along with the TOKEN.
 *   5. Run `sendTestEvent` from the editor (or the plugin's test button):
 *      your phone should ding in about 2 minutes.
 *
 * Request body (JSON), sent by the plugin:
 *   {
 *     "token": "...",
 *     "test": false,
 *     "batches": [
 *       {"key": "yew-1", "title": "7 yews ready", "readyAt": 1793050800, "detail": "Falador, ..."}
 *     ]
 *   }
 * The batches list is the FULL current set. Every future event this script made is
 * replaced by it, so a replant or a harvest never leaves a stale ding behind.
 */

const CALENDAR_NAME = 'OSRS timers';
const TAG = 'treepings';
const EVENT_MINUTES = 10;
const LOOKAHEAD_DAYS = 14;

function doPost(e) {
  try {
    const body = JSON.parse((e && e.postData && e.postData.contents) || '{}');
    const expected = PropertiesService.getScriptProperties().getProperty('TOKEN');
    if (!expected || body.token !== expected) {
      return reply_({ ok: false, error: 'bad token' });
    }

    if (body.test) {
      sendTestEvent();
      return reply_({ ok: true, test: true });
    }

    const batches = Array.isArray(body.batches) ? body.batches : [];
    const written = syncBatches_(batches);
    return reply_({ ok: true, written: written });
  } catch (err) {
    return reply_({ ok: false, error: String(err) });
  }
}

function doGet() {
  return reply_({ ok: true, service: 'treepings' });
}

/** Run once from the editor to create the calendar and grant permission. */
function setup() {
  const cal = calendar_();
  Logger.log('Using calendar: ' + cal.getName() + ' (' + cal.getId() + ')');
}

/** Creates a test event 2 minutes from now. Your phone should ding. */
function sendTestEvent() {
  const start = new Date(Date.now() + 2 * 60 * 1000);
  createEvent_(calendar_(), 'test', 'Tree Pings test ding', start, 'If you got this, it works.');
}

/** Replaces every future Tree Pings event with the given batches. */
function syncBatches_(batches) {
  const cal = calendar_();
  const lock = LockService.getScriptLock();
  lock.waitLock(20000);
  try {
    const now = new Date();
    const until = new Date(now.getTime() + LOOKAHEAD_DAYS * 24 * 3600 * 1000);
    cal.getEvents(now, until).forEach(function (ev) {
      if (ev.getTag(TAG) && ev.getTag('key') !== 'test') {
        ev.deleteEvent();
      }
    });

    let written = 0;
    batches.forEach(function (b) {
      const readyAt = Number(b.readyAt);
      if (!b.key || !b.title || !isFinite(readyAt)) {
        return;
      }
      const start = new Date(readyAt * 1000);
      if (start <= now) {
        return; // already ready; nothing to remind about
      }
      createEvent_(cal, String(b.key), String(b.title), start, String(b.detail || ''));
      written++;
    });
    return written;
  } finally {
    lock.releaseLock();
  }
}

function createEvent_(cal, key, title, start, detail) {
  const end = new Date(start.getTime() + EVENT_MINUTES * 60 * 1000);
  const ev = cal.createEvent(title, start, end, { description: detail });
  ev.setVisibility(CalendarApp.Visibility.PRIVATE);
  ev.removeAllReminders();
  ev.addPopupReminder(0);
  ev.setTag(TAG, '1');
  ev.setTag('key', key);
  return ev;
}

function calendar_() {
  const props = PropertiesService.getScriptProperties();
  const id = props.getProperty('CALENDAR_ID');
  if (id) {
    const existing = CalendarApp.getCalendarById(id);
    if (existing) {
      return existing;
    }
  }
  const byName = CalendarApp.getCalendarsByName(CALENDAR_NAME);
  const cal = byName.length ? byName[0] : CalendarApp.createCalendar(CALENDAR_NAME);
  props.setProperty('CALENDAR_ID', cal.getId());
  return cal;
}

function reply_(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj)).setMimeType(ContentService.MimeType.JSON);
}
