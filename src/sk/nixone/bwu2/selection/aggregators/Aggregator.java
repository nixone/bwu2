package sk.nixone.bwu2.selection.aggregators;

import sk.nixone.bwu2.selection.UnitSet;

public interface Aggregator<T>
{
	public T aggregate(UnitSet units);
}
