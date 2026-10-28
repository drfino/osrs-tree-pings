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
		assertEquals("Morning run: Yew x7, Papaya x5", only(bs, at("2026-10-27T08:00")).title);
	}

	@Test
	public void slowCropsRideAlongOnTheNextTreeDing()
	{
		List<Batch> bs = Batcher.build(day(), at("2026-10-27T00:56"), WINDOW, 2, LOOKAHEAD, AWAKE);
		assertEquals("Yew x7 ready (if replanted), also Calquat x2", only(bs, at("2026-10-27T14:40")).title);
		assertEquals("Yew x7 ready (if replanted)", only(bs, at("2026-10-27T21:20")).title);
		assertEquals(3, bs.size());
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
		assertEquals("Yew ready, also Calquat", bs.get(0).title);
		assertEquals(at("2026-10-27T15:30"), bs.get(0).readyAt);
	}

	@Test
	public void withoutRegularTreesEveryCropDings()
	{
		List<ReadyPatch> ps = Arrays.asList(new ReadyPatch("fruit_tree", "Palm", "Catherby", at("2026-10-27T15:00"), PALM));
		List<Batch> bs = Batcher.build(ps, at("2026-10-27T12:00"), WINDOW, 2, LOOKAHEAD, AWAKE);
		assertEquals(1, bs.size());
		assertEquals("Palm ready", bs.get(0).title);
	}

	@Test
	public void awakeHoursAcrossDaylightSaving()
	{
		// US clocks fall back at 2 AM on Nov 1 2026.
		assertEquals(at("2026-11-01T08:00"), AWAKE.dingTime(at("2026-11-01T01:30")));
		assertEquals(at("2026-10-31T23:00"), AWAKE.dingTime(at("2026-10-31T23:00")));
	}

	@Test
	public void readyNowListsFinishedCrops()
	{
		assertEquals("Papaya x5, Yew x7", Batcher.readyNow(day(), at("2026-10-27T08:30")));
		assertEquals("", Batcher.readyNow(day(), at("2026-11-20T08:30")));
	}
}
