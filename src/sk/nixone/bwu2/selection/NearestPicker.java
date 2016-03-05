package sk.nixone.bwu2.selection;

import java.util.Set;

import bwapi.Position;
import bwapi.Unit;

public class NearestPicker implements Picker
{
	private final Unit unit;
	private final Position position;
	
	public NearestPicker(Unit unit)
	{
		this.unit = unit;
		this.position = null;
	}
	
	public NearestPicker(Position position)
	{
		this.unit = null;
		this.position = position;
	}
	
	@Override
	public Unit pickFrom(Set<Unit> units)
	{
		Unit selectedUnit = null;
		double shortestDistance = Double.POSITIVE_INFINITY;
		
		for (Unit setUnit : units)
		{
			Position position = getPosition();
			double distance = setUnit.getDistance(position);
			
			if (distance < shortestDistance)
			{
				shortestDistance = distance;
				selectedUnit = setUnit;
			}
		}
		
		return selectedUnit;
	}
	
	private Position getPosition()
	{
		if (unit != null)
		{
			return unit.getPosition();
		}
		else
		{
			return position;
		}
	}
}
