package sk.nixone.bwu2.selection;

import java.util.Set;

import bwapi.Unit;

public class Pickers
{
	public static Picker FIRST = new Picker()
	{
		@Override
		public Unit pickFrom(Set<Unit> units)
		{
			if (units.isEmpty())
			{
				return null;
			}
			else
			{
				return units.iterator().next();
			}
		}
	};
}
