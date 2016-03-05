package sk.nixone.bwu2.selection;

import bwapi.Unit;
import sk.nixone.bwu2.selection.UnitSelector.BooleanSelector;

/**
 * Logical selectors of basic logical operations.
 * 
 * @author nixone
 * 
 */
public class LogicalSelector
{
	static public class Or implements BooleanSelector
	{
		BooleanSelector[] selectors;

		public Or(BooleanSelector... selectors)
		{
			this.selectors = selectors;
		}

		@Override
		public boolean isTrueFor(Unit unit)
		{
			for (BooleanSelector s : selectors)
			{
				if (s.isTrueFor(unit))
				{
					return true;
				}
			}
			return false;
		}
	}

	static public class And implements BooleanSelector
	{
		BooleanSelector[] selectors;

		public And(BooleanSelector... selectors)
		{
			this.selectors = selectors;
		}

		@Override
		public boolean isTrueFor(Unit unit)
		{
			for (BooleanSelector s : selectors)
			{
				if (!s.isTrueFor(unit))
				{
					return false;
				}
			}
			return true;
		}
	}

	static public class Nand implements BooleanSelector
	{
		BooleanSelector[] selectors;

		public Nand(BooleanSelector... selectors)
		{
			this.selectors = selectors;
		}

		@Override
		public boolean isTrueFor(Unit unit)
		{
			for (BooleanSelector s : selectors)
			{
				if (!s.isTrueFor(unit))
				{
					return true;
				}
			}
			return false;
		}
	}

	static public class Nor implements BooleanSelector
	{
		BooleanSelector[] selectors;

		public Nor(BooleanSelector... selectors)
		{
			this.selectors = selectors;
		}

		@Override
		public boolean isTrueFor(Unit unit)
		{
			for (BooleanSelector s : selectors)
			{
				if (s.isTrueFor(unit))
				{
					return false;
				}
			}
			return true;
		}
	}
}
