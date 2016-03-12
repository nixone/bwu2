package sk.nixone.bwu2.selection.aggregators;

import sk.nixone.bwu2.selection.Units;

public interface Aggregator<T>
{
	public T aggregate(Units units);
}
