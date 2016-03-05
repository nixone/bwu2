package sk.nixone.bwu2.selection.aggregators;

import bwapi.Unit;
import sk.nixone.bwu2.selection.UnitSelector.RealSelector;
import sk.nixone.bwu2.selection.UnitSet;

public class AverageRealAggregator implements Aggregator<Double>
{
	private RealSelector selector;
	
	public AverageRealAggregator(RealSelector selector)
	{
		this.selector = selector;
	}
	
	public Double aggregate(UnitSet units)
	{
		double accumulated = 0;
		
		for(Unit unit : units)
		{
			accumulated += selector.getValue(unit);
		}
		
		if(units.size() > 0)
		{
			accumulated /= units.size();
		}
		
		return accumulated;
	}
}
