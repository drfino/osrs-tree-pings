package com.drfino.treepings;

import com.drfino.treepings.farming.ReadyPatch;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BatcherTest
{
	private static final ZoneId NY = ZoneId.of("America/New_York");
	private static final long YEW = 400 * 60;
	private static final long PALM = 960 * 60;
	private static final long CALQUAT = 1280 * 60;
	private static final long WINDOW = 45 * 60;
	private static final long LOOKAHEAD = 13L * 86400;
	private static final Batcher.Awake AWAKE = new Batcher.Awake(NY, 8, 0);
	private static final String ICON = "\uD83C\uDF33 ";

	private static long at(String time)
	{
		return LocalDateTime.parse(time).atZone(NY).toEpochSecond();
	}

	private static List<ReadyPatch> day()
	{
		List<ReadyPatch> ps = new ArrayList<>();
		for (String place : Arrays.asList("Falador", "Lumbridge", "Taverley", "Varrock", "Gnome Stronghold", "Auburnvale"))
		{
			ps.add(new ReadyPatch("tree", "Yew", place, at("2026-10-27T06:51"), YEW));
		}
		ps.add(new ReadyPatch("tree", "Yew", "Farming Guild", at("2026-10-27T06:11"), YEW));
		for (String place : Arrays.asList("Brimhaven", "Catherby", "Gnome Stronghold", "Kastori", "Tree Gnome Village"))
		{
			ps.add(new ReadyPatch("fruit_tree", "Papaya", place, at("2026-10-27T03:31"), PALM));
		}
		ps.add(new ReadyPatch("calquat", "Calquat", "Kastori", at("2026-10-27T08:51"), CALQUAT));
		ps.add(new ReadyPatch("calquat", "Calquat", "Tai Bwo Wannai", at("2026-10-27T08:51"), CALQUAT));
		return ps;
	}

	private static Batch only(List<Batch> batches, long readyAt)
	{
		Batch found = null;
		for (Batch b : batches)
		{
			if (b.readyAt == readyAt)
			{
				found = b;
			}
		}
		assertTrue("no ding at that time", found != null);
		return found;
	}

	@Test
	public void overnightFinishesBecomeOneMorningRun()
	{
		List<Batch> bs = Batcher.build(day(), at("2026-10-27T00:56"), WINDOW, 2, LOOKAHEAD, AWAKE);
		assertEquals(ICON + "Yew, Papaya", only(bs, at("2026-10-27T08:00")).title);
	}

	@Test
	public void slowCropsRideAlongOnTheNextTreeDing()
	{
		List<Batch> bs = Batcher.build(day(), at("2026-10-27T00:56"), WINDOW, 2, LOOKAHEAD, AWAKE);
		assertEquals(ICON + "Yew, Calquat", only(bs, at("2026-10-27T14:40")).title);
		assertEquals(ICON + "Yew", only(bs, at("2026-10-27T21:20")).title);
		// Each is replanted on the run that announced it: papayas at 8:00 are back the next
		// morning, calquats at 14:40 are ready around noon and back on the next 14:40 run.
		assertEquals(ICON + "Yew, Papaya", only(bs, at("2026-10-28T08:00")).title);
		assertEquals(ICON + "Yew, Calquat", only(bs, at("2026-10-28T14:40")).title);
	}

	@Test
	public void noDingsWhileAsleep()
	{
		for (Batch b : Batcher.build(day(), at("2026-10-27T00:56"), WINDOW, 2, LOOKAHEAD, AWAKE))
		{
			assertTrue(b.title, java.time.Instant.ofEpochSecond(b.readyAt).atZone(NY).getHour() >= 8);
		}
	}

	@Test
	public void cropFinishingJustAfterATreeDingJoinsIt()
	{
		List<ReadyPatch> ps = Arrays.asList(
			new ReadyPatch("tree", "Yew", "Falador", at("2026-10-27T15:00"), YEW),
			new ReadyPatch("calquat", "Calquat", "Kastori", at("2026-10-27T15:30"), CALQUAT));
		List<Batch> bs = Batcher.build(ps, at("2026-10-27T12:00"), WINDOW, 0, LOOKAHEAD, AWAKE);
		assertEquals(1, bs.size());
		assertEquals(ICON + "Yew, Calquat", bs.get(0).title);
		assertEquals(at("2026-10-27T15:30"), bs.get(0).readyAt);
	}

	@Test
	public void withoutRegularTreesEveryCropDings()
	{
		List<ReadyPatch> ps = Arrays.asList(new ReadyPatch("fruit_tree", "Palm", "Catherby", at("2026-10-27T15:00"), PALM));
		List<Batch> bs = Batcher.build(ps, at("2026-10-27T12:00"), WINDOW, 0, LOOKAHEAD, AWAKE);
		assertEquals(1, bs.size());
		assertEquals(ICON + "Palm", bs.get(0).title);
	}

	@Test
	public void awakeHoursAcrossDaylightSaving()
	{
		// US clocks fall back at 2 AM on Nov 1 2026.
		assertEquals(at("2026-11-01T08:00"), AWAKE.dingTime(at("2026-11-01T01:30")));
		assertEquals(at("2026-10-31T23:00"), AWAKE.dingTime(at("2026-10-31T23:00")));
	}

	@Test
	public void fruitTreesAndCalquatsGetFollowOnsToo()
	{
		// After the 8 AM morning run, palms (16h) and calquats (21h20m) replanted then are
		// ready overnight, so they come back as one shared ding the next morning.
		List<ReadyPatch> ps = Arrays.asList(
			new ReadyPatch("fruit_tree", "Palm", "Catherby", at("2026-10-27T03:31"), PALM),
			new ReadyPatch("calquat", "Calquat", "Kastori", at("2026-10-27T06:11"), CALQUAT));
		List<Batch> bs = Batcher.build(ps, at("2026-10-27T09:00"), WINDOW, 1, LOOKAHEAD, AWAKE);
		assertEquals(ICON + "Palm, Calquat", only(bs, at("2026-10-28T08:00")).title);
		for (Batch b : bs)
		{
			assertEquals(8, java.time.Instant.ofEpochSecond(b.readyAt).atZone(NY).getHour());
		}
	}

	@Test
	public void plantOnceTreesGetNoFollowOns()
	{
		List<ReadyPatch> ps = Arrays.asList(
			new ReadyPatch("spirit_tree", "Spirit Tree", "Farming Guild", at("2026-10-27T15:00"), PALM));
		List<Batch> bs = Batcher.build(ps, at("2026-10-27T12:00"), WINDOW, 2, LOOKAHEAD, AWAKE);
		assertEquals(1, bs.size());
	}

	@Test
	public void celastrusGetsFollowOnsForMobileReplants()
	{
		// A celastrus (13h20m) planted on the 8 AM PC run dings that evening, then keeps
		// dinging after replants RuneLite never sees, like the other replanted trees.
		long celastrus = 800 * 60;
		List<ReadyPatch> ps = Arrays.asList(
			new ReadyPatch("celastrus", "Celastrus", "Farming Guild", at("2026-11-04T21:20"), celastrus));
		List<Batch> bs = Batcher.build(ps, at("2026-11-04T08:05"), WINDOW, 1, LOOKAHEAD, AWAKE);
		assertEquals(ICON + "Celastrus", only(bs, at("2026-11-04T21:20")).title);
		assertEquals(ICON + "Celastrus", only(bs, at("2026-11-05T10:40")).title);
		assertTrue(bs.size() > 2);
	}

	@Test
	public void smallDriftIsTheSameSchedule()
	{
		long now = at("2026-10-27T05:00");
		List<Batch> sent = Batcher.build(day(), now, WINDOW, 1, LOOKAHEAD, AWAKE);
		List<ReadyPatch> nudged = new ArrayList<>();
		for (ReadyPatch p : day())
		{
			nudged.add(new ReadyPatch(p.getTypeKey(), p.getProduceName(), p.getLocation(),
				p.getReadyAt() + 3 * 60, p.getGrowSeconds()));
		}
		List<Batch> next = Batcher.build(nudged, now + 3600, WINDOW, 1, LOOKAHEAD, AWAKE);
		assertTrue(Batcher.sameSchedule(sent, next, now + 3600, 10 * 60));
	}

	@Test
	public void realChangeIsANewSchedule()
	{
		long now = at("2026-10-27T05:00");
		List<Batch> sent = Batcher.build(day(), now, WINDOW, 1, LOOKAHEAD, AWAKE);
		List<ReadyPatch> moved = new ArrayList<>(day());
		moved.add(new ReadyPatch("tree", "Magic", "Lumbridge", at("2026-10-27T15:00"), YEW));
		List<Batch> next = Batcher.build(moved, now, WINDOW, 1, LOOKAHEAD, AWAKE);
		assertTrue(!Batcher.sameSchedule(sent, next, now, 10 * 60));
		assertTrue(!Batcher.sameSchedule(null, next, now, 10 * 60));
	}

	@Test
	public void rhythmCoversDaysOfMobileOnlyRuns()
	{
		// RuneLite last saw the yews three days ago. With one extra run a day, the calendar
		// still gets a morning and an afternoon yew run every day for the next few days.
		List<ReadyPatch> ps = new ArrayList<>();
		for (String place : Arrays.asList("Falador", "Lumbridge", "Varrock"))
		{
			ps.add(new ReadyPatch("tree", "Yew", place, at("2026-10-29T06:51"), YEW));
		}
		long now = at("2026-11-01T10:52");
		List<Batch> bs = Batcher.build(ps, now, WINDOW, 1, LOOKAHEAD, AWAKE);
		assertEquals(ICON + "Yew", only(bs, at("2026-11-01T14:40")).title);
		assertEquals(ICON + "Yew", only(bs, at("2026-11-02T08:00")).title);
		assertEquals(ICON + "Yew", only(bs, at("2026-11-02T14:40")).title);
		for (Batch b : bs)
		{
			assertTrue(b.title, b.readyAt <= now + Batcher.RHYTHM_SECONDS);
		}
		assertEquals(10, bs.size()); // Nov 1 afternoon, then Nov 2-5 morning + afternoon, then Nov 6 morning
	}

	@Test
	public void noAssumedRunsWhenFollowOnsAreOff()
	{
		List<ReadyPatch> ps = Arrays.asList(new ReadyPatch("tree", "Yew", "Falador", at("2026-10-29T06:51"), YEW));
		assertEquals(0, Batcher.build(ps, at("2026-11-01T10:52"), WINDOW, 0, LOOKAHEAD, AWAKE).size());
	}

	@Test
	public void plantingRunCountsAsOneOfTheDaysRuns()
	{
		// A full PC run at 8:30 makes the 15:10 ding that day's second run. With one run after
		// the morning, there's no third ding that evening: the next one is the morning.
		List<ReadyPatch> ps = Arrays.asList(new ReadyPatch("tree", "Yew", "Falador", at("2026-11-02T15:10"), YEW));
		List<Batch> bs = Batcher.build(ps, at("2026-11-02T08:35"), WINDOW, 1, LOOKAHEAD, AWAKE);
		assertEquals(at("2026-11-02T15:10"), bs.get(0).readyAt);
		assertEquals(at("2026-11-03T08:00"), bs.get(1).readyAt);
	}

	@Test
	public void abandonedPatchesGetNoRhythm()
	{
		// Last seen three weeks ago: the player stopped farming it, so it shouldn't join every run.
		List<ReadyPatch> ps = Arrays.asList(new ReadyPatch("tree", "Maple", "Varrock", at("2026-10-10T12:00"), YEW));
		assertEquals(0, Batcher.build(ps, at("2026-11-01T10:00"), WINDOW, 1, LOOKAHEAD, AWAKE).size());
	}

	@Test
	public void doneTreesCatchUpWithAFreshlyPlantedOne()
	{
		// Seen on PC: five palms replanted, due 23:51. Kastori and Lletya were replanted on the
		// same mobile run but RuneLite still thinks they finished yesterday. They should share
		// the 23:51 timer, so tonight's yew run doesn't mention palms at all.
		long now = at("2026-11-04T15:05");
		List<ReadyPatch> ps = new ArrayList<>();
		for (String place : Arrays.asList("Falador", "Lumbridge", "Varrock"))
		{
			ps.add(new ReadyPatch("tree", "Yew", place, at("2026-11-04T21:11"), YEW));
		}
		for (String place : Arrays.asList("Brimhaven", "Catherby", "Gnome Stronghold", "Farming Guild", "Tree Gnome Village"))
		{
			ps.add(new ReadyPatch("fruit_tree", "Palm", place, at("2026-11-04T23:51"), PALM));
		}
		ps.add(new ReadyPatch("fruit_tree", "Palm", "Kastori", at("2026-11-03T05:11"), PALM));
		ps.add(new ReadyPatch("fruit_tree", "Palm", "Lletya", at("2026-11-03T05:11"), PALM));
		List<Batch> bs = Batcher.build(ps, now, WINDOW, 1, LOOKAHEAD, AWAKE);
		assertEquals(ICON + "Yew", only(bs, at("2026-11-04T21:11")).title);
		Batch morning = only(bs, at("2026-11-05T08:00"));
		assertTrue(morning.detail, morning.detail.contains("Kastori") && morning.detail.contains("Lletya"));
	}

	@Test
	public void runningTimersAreNeverMoved()
	{
		// Six yews from the 8:00 PC run, plus one dead tree replanted at 11:00. The six keep 14:40.
		long now = at("2026-11-04T11:05");
		List<ReadyPatch> ps = new ArrayList<>();
		for (String place : Arrays.asList("Falador", "Lumbridge", "Varrock", "Taverley", "Gnome Stronghold", "Auburnvale"))
		{
			ps.add(new ReadyPatch("tree", "Yew", place, at("2026-11-04T14:40"), YEW));
		}
		ps.add(new ReadyPatch("tree", "Yew", "Farming Guild", at("2026-11-04T17:40"), YEW));
		List<ReadyPatch> out = Batcher.catchUpStalePatches(ps, now);
		assertEquals(ps, out);
	}

	@Test
	public void slowCropsAreReplantedOnTheRunThatAnnouncesThem()
	{
		// Yews and the Celastrus were planted on the 8:00 run. The Celastrus (13h20m) is ready at
		// 21:20 but only announced on the next 8:00 run, so that's when it gets replanted: it
		// belongs on each morning run, never on an afternoon yew ding while it's still growing.
		long now = at("2026-11-09T09:00");
		List<ReadyPatch> ps = new ArrayList<>();
		for (String place : Arrays.asList("Falador", "Lumbridge", "Varrock"))
		{
			ps.add(new ReadyPatch("tree", "Yew", place, at("2026-11-09T14:40"), YEW));
		}
		ps.add(new ReadyPatch("celastrus", "Celastrus", "Farming Guild", at("2026-11-09T21:20"), 800 * 60));
		List<Batch> bs = Batcher.build(ps, now, WINDOW, 1, LOOKAHEAD, AWAKE);
		for (String day : Arrays.asList("2026-11-10", "2026-11-11", "2026-11-12"))
		{
			assertEquals(ICON + "Yew, Celastrus", only(bs, at(day + "T08:00")).title);
			assertEquals(ICON + "Yew", only(bs, at(day + "T14:40")).title);
		}
	}
}
