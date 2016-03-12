package sk.nixone.bwu2.selection.aggregators;

import bwapi.Unit;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.selection.UnitSelector.Vector2DSelector;
import sk.nixone.bwu2.selection.Units;

public class MeanVector2DAggregator implements Aggregator<Vector2D>
{
	private Vector2DSelector selector;
	
	public MeanVector2DAggregator(Vector2DSelector selector)
	{
		this.selector = selector;
	}
	
	public Vector2D aggregate(Units units)
	{
		Vector2D aggregate = Vector2D.ZERO;
		int aggregates = 0;
		
		for(Unit unit : units)
		{
			Vector2D sample = selector.getValue(unit);
			if(sample != null)
			{
				aggregate = aggregate.add(sample);
				aggregates++;
			}
		}
		
		if(aggregates > 0)
		{
			aggregate = aggregate.scale(1f/aggregates);
		}
		
		return aggregate;
	}
}
