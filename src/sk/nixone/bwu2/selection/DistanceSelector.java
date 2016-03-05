package sk.nixone.bwu2.selection;

import bwapi.Unit;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.math.Vector2DMath;
import sk.nixone.bwu2.selection.UnitSelector.RealSelector;

/**
 * Class usable as a selector of units that are in a certain relation in the
 * context of a distance from some unit or a fixed position.
 * 
 * @author nixone
 * 
 */
public class DistanceSelector implements RealSelector
{
	private Vector2D position = null;
	private Unit unit = null;

	/**
	 * Creates a distance selector from certain position
	 * 
	 * @param position
	 */
	public DistanceSelector(Vector2D position)
	{
		this.position = position;
	}

	/**
	 * Creates a distance selector from certain unit (position is gathered
	 * later, so it can be re-used in many frames).
	 * 
	 * @param unit
	 */
	public DistanceSelector(Unit unit)
	{
		this.unit = unit;
	}

	@Override
	public double getValue(Unit unit)
	{
		Vector2D origin = this.position;
		
		if (origin == null)
		{
			origin = Vector2DMath.toVector(this.unit.getPosition());
		}
		
		Vector2D unitPosition = Vector2DMath.toVector(unit.getPosition());
		
		double distance = origin.sub(unitPosition).getLength();
		return distance;
	}
}
