package sk.nixone.bwu2.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import bwapi.Game;
import bwapi.Unit;
import bwapi.UnitType;
import sk.nixone.bwu2.math.Comparison;
import sk.nixone.bwu2.math.Vector2D;
import sk.nixone.bwu2.math.Vector2DMath;
import sk.nixone.bwu2.selection.UnitSelector.IntegerSelector;
import sk.nixone.bwu2.selection.UnitSelector.RealSelector;
import sk.nixone.bwu2.selection.actions.UnitAction;
import sk.nixone.bwu2.selection.actions.UnitActionBuffer;
import sk.nixone.bwu2.selection.aggregators.AverageRealAggregator;

/**
 * Represents a set of units and helper operations for unit selection.
 * 
 * @author nixone
 * 
 */
@SuppressWarnings("serial")
public class Units extends ArrayList<Unit>
{
	/**
	 * Creates an empty unit set
	 */
	public Units()
	{
		// empty for empty unitset
	}

	/**
	 * Creates an unit set from collection of units. If a single unit appears in
	 * this collection more than once, unit set will contain only one instance
	 * of the unit.
	 * 
	 * @param units
	 *            collection of units
	 */
	public Units(Collection<Unit> units)
	{
		for (Unit unit : units)
		{
			add(unit);
		}
	}
	
	public Units(Unit... units) {
		for (Unit unit : units) {
			add(unit);
		}
	}

	/**
	 * Creates a new unit set containing units from this unit set that are
	 * approved by the selector.
	 * 
	 * @param selector
	 * @return new unit set
	 */
	public Units where(UnitSelector.BooleanSelector selector)
	{
		Units result = new Units();
		for (Unit unit : this)
		{
			if (selector.isTrueFor(unit))
			{
				result.add(unit);
			}
		}
		return result;
	}

	/**
	 * Creates a new unit set containing units from this unit set that are not
	 * approved by the selector.
	 * 
	 * @param selector
	 * @return
	 */
	public Units whereNot(UnitSelector.BooleanSelector selector)
	{
		Units result = new Units();
		for (Unit unit : this)
		{
			if (!selector.isTrueFor(unit))
			{
				result.add(unit);
			}
		}
		return result;
	}

	/**
	 * Creates a new unit set containing units from this unit set that have the
	 * selector value less or equal to the value provided as a parameter.
	 * 
	 * @param selector
	 *            integer information selector
	 * @param value
	 *            threshold
	 * @return
	 */
	public Units whereLessOrEqual(UnitSelector.IntegerSelector selector, int value)
	{
		return where(new IntegerComparisonSelector(selector, value, Comparison.LESS_OR_EQUAL));
	}

	/**
	 * Creates a new unit set containing units from this unit set that have the
	 * selector value less or equal to the value provided as a parameter.
	 * 
	 * @param selector
	 *            real information selector
	 * @param value
	 *            threshold
	 * @return
	 */
	public Units whereLessOrEqual(UnitSelector.RealSelector selector, double value)
	{
		return where(new RealComparisonSelector(selector, value, Comparison.LESS_OR_EQUAL));
	}

	/**
	 * Creates a new unit set containing units from this unit set that have the
	 * selector value greather or equal to the value provided as a parameter.
	 * 
	 * @param selector
	 *            integer information selector
	 * @param value
	 *            threshold
	 * @return
	 */
	public Units whereGreatherOrEqual(UnitSelector.IntegerSelector selector, int value)
	{
		return where(new IntegerComparisonSelector(selector, value, Comparison.GREATER_OR_EQUAL));
	}

	/**
	 * Creates a new unit set containing units from this unit set that have the
	 * selector value greather or equal to the value provided as a parameter.
	 * 
	 * @param selector
	 *            real information selector
	 * @param value
	 *            threshold
	 * @return
	 */
	public Units whereGreatherOrEqual(UnitSelector.RealSelector selector, double value)
	{
		return where(new RealComparisonSelector(selector, value, Comparison.GREATER_OR_EQUAL));
	}

	/**
	 * Creates a new unit set containing units from this unit set that have the
	 * type of <code>unitType</code>
	 * 
	 * @param unitType
	 *            type to be equal
	 * @return
	 */
	public Units whereType(UnitType unitType)
	{
		return where(new UnitSelector.UnitTypeSelector(unitType));
	}

	/**
	 * Creates a new unit set containing units from this unit set that do not
	 * have the type of <code>unitType</code>
	 * 
	 * @param unitType
	 * @return
	 */
	public Units whereTypeNot(UnitType unitType)
	{
		return this.minus(this.whereType(unitType));
	}

	/**
	 * Computes arithmetic center of all units in this set.
	 * 
	 * @return vector representing center of this unit set
	 */
	public Vector2D getArithmeticCenter()
	{
		if (size() == 0)
		{
			return null;
		}

		Vector2D center = Vector2D.ZERO;

		for (Unit unit : this)
		{
			center = center.add(Vector2DMath.toVector(unit.getPosition()));
		}
		center = center.scale(1.0f / size());

		return center;
	}

	/**
	 * Computes average distance of this unit set (average over all units) from
	 * a certain given point.
	 * 
	 * @param point
	 * @return average distance from point
	 */
	public double getAverageDistanceFrom(Vector2D point)
	{
		return (new AverageRealAggregator(new DistanceSelector(point))).aggregate(this);
	}
	
	public float getMaximumDistanceFrom(DistanceSelector distanceSelector) {
		float best = Float.NEGATIVE_INFINITY;
		for (Unit unit : this) {
			float d = (float)distanceSelector.getValue(unit);
			if (d > best) {
				best = d;
			}
		}
		return best;
	}

