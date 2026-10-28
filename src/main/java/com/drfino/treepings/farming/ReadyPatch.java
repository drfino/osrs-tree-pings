package com.drfino.treepings.farming;

/** A belled patch that is still growing, with its predicted finish time. */
public final class ReadyPatch
{
	private final String typeKey;
	private final String produceName;
	private final String location;
	private final long readyAt;
	private final long growSeconds;

	public ReadyPatch(String typeKey, String produceName, String location, long readyAt, long growSeconds)
	{
		this.typeKey = typeKey;
		this.produceName = produceName;
		this.location = location;
		this.readyAt = readyAt;
		this.growSeconds = growSeconds;
	}

	/** Patch type, e.g. "tree", "fruit_tree", "special". */
	public String getTypeKey()
	{
		return typeKey;
	}

	/** Crop name as RuneLite names it, e.g. "Yew". */
	public String getProduceName()
	{
		return produceName;
	}

	/** Where the patch is, e.g. "Falador". */
	public String getLocation()
	{
		return location;
	}

	/** Predicted finish time, unix seconds. */
	public long getReadyAt()
	{
		return readyAt;
	}

	/** Full grow time from planting to ready, seconds. Used for follow-on dings. */
	public long getGrowSeconds()
	{
		return growSeconds;
	}
}
