package com.treepings;

/** One calendar event: a group of patches that finish together. Serialized as-is to JSON. */
public final class Batch
{
	final String key;
	final String title;
	final long readyAt;
	final String detail;

	Batch(String key, String title, long readyAt, String detail)
	{
		this.key = key;
		this.title = title;
		this.readyAt = readyAt;
		this.detail = detail;
	}

	@Override
	public String toString()
	{
		return key + " @" + readyAt + " \"" + title + "\" [" + detail + "]";
	}
}