	/**
	 * Returns the first unit of specified types that exists in this set.
	 * 
	 * @param unitTypes
	 *            possible types ordered by preference
	 * @return the first unit of specified types that exists in this set
	 */
	public Unit firstOf(UnitType... unitTypes)
	{
		for (UnitType type : unitTypes)
		{
			Units subSet = whereType(type);
			if (subSet.size() > 0)
			{
				return subSet.first();
			}
		}
		return null;
	}

	/**
	 * Returns the new set containing first <code>n</code> units
	 * of specified <code>unitTypes</code>. Order of provided unit types
	 * is important. If the specified count of units (<code>n</code>) can
	 * be reached by units of only first unit type, it would be done this way.
	 * Only when certain type of units is depleted from this set, the next unitType
	 * is used. If the required number of units cannot be reached, uncomplete set
	 * will be returned, so the size of returned set can possibly be less than <code>n</code>.
	 * 
	 * @param n 
	 * 			number of required units
	 * @param unitTypes
	 * 			required unit types
	 * @return set of required units
	 */
	public Units firstNOf(int n, UnitType... unitTypes)
	{
		Units result = new Units();

		for (UnitType type : unitTypes)
		{
			Units subSet = whereType(type);
			Iterator<Unit> it = subSet.iterator();

			while (it.hasNext() && result.size() < n)
			{
				result.add(it.next());
			}
		}

		return result;
	}

	/**
	 * Returns any single (first) unit from this set.
	 * 
	 * @return any single (first) unit from this set.
	 */
	public Unit first()
	{
		if (size() == 0)
		{
			return null;
		}
		return iterator().next();
	}

	public Units minus(Unit unit) {
		Units result = new Units(this);
		result.remove(unit);
		return result;
	}
	
	/**
	 * Returns the set of all units that are in this set but are not in the
	 * specified set.
	 * 
	 * @param units
	 * 			specified set
	 * @return all units that are in this set but are not in the specified one
	 */
	public Units minus(Units units)
	{
		Units result = new Units();
		for (Unit unit : this)
		{
			if (!units.contains(unit))
			{
				result.add(unit);
			}
		}
		return result;
	}

	/**
	 * Returns the set of all units that are in both this and specified set.
	 * 
	 * @param set
	 * 			specified set
	 * @return intersection of this and specified set
	 */
	public Units intersection(Units set)
	{
		Units result = new Units();

		for (Unit unit : this)
		{
			if (set.contains(unit))
			{
				result.add(unit);
			}
		}

		return result;
	}

	/**
	 * Returns all units that are in this set or in the specified set.
	 * 
	 * @param set
	 * @return fact
	 */
	public Units union(Units set)
	{
		Units result = new Units(this);

		result.addAll(set);

		return result;
	}

	/**
	 * Decides if these units are at specified position, decides if the average
	 * distance of all units from specified position in this set is smaller or
	 * equal to the tolerance.
	 * 
	 * @param position
	 * @param tolerance
	 * @return decision
	 */
	public boolean areAt(Vector2D position, double tolerance)
	{
		return getAverageDistanceFrom(position) <= tolerance;
	}
	
	public Unit pick(Picker picker)
	{
		return picker.pickFrom(this);
	}
	
	public Units limit(int maximum) {
		Units result = new Units();
		for (Unit unit : this) {
			if (result.size() >= maximum) break;
			result.add(unit);
		}
		return result;
	}
	
	public Units order(RealSelector ordering) {
		Units result = new Units();
		double lastValue = Double.NEGATIVE_INFINITY;
    	
    	while (result.size() < size()) {
    		double bestValue = Double.MAX_VALUE;
    		Unit bestUnit = null;
        	for (Unit unit : this) {
        		if (unit == bestUnit) continue;
        		if (result.contains(unit)) continue;
        		double value = ordering.getValue(unit);
        		if (value < lastValue) continue;
        		if (value <= bestValue) {
        			bestValue = value;
        			bestUnit = unit;
        		}
        	}
        	if (bestUnit == null) {
        		break;
        	} else {
        		result.add(bestUnit);
        		lastValue = bestValue;
        	}
    	}
    	return result;
	}
	
	public Units order(final IntegerSelector orderingSelector) {
		return order(new RealSelector() {
			@Override
			public double getValue(Unit unit) {
				return orderingSelector.getValue(unit);
			}
		});
	}
	
	public Units reverse() {
		Units result = new Units();
		for (int i=size()-1; i>=0; i--) {
			result.add(get(i));
		}
		return result;
	}
	
	public void act(UnitActionBuffer buffer, UnitAction action) {
		for (Unit unit : this) {
			buffer.act(unit, action);
		}
	}
	
	public int collectMax(IntegerSelector valueSelector) {
		int result = Integer.MIN_VALUE;
		for (Unit unit : this) {
			int v = valueSelector.getValue(unit);
			if (v > result) {
				result = v;
			}
		}
		return result;
	}
	
	public double collectMax(RealSelector valueSelector) {
		double result = Double.NEGATIVE_INFINITY;
		for (Unit unit : this) {
			double v = valueSelector.getValue(unit);
			if (v > result) {
				result = v;
			}
		}
		return result;
	}
	
	public int collectMin(IntegerSelector valueSelector) {
		int result = Integer.MAX_VALUE;
		for (Unit unit : this) {
			int v = valueSelector.getValue(unit);
			if (v < result) {
				result = v;
			}
		}
		return result;
	}
	
	public double collectMin(RealSelector valueSelector) {
		double result = Double.POSITIVE_INFINITY;
		for (Unit unit : this) {
			double v = valueSelector.getValue(unit);
			if (v < result) {
				result = v;
			}
		}
		return result;
	}
}
